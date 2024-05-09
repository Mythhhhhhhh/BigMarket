package cn.myth.domain.strategy.model.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * 策略实体
 */
@Data
@Accessors(chain = true, fluent = true)
public class StrategyEntity {

    /** 抽奖策略ID */
    private Long strategyId;
    /** 抽奖策略描述 */
    private String strategyDesc;
    /** 抽奖规则模型 rule_weight,rule_blacklist */
    private String ruleModels;

    public String[] ruleModels() {
        if (StringUtils.isBlank(ruleModels)) return null;
        return ruleModels.split(",");
    }

    public String getRuleWeight() {
        return Optional.ofNullable(ruleModels())
                .map(Arrays::stream)
                .orElseGet(Stream::empty)
                .filter("rule_weight"::equals)
                .findFirst()
                .orElse(null);
    }

}
