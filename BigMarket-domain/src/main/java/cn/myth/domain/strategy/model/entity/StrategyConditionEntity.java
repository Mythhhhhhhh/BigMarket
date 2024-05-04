package cn.myth.domain.strategy.model.entity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 策略条件实体
 */
@Data
@Accessors(chain = true, fluent = true)
public class StrategyConditionEntity {

    /** 用户ID */
    private String userId;
    /** 策略ID */
    private Integer strategyId;

}
