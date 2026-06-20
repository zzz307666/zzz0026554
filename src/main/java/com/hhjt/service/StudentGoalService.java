package com.hhjt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hhjt.entity.StudentGoal;

import java.math.BigDecimal;

/**
 * 学生目标服务接口
 */
public interface StudentGoalService {
    
    /**
     * 分页查询学生目标列表
     */
    IPage<StudentGoal> getGoalPage(Integer page, Integer size, Long studentId, Integer status);
    
    /**
     * 根据ID查询目标
     */
    StudentGoal getGoalById(Long id);
    
    /**
     * 创建目标
     */
    boolean createGoal(StudentGoal goal);
    
    /**
     * 更新目标
     */
    boolean updateGoal(StudentGoal goal);
    
    /**
     * 取消目标
     */
    boolean cancelGoal(Long id);
    
    /**
     * 删除目标
     */
    boolean deleteGoal(Long id);
    
    /**
     * 更新目标进度
     */
    boolean updateProgress(Long id, BigDecimal currentValue);
    
    /**
     * 检查并自动完成目标
     */
    void checkAndCompleteGoals();
    
    /**
     * 获取学生活跃目标数量
     */
    long getActiveGoalCount(Long studentId);
    
    /**
     * 根据状态获取目标数量
     */
    long getGoalCountByStatus(Long studentId, Integer status);
}