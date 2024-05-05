package cn.myth.domain.strategy.service.rule.impl;

import cn.myth.domain.strategy.model.entity.RuleActionEntity;
import cn.myth.domain.strategy.model.entity.RuleMatterEntity;
import cn.myth.domain.strategy.model.vo.RuleLogicCheckTypeVO;
import cn.myth.domain.strategy.repository.IStrategyRepository;
import cn.myth.domain.strategy.service.annotation.LogicStrategy;
import cn.myth.domain.strategy.service.rule.ILogicFilter;
import cn.myth.domain.strategy.service.rule.factory.DefaultLogicFactory;
import cn.myth.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
@Component
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.RULE_BLACKLIST)
public class RuleBackListLogicFilter implements ILogicFilter {

    @Resource
    private IStrategyRepository repository;

    @Override
    public RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> filter(RuleMatterEntity ruleMatterEntity) {
        log.info("规则过滤-黑名单 userId:{} strategyId:{} ruleModel:{}", ruleMatterEntity.userId(), ruleMatterEntity.strategyId(), ruleMatterEntity.ruleModel());
        String userId = ruleMatterEntity.userId();

        // 查询规则值配置
        String ruleValue = repository.queryStrategyRuleValue(ruleMatterEntity.strategyId(), ruleMatterEntity.awardId(), ruleMatterEntity.ruleModel());
        String[] splitRuleValue = ruleValue.split(Constants.COLON);
        Integer awardId = Integer.parseInt(splitRuleValue[0]);

        // 过滤其他规则
        String[] userBlackIds = splitRuleValue[1].split(Constants.SPLIT);
        Optional<String> result = Arrays.stream(userBlackIds)
                .filter(userId::equals)
                .findFirst();

        return result.isPresent()
                ? new RuleActionEntity<RuleActionEntity.RaffleBeforeEntity>()
                .ruleModel(DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode())
                .data(new RuleActionEntity.RaffleBeforeEntity()
                        .strategyId(ruleMatterEntity.strategyId())
                        .awardId(awardId))
                .code(RuleLogicCheckTypeVO.TAKE_OVER.getCode())
                .info(RuleLogicCheckTypeVO.TAKE_OVER.getInfo())
                : new RuleActionEntity<RuleActionEntity.RaffleBeforeEntity>()
                .code(RuleLogicCheckTypeVO.ALLOW.getCode())
                .info(RuleLogicCheckTypeVO.ALLOW.getInfo());
    }
}
