package cn.myth.test.domain;

import cn.myth.domain.strategy.model.entity.RaffleAwardEntity;
import cn.myth.domain.strategy.model.entity.RaffleFactorEntity;
import cn.myth.domain.strategy.service.IRaffleStrategy;
import cn.myth.domain.strategy.service.armory.IStrategyArmory;
import cn.myth.domain.strategy.service.rule.impl.RuleLockLogicFilter;
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
    private IStrategyArmory strategyArmory;
    @Resource
    private IRaffleStrategy raffleStrategy;
    @Resource
    private RuleWeightLogicFilter ruleWeightLogicFilter;
    @Resource
    private RuleLockLogicFilter ruleLockLogicFilter;

    @Before
    public void setUp() {
        // 策略装配 100001、100002、100003
        log.info("测试结果：{}", strategyArmory.assembleLotteryStrategy(100001L));
        log.info("测试结果：{}", strategyArmory.assembleLotteryStrategy(100002L));
        log.info("测试结果：{}", strategyArmory.assembleLotteryStrategy(100003L));

        // 通过反射 mock 规则中的值
        ReflectionTestUtils.setField(ruleWeightLogicFilter, "userScore", 40500L);
        ReflectionTestUtils.setField(ruleLockLogicFilter, "userRaffleCount", 0L);
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


    /**
     * 次数错校验，抽奖n次后解锁。100003 策略，你可以通过调整 @Before 的 setUp 方法中个人抽奖次数来验证。比如最开始设置0，之后设置10
     * ReflectionTestUtils.setField(ruleLockLogicFilter, "userRaffleCount", 10L);
     */
    @Test
    public void test_raffle_center_rule_lock(){
        RaffleFactorEntity raffleFactorEntity = new RaffleFactorEntity()
                .userId("myth")
                .strategyId(100003L);

        RaffleAwardEntity raffleAwardEntity = raffleStrategy.performRaffle(raffleFactorEntity);

        log.info("请求参数：{}", raffleFactorEntity);
        log.info("测试结果：{}", raffleAwardEntity);
    }

}
