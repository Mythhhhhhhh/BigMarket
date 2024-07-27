package cn.myth.domain.activity.service.quota.policy.impl;

import cn.myth.domain.activity.model.aggregate.CreateQuotaOrderAggregate;
import cn.myth.domain.activity.model.vo.OrderStateVO;
import cn.myth.domain.activity.repository.IActivityRepository;
import cn.myth.domain.activity.service.quota.policy.ITradePolicy;
import org.springframework.stereotype.Service;

/**
 * 积分兑换，支付类订单
 */
@Service("credit_pay_trade")
public class CreditPayTradePolicy implements ITradePolicy {

    private final IActivityRepository activityRepository;

    public CreditPayTradePolicy(IActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Override
    public void trade(CreateQuotaOrderAggregate createQuotaOrderAggregate) {
        createQuotaOrderAggregate.setOrderState(OrderStateVO.wait_pay);
        activityRepository.doSaveCreditPayOrder(createQuotaOrderAggregate);
    }

}
