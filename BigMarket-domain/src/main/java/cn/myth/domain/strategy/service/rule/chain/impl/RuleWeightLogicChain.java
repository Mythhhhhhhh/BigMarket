package cn.myth.domain.strategy.service.rule.chain.impl;

import cn.myth.domain.strategy.repository.IStrategyRepository;
import cn.myth.domain.strategy.service.armory.IStrategyDispatch;
import cn.myth.domain.strategy.service.rule.chain.AbstractLogicChain;
import cn.myth.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import cn.myth.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 权重抽奖责任链
 */
@Slf4j
@Component("rule_weight")
public class RuleWeightLogicChain extends AbstractLogicChain {

    @Resource
    private IStrategyRepository repository;
    @Resource
    protected IStrategyDispatch strategyDispatch;
    // 根据用户ID查询用户抽奖消耗的积分值，我们先写死为固定的值。后续需要从数据库中查询。
    public Long userScore = 0L;

    /**
     * 权重责任链过滤；
     * 1. 权重规则格式；4000:102,103,104,105 5000:102,103,104,105,106,107 6000:102,103,104,105,106,107,108,109
     * 2. 解析数据格式；判断哪个范围符合用户的特定抽奖范围
     */
    @Override
    public DefaultChainFactory.StrategyAwardVO logic(String userId, Long strategyId) {
        log.info("抽奖责任链-权重开始 userId: {} strategyId: {} ruleModel: {}", userId, strategyId, ruleModel());
        String ruleValue = repository.queryStrategyRuleValue(strategyId, ruleModel());
        // 1. 解析权重规则值 4000:102,103,104,105 拆解为；4000 -> 4000:102,103,104,105 便于比对判断
        Map<Long, String> analyticalValueGroup = getAnalyticalValue(ruleValue);
        if (null == analyticalValueGroup || analyticalValueGroup.isEmpty()) {
            log.warn("抽奖责任链-权重告警【策略配置权重，但ruleValue未配置相应值】 userId: {} strategyId: {} ruleModel: {}", userId, strategyId, ruleModel());
            return next().logic(userId, strategyId);
        }

        // 2. 转换Keys值，并默认排序
        List<Long> analyticalSortedKeys = new ArrayList<>(analyticalValueGroup.keySet());
        Collections.sort(analyticalSortedKeys);

        // 3. 找出最小符合的值，也就是【4500 积分，能找到 4000:102,103,104,105】、【5000 积分，能找到 5000:102,103,104,105,106,107】
        Long nextValue = analyticalSortedKeys.stream()
                .sorted(Comparator.reverseOrder())
                .filter(key -> userScore >= key)
                .findFirst()
                .orElse(null);

        // 4. 权重抽奖
        if (null != nextValue) {
            Integer awardId = strategyDispatch.getRandomAwardId(strategyId, analyticalValueGroup.get(nextValue));
            log.info("抽奖责任链-权重接管 userId: {} strategyId: {} ruleModel: {} awardId: {}", userId, strategyId, ruleModel(), awardId);
            return DefaultChainFactory.StrategyAwardVO.builder()
                    .awardId(awardId)
                    .logicModel(ruleModel())
                    .build();
        }

        // 5. 过滤其他责任链
        log.info("抽奖责任链-权重放行 userId: {} strategyId: {} ruleModel: {}", userId, strategyId, ruleModel());
        return next().logic(userId, strategyId);
    }

    @Override
    protected String ruleModel() {
        return DefaultChainFactory.LogicModel.RULE_WEIGHT.getCode();
    }


    private Map<Long, String> getAnalyticalValue(String ruleValue) {
        String[] ruleValueGroups = ruleValue.split(Constants.SPACE);
        return Arrays.stream(ruleValueGroups)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toMap(
                        ruleValueKey -> {
                            String[] parts = ruleValueKey.split(Constants.COLON);
                            if (parts.length != 2) {
                                throw new IllegalArgumentException("rule_weight rule_rule invalid input format" + ruleValueKey);
                            }
                            return Long.parseLong(parts[0]);
                        },
                        ruleValueKey -> ruleValueKey,
                        (oldValue, newValue) -> oldValue,
                        HashMap::new
                ));
    }
}
