package cn.myth.domain.strategy.service.rule.tree.factory;

import cn.myth.domain.strategy.model.vo.RuleLogicCheckTypeVO;
import cn.myth.domain.strategy.model.vo.RuleTreeVO;
import cn.myth.domain.strategy.service.rule.tree.ILogicTreeNode;
import cn.myth.domain.strategy.service.rule.tree.factory.engine.IDecisionTreeEngine;
import cn.myth.domain.strategy.service.rule.tree.factory.engine.impl.DecisionTreeEngine;
import lombok.*;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 规则树工厂
 */
@Service
@RequiredArgsConstructor
public class DefaultTreeFactory {

    private final Map<String, ILogicTreeNode> logicTreeNodeGroup;

    public IDecisionTreeEngine openLogicTree(RuleTreeVO ruleTreeVO) {
        return new DecisionTreeEngine(logicTreeNodeGroup, ruleTreeVO);
    }

    /**
     * 决策树个动作实习
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TreeActionEntity {
        private RuleLogicCheckTypeVO ruleLogicCheckType;
        private StrategyAwardData strategyAwardData;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StrategyAwardData {
        /** 抽奖奖品ID - 内部流转使用 */
        private Integer awardId;
        /** 抽奖奖品规则 */
        private String awardRuleValue;
    }
}
