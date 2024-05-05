package cn.myth.domain.strategy.model.entity;

import cn.myth.types.common.Constants;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 策略规则实体
 */
@Data
@Accessors(chain = true, fluent = true)
public class StrategyRuleEntity {

    /** 抽奖策略ID */
    private Long strategyId;
    /** 抽奖奖品ID【规则类型为策略，则不需要奖品ID】 */
    private Integer awardId;
    /** 抽象规则类型；1-策略规则、2-奖品规则 */
    private Integer ruleType;
    /** 抽奖规则类型【rule_random - 随机值计算、rule_lock - 抽奖几次后解锁、rule_luck_award - 幸运奖(兜底奖品)】 */
    private String ruleModel;
    /** 抽奖规则比值 */
    private String ruleValue;
    /** 抽奖规则描述 */
    private String ruleDesc;

    /**
     * 获取权重值
     * 数据案例；4000:102,103,104,105 5000:102,103,104,105,106,107 6000:102,103,104,105,106,107,108,109
     */
    public Map<String, List<Integer>> getRuleWeightValues() {
        if (!"rule_weight".equals(ruleModel)) return null;
        String[] ruleValueGroups = ruleValue.split(Constants.SPACE);
        Map<String, List<Integer>> resultMap = Arrays.stream(ruleValueGroups)
                .filter(StringUtils::isNotEmpty) // 检查是否为空
                .collect(Collectors.toMap(
                        ruleValueGroup -> ruleValueGroup, // key
                        ruleValueGroup -> { // value
                            String[] parts = ruleValueGroup.split(Constants.COLON);
                            if (parts.length != 2) {
                                throw new IllegalArgumentException("rule_weight rule_rule invalid input format" + ruleValueGroup);
                            }
                            // 解析值
                            String[] valueStrings = parts[1].split(Constants.SPLIT);
                            return Arrays.stream(valueStrings)
                                    .map(Integer::parseInt)
                                    .collect(Collectors.toList());
                        },
                        (oldValue, newValue) -> oldValue,
                        HashMap::new
                ));
        return resultMap;
    }
}
