package cn.myth.domain.strategy.service.raffle;

import cn.myth.domain.strategy.model.entity.*;
import cn.myth.domain.strategy.model.vo.RuleLogicCheckTypeVO;
import cn.myth.domain.strategy.repository.IStrategyRepository;
import cn.myth.domain.strategy.service.AbstractRaffleStrategy;
import cn.myth.domain.strategy.service.armory.IStrategyDispatch;
import cn.myth.domain.strategy.service.rule.ILogicFilter;
import cn.myth.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import cn.myth.domain.strategy.service.rule.filter.factory.DefaultLogicFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 默认的抽奖策略实现
 */
@Slf4j
@Service
public class DefaultRaffleStrategy extends AbstractRaffleStrategy {

    @Resource
    private DefaultLogicFactory logicFactory;

    public DefaultRaffleStrategy(IStrategyRepository repository, IStrategyDispatch strategyDispatch, DefaultChainFactory defaultChainFactory) {
        super(repository, strategyDispatch, defaultChainFactory);
    }

    @Override
    protected RuleActionEntity<RuleActionEntity.RaffleCenterEntity> doCheckRaffleCenterLogic(RaffleFactorEntity raffleFactorEntity, String... logics) {
        if (logics == null || 0 == logics.length) return new RuleActionEntity<RuleActionEntity.RaffleCenterEntity>()
                .code(RuleLogicCheckTypeVO.ALLOW.getCode())
                .info(RuleLogicCheckTypeVO.ALLOW.getInfo());

        Map<String, ILogicFilter<RuleActionEntity.RaffleCenterEntity>> logicFilterGroup = logicFactory.openLogicFilter();

        RuleActionEntity<RuleActionEntity.RaffleCenterEntity> ruleActionEntity = null;
        for (String ruleModel : logics) {
            ILogicFilter<RuleActionEntity.RaffleCenterEntity> logicFilter = logicFilterGroup.get(ruleModel);
            RuleMatterEntity ruleMatterEntity = new RuleMatterEntity()
                    .userId(raffleFactorEntity.userId())
                    .strategyId(raffleFactorEntity.strategyId())
                    .awardId(raffleFactorEntity.awardId())
                    .ruleModel(ruleModel);
            ruleActionEntity = logicFilter.filter(ruleMatterEntity);
            // 非放行结果则顺序过滤
            log.info("抽奖中规则过滤 userId: {} ruleModel: {} code: {} info: {}", raffleFactorEntity.userId(), ruleModel, ruleActionEntity.code(), ruleActionEntity.info());
            if (!RuleLogicCheckTypeVO.ALLOW.getCode().equals(ruleActionEntity.code())) return ruleActionEntity;
        }
        return ruleActionEntity;
    }


}
