package cn.myth.domain.strategy.model.entity;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class AwardEntity {
    /** 用户ID */
    private String userId;
    /** 奖品ID */
    private Integer awardId;

}
