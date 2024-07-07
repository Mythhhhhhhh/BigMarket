package cn.myth.domain.activity.service.rule;

import cn.myth.domain.activity.model.entity.ActivityCountEntity;
import cn.myth.domain.activity.model.entity.ActivityEntity;
import cn.myth.domain.activity.model.entity.ActivitySkuEntity;

/**
 * 下单规则过滤接口
 */
public interface IActionChain extends IActionChainArmory {

    boolean action(ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityCountEntity activityCountEntity);

}
