package cn.myth.domain.strategy.model.vo;

import cn.myth.domain.strategy.service.rule.filter.factory.DefaultLogicFactory;
import cn.myth.types.common.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;

/**
 * 抽奖策略规则规则值对象；值对象，没有唯一ID，仅限于从数据库查询对象
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StrategyAwardRuleModelVO {

    private String ruleModels;

    /**
     * 获取抽奖中规则；或者使用 lambda 表达式
     */
    public String[] raffleCenterRuleModelList() {
        return Arrays.stream(ruleModels.split(Constants.SPLIT))
                .filter(DefaultLogicFactory.LogicModel::isCenter)
                .toArray(String[]::new);
    }

    public String[] raffleAfterRuleModelList() {
        return Arrays.stream(ruleModels.split(Constants.SPLIT))
                .filter(DefaultLogicFactory.LogicModel::isAfter)
                .toArray(String[]::new);
    }

}
