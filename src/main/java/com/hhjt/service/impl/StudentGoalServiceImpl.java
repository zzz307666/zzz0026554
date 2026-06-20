package com.hhjt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhjt.entity.StudentGoal;
import com.hhjt.mapper.StudentGoalMapper;
import com.hhjt.service.PointsService;
import com.hhjt.service.StudentGoalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学生目标服务实现类
 */
@Slf4j
@Service
public class StudentGoalServiceImpl implements StudentGoalService {

    @Autowired
    private StudentGoalMapper goalMapper;

    @Autowired
    private PointsService pointsService;

    @Override
    public IPage<StudentGoal> getGoalPage(Integer page, Integer size, Long studentId, Integer status) {
        Page<StudentGoal> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<StudentGoal> wrapper = new LambdaQueryWrapper<>();
        
        if (studentId != null) {
            wrapper.eq(StudentGoal::getStudentId, studentId);
        }
        if (status != null) {
            wrapper.eq(StudentGoal::getStatus, status);
        }
        
        wrapper.orderByDesc(StudentGoal::getCreateTime);
        
        return goalMapper.selectPage(pageInfo, wrapper);
    }

    @Override
    public StudentGoal getGoalById(Long id) {
        return goalMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createGoal(StudentGoal goal) {
        // 检查活跃目标数量（最多3个）
        long activeCount = getActiveGoalCount(goal.getStudentId());
        if (activeCount >= 3) {
            throw new RuntimeException("最多只能同时存在3个活跃目标");
        }
        
        goal.setStatus(1); // 进行中
        goal.setCurrentValue(BigDecimal.ZERO);
        goal.setCreateTime(LocalDateTime.now());
        goal.setUpdateTime(LocalDateTime.now());
        
        int rows = goalMapper.insert(goal);
        log.info("创建目标成功：学生ID={}, 目标类型={}", goal.getStudentId(), goal.getGoalType());
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateGoal(StudentGoal goal) {
        goal.setUpdateTime(LocalDateTime.now());
        int rows = goalMapper.updateById(goal);
        log.info("更新目标成功：ID={}", goal.getId());
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelGoal(Long id) {
        StudentGoal goal = goalMapper.selectById(id);
        if (goal == null) {
            throw new RuntimeException("目标不存在");
        }
        
        goal.setStatus(0); // 已取消
        goal.setUpdateTime(LocalDateTime.now());
        
        int rows = goalMapper.updateById(goal);
        log.info("取消目标成功：ID={}", id);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteGoal(Long id) {
        int rows = goalMapper.deleteById(id);
        log.info("删除目标成功：ID={}", id);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateProgress(Long id, BigDecimal currentValue) {
        StudentGoal goal = goalMapper.selectById(id);
        if (goal == null) {
            throw new RuntimeException("目标不存在");
        }
        
        goal.setCurrentValue(currentValue);
        goal.setUpdateTime(LocalDateTime.now());
        
        // 检查是否达成目标
        if (currentValue.compareTo(goal.getTargetValue()) >= 0 && goal.getStatus() == 1) {
            goal.setStatus(2); // 已完成
            goal.setCompletedTime(LocalDateTime.now());
            
            // 发放奖励积分
            if (goal.getRewardPoints() != null && goal.getRewardPoints().compareTo(BigDecimal.ZERO) > 0) {
                pointsService.addPoints(
                    goal.getStudentId(),
                    "BONUS",
                    goal.getRewardPoints(),
                    "达成运动目标：" + goal.getGoalType(),
                    goal.getId()
                );
            }
        }
        
        int rows = goalMapper.updateById(goal);
        log.info("更新目标进度：ID={}, 当前值={}", id, currentValue);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkAndCompleteGoals() {
        // TODO: 定时任务检查所有进行中的目标
        // 这里可以查询数据库，根据实际运动记录自动更新进度
        log.info("检查并自动完成目标");
    }

    @Override
    public long getActiveGoalCount(Long studentId) {
        LambdaQueryWrapper<StudentGoal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentGoal::getStudentId, studentId)
               .eq(StudentGoal::getStatus, 1); // 进行中
        return goalMapper.selectCount(wrapper);
    }

    @Override
    public long getGoalCountByStatus(Long studentId, Integer status) {
        LambdaQueryWrapper<StudentGoal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentGoal::getStudentId, studentId)
               .eq(StudentGoal::getStatus, status);
        return goalMapper.selectCount(wrapper);
    }
}