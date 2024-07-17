package cn.myth.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import cn.myth.infrastructure.persistent.po.RaffleActivityAccountDay;
import org.apache.ibatis.annotations.Mapper;

/**
 * 抽奖活动账户表-日次数
 */
@Mapper
public interface IRaffleActivityAccountDayDao {
    @DBRouter
    RaffleActivityAccountDay queryActivityAccountDayByUserId(RaffleActivityAccountDay raffleActivityAccountDayReq);

    int updateActivityAccountDaySubtractionQuota(RaffleActivityAccountDay raffleActivityAccountDay);

    void insertActivityAccountDay(RaffleActivityAccountDay raffleActivityAccountDay);

    @DBRouter
    Integer queryRaffleActivityAccountDayPartakeCount(RaffleActivityAccountDay raffleActivityAccountDay);
}
