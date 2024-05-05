package cn.myth.test.domain;

import cn.myth.domain.strategy.service.armory.IStrategyArmory;
import cn.myth.domain.strategy.service.armory.IStrategyDispatch;
import cn.myth.infrastructure.persistent.redis.IRedisService;
import cn.myth.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RMap;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 策略领域测试
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class StrategyTest {

    @Resource
    private IStrategyArmory strategyArmory;

    @Resource
    private IStrategyDispatch strategyDispatch;

    /**
     * 策略ID；100001L、100002L 装配的时候创建策略表写入到 Redis Map 中
     */
    @Before
    public void test_strategyArmory() {
        boolean success = strategyArmory.assembleLotteryStrategy(100001L);
        log.info("测试结果：{}", success);
    }

    /**
     * 从装配的策略中随机获取奖品ID值
     */
    @Test
    public void test_getRandomAwardId() {
        log.info("测试结果：{} - 4000 策略配置", strategyDispatch.getRandomAwardId(100001L, "4000:102,103,104,105"));
        log.info("测试结果：{} - 5000 策略配置", strategyDispatch.getRandomAwardId(100001L, "5000:102,103,104,105,106,107"));
        log.info("测试结果：{} - 6000 策略配置", strategyDispatch.getRandomAwardId(100001L, "6000:102,103,104,105,106,107,108,109"));
    }


    /**
     * 从装配的策略中随机获取奖品ID值
     */
    @Test
    public void test_getAssembleRandomVal() {
        Map<Integer, Integer> cacheRateTable = redisService.getMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + 100002L);
        Map<Integer, Integer> countMap = new HashMap<>();
        cacheRateTable.forEach((k, v) -> {
            countMap.put(v, countMap.getOrDefault(v, 0) + 1);
        });

        countMap.forEach((k, v) -> {
            System.out.println(k + "->" + v);
        });
        /**
         * 101->31
         * 102->7
         * 106->1
         */


        for (int i = 0; i < 10; i++) {
            log.info("测试结果：{} - 奖品ID值", strategyDispatch.getRandomAwardId(100002L));
        }
    }






    @Resource
    private IRedisService redisService;


    @Test
    public void test_map() {
        RMap<Integer, Integer> map = redisService.getMap("strategy_id_100001");
        map.put(1, 101);
        map.put(2, 101);
        map.put(3, 101);
        map.put(4, 102);
        map.put(5, 102);
        map.put(6, 102);
        map.put(7, 103);
        map.put(8, 103);
        map.put(9, 104);
        map.put(10, 105);

        log.info("测试结果：{}", redisService.getMap("strategy_id_100001").get(1));
    }

    @Test
    public void test_shuffle(){
        Map<Integer, Integer> strategyAwardSearchRateTable = new HashMap<>();
        // 添加内容到Map中
        strategyAwardSearchRateTable.put(1, 10);
        strategyAwardSearchRateTable.put(2, 20);
        strategyAwardSearchRateTable.put(3, 30);
        strategyAwardSearchRateTable.put(4, 40);

        // 将Map中的值转换为List
        List<Integer> valueList = new ArrayList<>(strategyAwardSearchRateTable.values());

        // 使用Collections.shuffle()方法对值的List进行乱序
        Collections.shuffle(valueList);

        // 将乱序后的值重新放回Map中
        Map<Integer, Integer> randomizedMap = new LinkedHashMap<>();
        Iterator<Integer> valueIterator = valueList.iterator();
        for (Integer key : strategyAwardSearchRateTable.keySet()) {
            randomizedMap.put(key, valueIterator.next());
        }

        // 打印乱序后的Map内容
        for (Map.Entry<Integer, Integer> entry : randomizedMap.entrySet()) {
            System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }
        System.out.println("--------------------------------");

        Map<Integer, Integer> shuffleStrategyAwardSearchRateTable = IntStream.range(0, valueList.size())
                .boxed()
                .collect(Collectors.toMap(
                        i -> i,// 键就是索引本身
                        valueList::get,// 值是对应索引的奖品ID
                        (oldValue, newValue) -> oldValue,// 如果有冲突（这种情况不会发生，因为我们用的是索引），保留旧值
                        LinkedHashMap::new
                ));
        for (Map.Entry<Integer, Integer> entry : shuffleStrategyAwardSearchRateTable.entrySet()) {
            System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }
    }

}
