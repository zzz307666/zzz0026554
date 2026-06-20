package com.hhjt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hhjt.entity.StudentPoints;

import java.math.BigDecimal;

/**
 * 学生积分服务接口
 */
public interface PointsService {
    
    /**
     * 增加积分
     */
    void addPoints(Long studentId, String pointsType, BigDecimal value, String description, Long relatedId);
    
    /**
     * 查询学生总积分
     */
    BigDecimal getStudentTotalPoints(Long studentId);
    
    /**
     * 查询积分明细（分页）
     */
    IPage<StudentPoints> getPointsPage(Integer page, Integer size, Long studentId, String pointsType);
    
    /**
     * 应用积分规则（打卡审核通过后自动调用）
     */
    void applyPointsRules(Long recordId);
}
