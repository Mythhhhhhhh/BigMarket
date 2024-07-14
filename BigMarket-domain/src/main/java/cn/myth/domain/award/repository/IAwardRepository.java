package cn.myth.domain.award.repository;

import cn.myth.domain.award.model.aggregate.UserAwardRecordAggregate;

/**
 * 奖品仓储服务
 */
public interface IAwardRepository {

    void saveUserAwardRecord(UserAwardRecordAggregate userAwardRecordAggregate);

}
