package cn.myth.domain.activity.service;

import cn.myth.domain.activity.repository.IActivityRepository;
import org.springframework.stereotype.Service;

/**
 * 抽奖活动服务
 */
@Service
public class RaffleActivityService extends AbstractRaffleActivity {

    public RaffleActivityService(IActivityRepository activityRepository) {
        super(activityRepository);
    }

}
