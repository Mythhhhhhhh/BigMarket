package cn.myth.domain.activity.service.armory;

/**
 * 活动装配预热
 */
public interface IActivityArmory {

    boolean assembleActivitySku(Long sku);

    boolean assembleActivitySkuByActivityId(Long activityId);
}
