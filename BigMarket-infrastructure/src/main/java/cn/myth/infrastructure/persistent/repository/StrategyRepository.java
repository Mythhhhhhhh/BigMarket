package cn.myth.infrastructure.persistent.repository;

import cn.myth.domain.strategy.model.entity.StrategyAwardEntity;
import cn.myth.domain.strategy.repository.IStrategyRepository;
import cn.myth.infrastructure.persistent.dao.IStrategyAwardDao;
import cn.myth.infrastructure.persistent.po.StrategyAward;
import cn.myth.infrastructure.persistent.redis.IRedisService;
import cn.myth.types.common.Constants;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 策略服务仓储实现
 */
@Repository
public class StrategyRepository implements IStrategyRepository {

    @Resource
    private IStrategyAwardDao strategyAwardDao;
    @Resource
    private IRedisService redisService;

    @Override
    public List<StrategyAwardEntity> queryStrategyAwardList(Long strategyId) {
        // 优先从缓存获取
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_KEY + strategyId;
        List<StrategyAwardEntity> strategyAwardEntityList = redisService.getValue(cacheKey);
        if (!CollectionUtils.isEmpty(strategyAwardEntityList)) return strategyAwardEntityList;
        // 从库中获取数据
        List<StrategyAward> strategyAwards = strategyAwardDao.queryStrategyAwardListByStrategyId(strategyId);
        // 转换
        strategyAwardEntityList = strategyAwards.stream()
                .map(strategyAward -> {
                    StrategyAwardEntity strategyAwardEntity = new StrategyAwardEntity();
                    strategyAwardEntity.strategyId(strategyAward.getId());
                    strategyAwardEntity.awardId(strategyAward.getAwardId());
                    strategyAwardEntity.awardCount(strategyAward.getAwardCount());
                    strategyAwardEntity.awardCountSurplus(strategyAward.getAwardCountSurplus());
                    strategyAwardEntity.awardRate(strategyAward.getAwardRate());
                    return strategyAwardEntity;
                })
                .collect(Collectors.toList());
        redisService.setValue(cacheKey, strategyAwardEntityList);
        return strategyAwardEntityList;
    }

    @Override
    public void storeStrategyAwardSearchRateTable(Long strategyId, Integer rateRange, Map<Integer, Integer> strategyAwardSearchRateTable) {
        // 1. 存储抽奖策略范围值，如10000，用于生成1000以内的随机数
        redisService.setValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + strategyId, rateRange);
        // 2. 存储概率查找表
        Map<Integer, Integer> cacheRateTable = redisService.getMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + strategyId);
        cacheRateTable.putAll(strategyAwardSearchRateTable); // 这里是取出一个代理对象，对它的任何操作都会同步到给redis
    }

    @Override
    public Integer getStrategyAwardAssemble(Long strategyId, Integer rateKey) {
        return redisService.getFromMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + strategyId, rateKey);
    }

    @Override
    public int getRateRange(Long strategyId) {
        return redisService.getValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + strategyId);
    }
}
