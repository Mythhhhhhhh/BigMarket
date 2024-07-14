package cn.myth.domain.activity.service.quota;

import cn.myth.domain.activity.model.entity.ActivityCountEntity;
import cn.myth.domain.activity.model.entity.ActivityEntity;
import cn.myth.domain.activity.model.entity.ActivitySkuEntity;
import cn.myth.domain.activity.repository.IActivityRepository;
import cn.myth.domain.activity.service.quota.rule.factory.DefaultActivityChainFactory;

/**
 * 抽奖活动的支撑类
 */
public class RaffleActivityAccountQuotaSupport {

    protected DefaultActivityChainFactory defaultActivityChainFactory;

    protected IActivityRepository activityRepository;

    public RaffleActivityAccountQuotaSupport(IActivityRepository activityRepository, DefaultActivityChainFactory defaultActivityChainFactory) {
        this.activityRepository = activityRepository;
        this.defaultActivityChainFactory = defaultActivityChainFactory;
    }

    public ActivitySkuEntity queryActivitySku(Long sku) {
        return activityRepository.queryActivitySku(sku);
    }

    public ActivityEntity queryRaffleActivityByActivityId(Long activityId) {
        return activityRepository.queryRaffleActivityByActivityId(activityId);
    }

    public ActivityCountEntity queryRaffleActivityCountByActivityCountId(Long activityCountId) {
        return activityRepository.queryRaffleActivityCountByActivityCountId(activityCountId);
    }

}
