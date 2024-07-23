package cn.myth.domain.rebate.repository;

import cn.myth.domain.rebate.model.aggregate.BehaviorRebateAggregate;
import cn.myth.domain.rebate.model.entity.BehaviorRebateOrderEntity;
import cn.myth.domain.rebate.model.vo.BehaviorTypeVO;
import cn.myth.domain.rebate.model.vo.DailyBehaviorRebateVO;

import java.util.List;

/**
 * 行为返利服务仓储接口
 */
public interface IBehaviorRebateRepository {

    List<DailyBehaviorRebateVO> queryDailyBehaviorRebateConfig(BehaviorTypeVO behaviorTypeVO);

    void saveUserRebateRecord(String userId, List<BehaviorRebateAggregate> behaviorRebateAggregates);

    List<BehaviorRebateOrderEntity> queryOrderByOutBusinessNo(String userId, String outBusinessNo);
}
