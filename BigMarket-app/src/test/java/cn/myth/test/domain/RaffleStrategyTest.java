package cn.myth.test.domain;

import cn.myth.domain.strategy.model.entity.RaffleAwardEntity;
import cn.myth.domain.strategy.model.entity.RaffleFactorEntity;
import cn.myth.domain.strategy.service.IRaffleStrategy;
import cn.myth.domain.strategy.service.rule.impl.RuleWeightLogicFilter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.annotation.Resource;

/**
 * 抽奖策略测试
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RaffleStrategyTest {

    @Resource
    private IRaffleStrategy raffleStrategy;

    @Resource
    private RuleWeightLogicFilter ruleWeightLogicFilter;

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(ruleWeightLogicFilter, "userScore", 40500L);
    }

    @Test
    public void test_performRaffle() {
        RaffleFactorEntity raffleFactorEntity = new RaffleFactorEntity()
                .userId("myth")
                .strategyId(100001L);

        RaffleAwardEntity raffleAwardEntity = raffleStrategy.performRaffle(raffleFactorEntity);

        log.info("请求参数：{}", raffleFactorEntity);
        log.info("测试结果：{}", raffleAwardEntity);
    }


    @Test
    public void test_performRaffle_blacklist() {
        RaffleFactorEntity raffleFactorEntity = new RaffleFactorEntity()
                .userId("user003")  // 黑名单用户 user001,user002,user003
                .strategyId(100001L);

        RaffleAwardEntity raffleAwardEntity = raffleStrategy.performRaffle(raffleFactorEntity);

        log.info("请求参数：{}", raffleFactorEntity);
        log.info("测试结果：{}", raffleAwardEntity);
    }

}
