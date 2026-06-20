package com.hhjt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hhjt.entity.AchievementBadge;
import com.hhjt.entity.StudentBadge;

/**
 * 成就徽章服务接口
 */
public interface AchievementBadgeService {
    
    /**
     * 分页查询徽章列表
     */
    IPage<AchievementBadge> getBadgePage(Integer page, Integer size);
    
    /**
     * 根据ID查询徽章
     */
    AchievementBadge getBadgeById(Long id);
    
    /**
     * 创建徽章
     */
    boolean createBadge(AchievementBadge badge);
    
    /**
     * 更新徽章
     */
    boolean updateBadge(AchievementBadge badge);
    
    /**
     * 删除徽章
     */
    boolean deleteBadge(Long id);
    
    /**
     * 检查并授予学生徽章
     * @param studentId 学生ID
     */
    void checkAndAwardBadges(Long studentId);
    
    /**
     * 获取学生已获得的徽章
     */
    IPage<StudentBadge> getStudentBadges(Integer page, Integer size, Long studentId);
    
    /**
     * 授予徽章给学生
     */
    boolean awardBadgeToStudent(Long studentId, Long badgeId);
    
    /**
     * 获取所有启用的徽章
     */
    java.util.List<AchievementBadge> getAllActiveBadges();
}
