package cn.myth.domain.strategy.service.armory;

import cn.myth.domain.strategy.model.entity.StrategyAwardEntity;
import cn.myth.domain.strategy.model.entity.StrategyEntity;
import cn.myth.domain.strategy.model.entity.StrategyRuleEntity;
import cn.myth.domain.strategy.repository.IStrategyRepository;
import cn.myth.types.common.Constants;
import cn.myth.types.enums.ResponseCode;
import cn.myth.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 策略装配库(兵工厂)，负责初始化策略计算
 */
@Slf4j
@Service
public class StrategyArmoryDispatch implements IStrategyArmory, IStrategyDispatch {

    @Resource
    private IStrategyRepository repository;

    @Override
    public boolean assembleLotteryStrategyByActivityId(Long activityId) {
        Long strategyId = repository.queryStrategyIdByActivityId(activityId);
        return assembleLotteryStrategy(strategyId);
    }

    @Override
    public boolean assembleLotteryStrategy(Long strategyId) {
        // 1. 查询策略配置
        List<StrategyAwardEntity> strategyAwardEntityList = repository.queryStrategyAwardList(strategyId);
        if (CollectionUtils.isEmpty(strategyAwardEntityList)) return false;
        assembleLotteryStrategy(String.valueOf(strategyId), strategyAwardEntityList);

        // 2.缓存奖品库存【用于decr扣减库存使用】
        strategyAwardEntityList.forEach(strategyAwardEntity -> {
            cacheStrategyAwardCount(strategyId, strategyAwardEntity.awardId(), strategyAwardEntity.awardCountSurplus());
        });

        // 2. 权重策略配置 - 适用于 rule_weight 权重规则配置
        StrategyEntity strategyEntity = repository.queryStrategyEntityByStrategyId(strategyId);
        String ruleWeight = strategyEntity.getRuleWeight();
        if (ruleWeight == null) return true;
        // TODO queryStrategyRule 方法名称限定，只查询一个对象。目前可能造成别人调用查询list返回

        StrategyRuleEntity strategyRuleEntity = repository.queryStrategyRule(strategyId, ruleWeight);
        if (strategyRuleEntity == null) {
            throw new AppException(ResponseCode.STRATEGY_RULE_WEIGHT_IS_NULL.getCode(), ResponseCode.STRATEGY_RULE_WEIGHT_IS_NULL.getInfo());
        }

        Map<String, List<Integer>> ruleWeightValueMap = strategyRuleEntity.getRuleWeightValues();
        Set<String> keys = ruleWeightValueMap.keySet();
        keys.forEach(key -> {
            List<Integer> ruleWeightValues = ruleWeightValueMap.get(key);
            List<StrategyAwardEntity> strategyAwardEntityListClone = strategyAwardEntityList.stream()
                    .filter(entity -> ruleWeightValues.contains(entity.awardId()))
                    .collect(Collectors.toList());
            assembleLotteryStrategy(String.valueOf(strategyId).concat(Constants.UNDERLINE).concat(key), strategyAwardEntityListClone);
        });

        return true;
    }

    /**
     * 计算公式；
     * 1. 找到范围内最小的概率值，比如 0.1、0.02、0.003，需要找到的值是 0.003
     * 2. 基于1找到的最小值，0.003 就可以计算出百分比、千分比的整数值。这里就是1000
     * 3. 那么「概率 * 1000」分别占比100个、20个、3个，总计是123个
     * 4. 后续的抽奖就用123作为随机数的范围值，生成的值100个都是0.1概率的奖品、20个是概率0.02的奖品、最后是3个是0.003的奖品。
     */
    public void assembleLotteryStrategy(String key, List<StrategyAwardEntity> strategyAwardEntityList) {
        // 1.获取最小概率值
        BigDecimal minAwardRate = strategyAwardEntityList.stream()
                .map(StrategyAwardEntity::awardRate)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

//        // 2.获取概率值总和
//        BigDecimal totalAwardRate = strategyAwardEntityList.stream()
//                .map(StrategyAwardEntity::awardRate)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        // 3. 用 1 % 0.0001 获得概率范围，百分位、千分位、万分位 向上取整
//        BigDecimal rateRange = totalAwardRate.divide(minAwardRate, 0, RoundingMode.CEILING);

        // 2. 用 1 * 1000 获得概率范围，百分位、千分位、万分位
        BigDecimal rateRange = BigDecimal.valueOf(convert(minAwardRate.doubleValue()));

        // 3. 生成策略奖品概率查找表「这里指需要在list集合中，存放上对应的奖品占位即可，占位越多等于概率越高」
        List<Integer> strategyAwardSearchRateTableList = strategyAwardEntityList.stream()
                .flatMap(strategyAward -> {
                    Integer awardId = strategyAward.awardId();
                    BigDecimal awardRate = strategyAward.awardRate();
                    int count = rateRange.multiply(awardRate).setScale(0, RoundingMode.CEILING).intValue();
                    return IntStream.range(0, count).mapToObj(i -> awardId);
                })
                .collect(Collectors.toList());

        // 4. 对存储的奖品进行乱序操作
        Collections.shuffle(strategyAwardSearchRateTableList);

        // 5. 生成出Map集合，key值，对应的就是后续的概率值。通过概率来获得对应的奖品ID
        Map<Integer, Integer> shuffleStrategyAwardSearchRateTable = IntStream.range(0, strategyAwardSearchRateTableList.size())
                .boxed()
                .collect(Collectors.toMap(
                        i -> i,// 键就是索引本身
                        strategyAwardSearchRateTableList::get,// 值是对应索引的奖品ID
                        (oldValue, newValue) -> oldValue,// 如果有冲突（这种情况不会发生，因为我们用的是索引），保留旧值
                        LinkedHashMap::new
                ));

        // 6. 存放到 Redis
        repository.storeStrategyAwardSearchRateTable(key, shuffleStrategyAwardSearchRateTable.size(), shuffleStrategyAwardSearchRateTable);
    }

    /**
     * 转换计算，只根据小数位来计算。如【0.01返回100】、【0.009返回1000】、【0.0018返回10000】
     */
    private double convert(double min){
        double current = min;
        double max = 1;
        while (current < 1){
            current = current * 10;
            max = max * 10;
        }
        return max;
    }

    /**
     * 缓存奖品库存到Redis
     *
     * @param strategyId 策略ID
     * @param awardId    奖品ID
     * @param awardCount 奖品库存
     */
    private void cacheStrategyAwardCount(Long strategyId, Integer awardId, Integer awardCount) {
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_COUNT_KEY + strategyId + Constants.UNDERLINE + awardId;
        repository.cacheStrategyAwardCount(cacheKey, awardCount);
    }


    @Override
    public Integer getRandomAwardId(Long strategyId) {
        // 分布式部署下，不一定为当前应用做的策略装配。也就是值不一定会保存到本应用，而是分布式应用，所以需要从 Redis 中获取。
        int rateRange = repository.getRateRange(strategyId);
        // 通过生成的随机值，获取概率值奖品查找表的结果
        return repository.getStrategyAwardAssemble(String.valueOf(strategyId), new SecureRandom().nextInt(rateRange));
    }

    @Override
    public Integer getRandomAwardId(Long strategyId, String ruleWeightValue) {
        String key = String.valueOf(strategyId).concat(Constants.UNDERLINE).concat(ruleWeightValue);
        // 分布式部署下，不一定为当前应用做的策略装配。也就是值不一定会保存到本应用，而是分布式应用，所以需要从 Redis 中获取。
        int rateRange = repository.getRateRange(key);
        // 通过生成的随机值，获取概率值奖品查找表的结果
        return repository.getStrategyAwardAssemble(key, new SecureRandom().nextInt(rateRange));
    }

    @Override
    public Boolean subtractionAwardStock(Long strategyId, Integer awardId, Date endDateTime) {
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_COUNT_KEY + strategyId + Constants.UNDERLINE + awardId;
        return repository.subtractionAwardStock(cacheKey, endDateTime);
    }


}
