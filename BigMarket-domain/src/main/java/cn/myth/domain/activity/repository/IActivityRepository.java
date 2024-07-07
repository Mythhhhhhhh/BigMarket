package cn.myth.domain.activity.repository;

import cn.myth.domain.activity.model.entity.ActivityCountEntity;
import cn.myth.domain.activity.model.entity.ActivityEntity;
import cn.myth.domain.activity.model.entity.ActivitySkuEntity;

/**
 * 活动仓储接口
 */
public interface IActivityRepository {

    ActivitySkuEntity queryActivitySku(Long sku);

    ActivityEntity queryRaffleActivityByActivityId(Long activityId);

    ActivityCountEntity queryRaffleActivityCountByActivityCountId(Long activityCountId);

}
