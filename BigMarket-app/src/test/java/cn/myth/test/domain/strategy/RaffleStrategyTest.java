package cn.myth.test.domain.strategy;

import cn.myth.domain.strategy.model.entity.RaffleAwardEntity;
import cn.myth.domain.strategy.model.entity.RaffleFactorEntity;
import cn.myth.domain.strategy.model.vo.RuleWeightVO;
import cn.myth.domain.strategy.service.IRaffleRule;
import cn.myth.domain.strategy.service.IRaffleStock;
import cn.myth.domain.strategy.service.IRaffleStrategy;
import cn.myth.domain.strategy.service.armory.IStrategyArmory;
import cn.myth.domain.strategy.service.rule.chain.impl.RuleWeightLogicChain;
import cn.myth.domain.strategy.service.rule.tree.impl.RuleLockLogicTreeNode;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CountDownLatch;

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
    private RuleWeightLogicChain ruleWeightLogicChain;
    @Resource
    private RuleLockLogicTreeNode ruleLockLogicTreeNode;
    @Resource
    private IRaffleStock raffleStock;
    @Resource
    private IRaffleRule raffleRule;

    @Before
    public void setUp() {
        // 策略装配 100001、100002、100003
        log.info("测试结果：{}", strategyArmory.assembleLotteryStrategy(100001L));
//        log.info("测试结果：{}", strategyArmory.assembleLotteryStrategy(100002L));
//        log.info("测试结果：{}", strategyArmory.assembleLotteryStrategy(100003L));
        log.info("测试结果：{}", strategyArmory.assembleLotteryStrategy(100006L));

        // 通过反射 mock 规则中的值
        // 通过反射 mock 规则中的值
//        ReflectionTestUtils.setField(ruleWeightLogicChain, "userScore", 4900L);
//        ReflectionTestUtils.setField(ruleLockLogicTreeNode, "userRaffleCount", 10L);

    }

    @Test
    public void test_performRaffle() throws InterruptedException {
        for (int i = 0; i < 3; i++) {
            RaffleFactorEntity raffleFactorEntity = new RaffleFactorEntity()
                    .userId("myth")
                    .strategyId(100006L);

            RaffleAwardEntity raffleAwardEntity = raffleStrategy.performRaffle(raffleFactorEntity);

            log.info("请求参数：{}", raffleFactorEntity);
            log.info("测试结果：{}", raffleAwardEntity);
        }
        // 等待 UpdateAwardStockJob 消费队列
        new CountDownLatch(1).await();
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

    @Test
    public void test_raffleRule() {
        List<RuleWeightVO> ruleWeightVOS = raffleRule.queryAwardRuleWeightByActivityId(100301L);
        log.info("测试结果：{}", JSON.toJSONString(ruleWeightVOS));
    }

}
