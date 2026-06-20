package com.hhjt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hhjt.entity.Announcement;

/**
 * 公告服务接口
 */
public interface AnnouncementService {
    
    /**
     * 分页查询公告列表
     */
    IPage<Announcement> getAnnouncementPage(Integer page, Integer size, String type, Integer status);
    
    /**
     * 根据ID查询公告
     */
    Announcement getAnnouncementById(Long id);
    
    /**
     * 发布公告
     */
    boolean publishAnnouncement(Announcement announcement, Long publisherId);
    
    /**
     * 更新公告
     */
    boolean updateAnnouncement(Announcement announcement);
    
    /**
     * 下架公告
     */
    boolean offlineAnnouncement(Long id);
    
    /**
     * 删除公告
     */
    boolean deleteAnnouncement(Long id);
    
    /**
     * 增加浏览次数
     */
    void increaseViewCount(Long id);
    
    /**
     * 获取首页公告列表（已发布且在有效期内）
     */
    IPage<Announcement> getActiveAnnouncements(Integer page, Integer size);
}
