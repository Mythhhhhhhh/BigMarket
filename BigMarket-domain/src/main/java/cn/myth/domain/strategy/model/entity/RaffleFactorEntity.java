package cn.myth.domain.strategy.model.entity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 抽奖因子实体
 */
@Data
@Accessors(chain = true, fluent = true)
public class RaffleFactorEntity {

    /** 用户ID */
    private String userId;
    /** 策略ID */
    private Long strategyId;

}
