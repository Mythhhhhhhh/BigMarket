package cn.myth.domain.activity.repository;

import cn.myth.domain.activity.model.aggregate.CreateOrderAggregate;
import cn.myth.domain.activity.model.entity.ActivityCountEntity;
import cn.myth.domain.activity.model.entity.ActivityEntity;
import cn.myth.domain.activity.model.entity.ActivitySkuEntity;
import cn.myth.domain.activity.model.vo.ActivitySkuStockKeyVO;

import java.util.Date;

/**
 * 活动仓储接口
 */
public interface IActivityRepository {

    ActivitySkuEntity queryActivitySku(Long sku);

    ActivityEntity queryRaffleActivityByActivityId(Long activityId);

    ActivityCountEntity queryRaffleActivityCountByActivityCountId(Long activityCountId);

    void doSaveOrder(CreateOrderAggregate createOrderAggregate);

    void cacheActivitySkuStockCount(String cacheKey, Integer stockCount);

    boolean subtractionActivitySkuStock(Long sku, String cacheKey, Date endDateTime);

    void activitySkuStockConsumeSendQueue(ActivitySkuStockKeyVO activitySkuStockKeyVO);

    ActivitySkuStockKeyVO takeQueueValue();

    void clearQueueValue();

    void updateActivitySkuStock(Long sku);

    void clearActivitySkuStock(Long sku);
}
