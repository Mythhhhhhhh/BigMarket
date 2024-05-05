package cn.myth.domain.strategy.service.armory;

import cn.myth.domain.strategy.model.entity.StrategyAwardEntity;
import cn.myth.domain.strategy.model.entity.StrategyEntity;
import cn.myth.domain.strategy.model.entity.StrategyRuleEntity;
import cn.myth.domain.strategy.repository.IStrategyRepository;
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
    public boolean assembleLotteryStrategy(Long strategyId) {
        // 1. 查询策略配置
        List<StrategyAwardEntity> strategyAwardEntityList = repository.queryStrategyAwardList(strategyId);
        if (CollectionUtils.isEmpty(strategyAwardEntityList)) return false;
        assembleLotteryStrategy(String.valueOf(strategyId), strategyAwardEntityList);

        // 2. 权重策略配置 - 适用于 rule_weight 权重规则配置
        StrategyEntity strategyEntity = repository.queryStrategyEntityByStrategyId(strategyId);
        String ruleWeight = strategyEntity.getRuleWeight();
        if (ruleWeight == null) return true;

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
            assembleLotteryStrategy(String.valueOf(strategyId).concat("_").concat(key), strategyAwardEntityListClone);
        });

        return true;
    }

    public void assembleLotteryStrategy(String key, List<StrategyAwardEntity> strategyAwardEntityList) {
        // 1.获取最小概率值
        BigDecimal minAwardRate = strategyAwardEntityList.stream()
                .map(StrategyAwardEntity::awardRate)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        // 2.获取概率值总和
        BigDecimal totalAwardRate = strategyAwardEntityList.stream()
                .map(StrategyAwardEntity::awardRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. 用 1 % 0.0001 获得概率范围，百分位、千分位、万分位 向上取整
        BigDecimal rateRange = totalAwardRate.divide(minAwardRate, 0, RoundingMode.CEILING);

        // 4. 生成策略奖品概率查找表「这里指需要在list集合中，存放上对应的奖品占位即可，占位越多等于概率越高」
        List<Integer> strategyAwardSearchRateTableList = strategyAwardEntityList.stream()
                .flatMap(strategyAward -> {
                    Integer awardId = strategyAward.awardId();
                    BigDecimal awardRate = strategyAward.awardRate();
                    int count = rateRange.multiply(awardRate).setScale(0, RoundingMode.CEILING).intValue();
                    return IntStream.range(0, count).mapToObj(i -> awardId);
                })
                .collect(Collectors.toList());

        // 5. 对存储的奖品进行乱序操作
        Collections.shuffle(strategyAwardSearchRateTableList);

        // 6. 生成出Map集合，key值，对应的就是后续的概率值。通过概率来获得对应的奖品ID
        Map<Integer, Integer> shuffleStrategyAwardSearchRateTable = IntStream.range(0, strategyAwardSearchRateTableList.size())
                .boxed()
                .collect(Collectors.toMap(
                        i -> i,// 键就是索引本身
                        strategyAwardSearchRateTableList::get,// 值是对应索引的奖品ID
                        (oldValue, newValue) -> oldValue,// 如果有冲突（这种情况不会发生，因为我们用的是索引），保留旧值
                        LinkedHashMap::new
                ));

        // 7. 存放到 Redis
        repository.storeStrategyAwardSearchRateTable(key, shuffleStrategyAwardSearchRateTable.size(), shuffleStrategyAwardSearchRateTable);
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
        String key = String.valueOf(strategyId).concat("_").concat(ruleWeightValue);
        // 分布式部署下，不一定为当前应用做的策略装配。也就是值不一定会保存到本应用，而是分布式应用，所以需要从 Redis 中获取。
        int rateRange = repository.getRateRange(key);
        // 通过生成的随机值，获取概率值奖品查找表的结果
        return repository.getStrategyAwardAssemble(key, new SecureRandom().nextInt(rateRange));
    }


}
