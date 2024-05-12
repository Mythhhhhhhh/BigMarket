package cn.myth.domain.strategy.service.rule.tree.factory.engine.impl;

import cn.myth.domain.strategy.model.vo.RuleLogicCheckTypeVO;
import cn.myth.domain.strategy.model.vo.RuleTreeNodeLineVO;
import cn.myth.domain.strategy.model.vo.RuleTreeNodeVO;
import cn.myth.domain.strategy.model.vo.RuleTreeVO;
import cn.myth.domain.strategy.service.rule.tree.ILogicTreeNode;
import cn.myth.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import cn.myth.domain.strategy.service.rule.tree.factory.engine.IDecisionTreeEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 决策树引擎
 */
@Slf4j
@RequiredArgsConstructor
public class DecisionTreeEngine implements IDecisionTreeEngine {

    private final Map<String, ILogicTreeNode> logicTreeNodeMap;

    private final RuleTreeVO ruleTreeVO;

    @Override
    public DefaultTreeFactory.StrategyAwardData process(String userId, Long strategyId, Integer awardId) {
        DefaultTreeFactory.StrategyAwardData strategyAwardData = null;

        // 获取基础信息
        String nextNode = ruleTreeVO.getTreeRootRuleNode();
        Map<String, RuleTreeNodeVO> treeNodeMap = ruleTreeVO.getTreeNodeMap();

        // 获取起始节点「根节点记录了第一个要执行的规则」
        RuleTreeNodeVO ruleTreeNode = treeNodeMap.get(nextNode);
        while (nextNode != null) {
            // 获取决策节点
            ILogicTreeNode logicTreeNode = logicTreeNodeMap.get(ruleTreeNode.getRuleKey());

            // 决策节点计算
            DefaultTreeFactory.TreeActionEntity logicEntity = logicTreeNode.logic(userId, strategyId, awardId);

            RuleLogicCheckTypeVO ruleLogicCheckTypeVO = logicEntity.getRuleLogicCheckType();
            strategyAwardData = logicEntity.getStrategyAwardData();
            log.info("决策树引擎【{}】treeId:{} node:{} code:{}", ruleTreeVO.getTreeName(), ruleTreeVO.getTreeId(), nextNode, ruleLogicCheckTypeVO.getCode());

            // 获取下个节点
            nextNode = nextNode(ruleLogicCheckTypeVO.getCode(), ruleTreeNode.getTreeNodeLineVOList());
            ruleTreeNode = treeNodeMap.get(nextNode);
        }

        // 返回最终结果
        return strategyAwardData;
    }

    public String nextNode(String matterValue, List<RuleTreeNodeLineVO> treeNodeLineVOList) {
        if (CollectionUtils.isEmpty(treeNodeLineVOList)) return null;
        Optional<RuleTreeNodeLineVO> matchedNodeLine = treeNodeLineVOList.stream()
                .filter(nodeLine -> decisionLogic(matterValue, nodeLine))
                .findFirst();

        return matchedNodeLine.map(RuleTreeNodeLineVO::getRuleNodeTo)
                .orElseThrow(() -> new RuntimeException("决策树引擎，nextNode 计算失败，未找到可执行节点：" + matterValue));
    }


    public boolean decisionLogic(String matterValue, RuleTreeNodeLineVO nodeLine) {
        switch (nodeLine.getRuleLimitType()) {
            case EQUAL:
                return matterValue.equals(nodeLine.getRuleLimitValue().getCode());
            // 以下规则暂时不需要实现
            case GT:
            case LT:
            case GE:
            case LE:
            default:
                return false;
        }
    }
}
