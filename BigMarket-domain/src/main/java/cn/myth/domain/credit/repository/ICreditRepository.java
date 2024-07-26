package cn.myth.domain.credit.repository;

import cn.myth.domain.credit.model.aggregate.TradeAggregate;

/**
 * 用户积分仓储
 */
public interface ICreditRepository {

    void saveUserCreditTradeOrder(TradeAggregate tradeAggregate);

}
