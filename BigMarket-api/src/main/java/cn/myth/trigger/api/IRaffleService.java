package cn.myth.trigger.api;

import cn.myth.trigger.api.dto.RaffleAwardListRequestDTO;
import cn.myth.trigger.api.dto.RaffleAwardListResponseDTO;
import cn.myth.trigger.api.dto.RaffleRequestDTO;
import cn.myth.trigger.api.dto.RaffleResponseDTO;
import cn.myth.types.model.Response;

import java.util.List;

/**
 * 抽奖服务接口
 */
public interface IRaffleService {

    /**
     * 策略装配接口
     *
     * @param strategyId 策略ID
     * @return 装配结果
     */
    Response<Boolean> strategyArmory(Long strategyId);

    /**
     * 查询抽奖奖品列表配置
     *
     * @param requestDTO 抽奖奖品列表查询请求参数
     * @return 奖品列表数据
     */
    Response<List<RaffleAwardListResponseDTO>> queryRaffleAwardList(RaffleAwardListRequestDTO requestDTO);

    /**
     * 随机抽奖接口
     *
     * @param requestDTO 请求参数
     * @return 抽奖结果
     */
    Response<RaffleResponseDTO> randomRaffle(RaffleRequestDTO requestDTO);
}
