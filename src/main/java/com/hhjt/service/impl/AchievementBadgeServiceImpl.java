package com.hhjt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhjt.entity.AchievementBadge;
import com.hhjt.entity.StudentBadge;
import com.hhjt.mapper.AchievementBadgeMapper;
import com.hhjt.mapper.StudentBadgeMapper;
import com.hhjt.service.AchievementBadgeService;
import com.hhjt.service.PointsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 成就徽章服务实现类
 */
@Slf4j
@Service
public class AchievementBadgeServiceImpl implements AchievementBadgeService {

    @Autowired
    private AchievementBadgeMapper badgeMapper;

    @Autowired
    private StudentBadgeMapper studentBadgeMapper;

    @Autowired
    private PointsService pointsService;

    @Override
    public IPage<AchievementBadge> getBadgePage(Integer page, Integer size) {
        Page<AchievementBadge> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<AchievementBadge> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(AchievementBadge::getBadgeLevel)
               .orderByAsc(AchievementBadge::getSortOrder);
        return badgeMapper.selectPage(pageInfo, wrapper);
    }

    @Override
    public AchievementBadge getBadgeById(Long id) {
        return badgeMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createBadge(AchievementBadge badge) {
        badge.setCreateTime(LocalDateTime.now());
        int rows = badgeMapper.insert(badge);
        log.info("创建徽章成功：{}", badge.getBadgeName());
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateBadge(AchievementBadge badge) {
        int rows = badgeMapper.updateById(badge);
        log.info("更新徽章成功：ID={}", badge.getId());
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBadge(Long id) {
        int rows = badgeMapper.deleteById(id);
        log.info("删除徽章成功：ID={}", id);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkAndAwardBadges(Long studentId) {
        // 获取所有启用的徽章
        List<AchievementBadge> badges = getAllActiveBadges();
        
        for (AchievementBadge badge : badges) {
            // 检查学生是否已获得该徽章
            LambdaQueryWrapper<StudentBadge> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(StudentBadge::getStudentId, studentId)
                   .eq(StudentBadge::getBadgeId, badge.getId());
            
            long count = studentBadgeMapper.selectCount(wrapper);
            if (count > 0) {
                continue; // 已获得，跳过
            }
            
            // 检查是否满足条件（这里简化处理，实际应该根据badge.getCondition()解析JSON并检查）
            boolean meetsCondition = checkBadgeCondition(studentId, badge);
            
            if (meetsCondition) {
                // 授予徽章
                awardBadgeToStudent(studentId, badge.getId());
                log.info("学生{}获得徽章：{}", studentId, badge.getBadgeName());
            }
        }
    }

    @Override
    public IPage<StudentBadge> getStudentBadges(Integer page, Integer size, Long studentId) {
        Page<StudentBadge> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<StudentBadge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentBadge::getStudentId, studentId)
               .orderByDesc(StudentBadge::getAchievedTime);
        return studentBadgeMapper.selectPage(pageInfo, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean awardBadgeToStudent(Long studentId, Long badgeId) {
        // 检查是否已获得
        LambdaQueryWrapper<StudentBadge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentBadge::getStudentId, studentId)
               .eq(StudentBadge::getBadgeId, badgeId);
        
        long count = studentBadgeMapper.selectCount(wrapper);
        if (count > 0) {
            log.warn("学生{}已获得徽章{}", studentId, badgeId);
            return false;
        }
        
        // 创建记录
        StudentBadge studentBadge = new StudentBadge();
        studentBadge.setStudentId(studentId);
        studentBadge.setBadgeId(badgeId);
        studentBadge.setAchievedTime(LocalDateTime.now());
        studentBadge.setIsDisplayed(1);
        
        int rows = studentBadgeMapper.insert(studentBadge);
        
        // 发放奖励积分
        AchievementBadge badge = badgeMapper.selectById(badgeId);
        if (badge != null && badge.getRewardPoints() != null && badge.getRewardPoints().compareTo(BigDecimal.ZERO) > 0) {
            pointsService.addPoints(
                studentId,
                "BADGE",
                badge.getRewardPoints(),
                "获得成就徽章：" + badge.getBadgeName(),
                badgeId
            );
        }
        
        log.info("授予徽章成功：学生ID={}, 徽章ID={}", studentId, badgeId);
        return rows > 0;
    }

    @Override
    public List<AchievementBadge> getAllActiveBadges() {
        LambdaQueryWrapper<AchievementBadge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AchievementBadge::getIsActive, 1)
               .orderByDesc(AchievementBadge::getBadgeLevel)
               .orderByAsc(AchievementBadge::getSortOrder);
        return badgeMapper.selectList(wrapper);
    }

    /**
     * 检查徽章达成条件（简化版本）
     * TODO: 实际应该解析JSON条件并进行复杂判断
     */
    private boolean checkBadgeCondition(Long studentId, AchievementBadge badge) {
        // 这里简化处理，返回false表示需要手动授予
        // 实际应该根据badge.getCondition()中的JSON配置进行检查
        // 例如：{"type": "sport_count", "value": 100} 表示运动次数达到100次
        return false;
    }
}
