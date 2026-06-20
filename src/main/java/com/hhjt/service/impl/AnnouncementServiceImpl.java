package com.hhjt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhjt.entity.Announcement;
import com.hhjt.mapper.AnnouncementMapper;
import com.hhjt.service.AnnouncementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 公告服务实现类
 */
@Slf4j
@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    @Autowired
    private AnnouncementMapper announcementMapper;

    @Override
    public IPage<Announcement> getAnnouncementPage(Integer page, Integer size, String type, Integer status) {
        Page<Announcement> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        
        // 筛选条件
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Announcement::getType, type);
        }
        if (status != null) {
            wrapper.eq(Announcement::getStatus, status);
        }
        
        wrapper.orderByDesc(Announcement::getPriority)
               .orderByDesc(Announcement::getCreateTime);
        
        return announcementMapper.selectPage(pageInfo, wrapper);
    }

    @Override
    public Announcement getAnnouncementById(Long id) {
        return announcementMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishAnnouncement(Announcement announcement, Long publisherId) {
        announcement.setPublisherId(publisherId);
        announcement.setStatus(1); // 已发布
        announcement.setViewCount(0);
        announcement.setCreateTime(LocalDateTime.now());
        announcement.setUpdateTime(LocalDateTime.now());
        
        int rows = announcementMapper.insert(announcement);
        log.info("发布公告: {}, 发布人ID: {}", announcement.getTitle(), publisherId);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAnnouncement(Announcement announcement) {
        announcement.setUpdateTime(LocalDateTime.now());
        int rows = announcementMapper.updateById(announcement);
        log.info("更新公告: ID={}", announcement.getId());
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean offlineAnnouncement(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new RuntimeException("公告不存在");
        }
        
        announcement.setStatus(2); // 已下架
        announcement.setUpdateTime(LocalDateTime.now());
        
        int rows = announcementMapper.updateById(announcement);
        log.info("下架公告: ID={}", id);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAnnouncement(Long id) {
        int rows = announcementMapper.deleteById(id);
        log.info("删除公告: ID={}", id);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void increaseViewCount(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement != null) {
            announcement.setViewCount(announcement.getViewCount() + 1);
            announcementMapper.updateById(announcement);
        }
    }

    @Override
    public IPage<Announcement> getActiveAnnouncements(Integer page, Integer size) {
        Page<Announcement> pageInfo = new Page<>(page, size);
        LocalDateTime now = LocalDateTime.now();
        
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getStatus, 1) // 已发布
               .and(w -> w.isNull(Announcement::getStartTime)
                         .or()
                         .le(Announcement::getStartTime, now))
               .and(w -> w.isNull(Announcement::getEndTime)
                         .or()
                         .ge(Announcement::getEndTime, now))
               .orderByDesc(Announcement::getPriority)
               .orderByDesc(Announcement::getCreateTime);
        
        return announcementMapper.selectPage(pageInfo, wrapper);
    }
}
