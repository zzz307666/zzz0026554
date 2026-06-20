package com.hhjt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhjt.entity.PointsExchange;
import com.hhjt.entity.PointsGift;
import com.hhjt.entity.Student;
import com.hhjt.entity.User;
import com.hhjt.mapper.*;
import com.hhjt.service.PointsExchangeService;
import com.hhjt.service.PointsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 积分兑换服务实现
 */
@Slf4j
@Service
public class PointsExchangeServiceImpl implements PointsExchangeService {
    
    @Autowired
    private PointsGiftMapper pointsGiftMapper;
    
    @Autowired
    private PointsExchangeMapper pointsExchangeMapper;
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PointsService pointsService;
    
    @Override
    public List<PointsGift> getAvailableGifts() {
        return pointsGiftMapper.selectList(
            new LambdaQueryWrapper<PointsGift>()
                .eq(PointsGift::getStatus, 1) // 只查询上架的
                .gt(PointsGift::getStock, 0)  // 库存大于0
                .orderByAsc(PointsGift::getSortOrder)
                .orderByAsc(PointsGift::getPointsCost)
        );
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean exchangeGift(Long studentId, Long giftId) {
        // 查询奖品信息
        PointsGift gift = pointsGiftMapper.selectById(giftId);
        if (gift == null || gift.getStatus() != 1) {
            throw new RuntimeException("奖品不存在或已下架");
        }
        
        // 检查库存
        if (gift.getStock() <= 0) {
            throw new RuntimeException("奖品库存不足");
        }
        
        // 查询学生当前积分
        BigDecimal currentPoints = pointsService.getStudentTotalPoints(studentId);
        if (currentPoints.compareTo(new BigDecimal(gift.getPointsCost())) < 0) {
            throw new RuntimeException("积分不足，当前积分：" + currentPoints + "，需要积分：" + gift.getPointsCost());
        }
        
        // 扣除积分
        pointsService.addPoints(studentId, "EXCHANGE_GIFT", 
            new BigDecimal(-gift.getPointsCost()), 
            "兑换奖品：" + gift.getGiftName(), 
            giftId);
        
        // 减少库存
        gift.setStock(gift.getStock() - 1);
        pointsGiftMapper.updateById(gift);
        
        // 创建兑换记录
        PointsExchange exchange = new PointsExchange();
        exchange.setStudentId(studentId);
        exchange.setGiftId(giftId);
        exchange.setGiftName(gift.getGiftName());
        exchange.setPointsCost(gift.getPointsCost());
        exchange.setExchangeTime(LocalDateTime.now());
        exchange.setStatus(0); // 待领取
        
        return pointsExchangeMapper.insert(exchange) > 0;
    }
    
    @Override
    public IPage<PointsExchange> getStudentExchanges(Integer page, Integer size, Long studentId) {
        Page<PointsExchange> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<PointsExchange> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsExchange::getStudentId, studentId)
               .orderByDesc(PointsExchange::getExchangeTime);
        
        return pointsExchangeMapper.selectPage(pageInfo, wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmReceive(Long exchangeId) {
        PointsExchange exchange = pointsExchangeMapper.selectById(exchangeId);
        if (exchange == null) {
            throw new RuntimeException("兑换记录不存在");
        }
        
        if (exchange.getStatus() != 0) {
            throw new RuntimeException("该兑换记录状态异常");
        }
        
        exchange.setStatus(1); // 已领取
        exchange.setReceiveTime(LocalDateTime.now());
        
        return pointsExchangeMapper.updateById(exchange) > 0;
    }
    
    @Override
    public IPage<PointsExchange> getAllExchanges(Integer page, Integer size, Integer status) {
        Page<PointsExchange> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<PointsExchange> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null) {
            wrapper.eq(PointsExchange::getStatus, status);
        }
        wrapper.orderByDesc(PointsExchange::getExchangeTime);
        
        IPage<PointsExchange> result = pointsExchangeMapper.selectPage(pageInfo, wrapper);
        
        // 填充学生信息
        if (result.getRecords() != null) {
            for (PointsExchange exchange : result.getRecords()) {
                if (exchange.getStudentId() != null) {
                    Student student = studentMapper.selectById(exchange.getStudentId());
                    if (student != null) {
                        exchange.setStudentNo(student.getStudentNo());
                        User user = userMapper.selectById(student.getUserId());
                        if (user != null) {
                            exchange.setStudentName(user.getRealName());
                        }
                    }
                }
            }
        }
        
        return result;
    }
}
