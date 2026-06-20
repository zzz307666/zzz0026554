package com.hhjt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hhjt.entity.PointsExchange;
import com.hhjt.entity.PointsGift;

import java.util.List;

/**
 * 积分兑换服务接口
 */
public interface PointsExchangeService {
    
    /**
     * 获取所有上架的奖品
     */
    List<PointsGift> getAvailableGifts();
    
    /**
     * 兑换奖品
     */
    boolean exchangeGift(Long studentId, Long giftId);
    
    /**
     * 查询学生的兑换记录
     */
    IPage<PointsExchange> getStudentExchanges(Integer page, Integer size, Long studentId);
    
    /**
     * 确认领取奖品
     */
    boolean confirmReceive(Long exchangeId);
    
    /**
     * 管理员查看所有兑换记录
     */
    IPage<PointsExchange> getAllExchanges(Integer page, Integer size, Integer status);
}
