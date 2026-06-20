package com.hhjt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhjt.entity.SysMessage;
import com.hhjt.mapper.SysMessageMapper;
import com.hhjt.service.SysMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息通知服务实现类
 */
@Slf4j
@Service
public class SysMessageServiceImpl implements SysMessageService {

    @Autowired
    private SysMessageMapper messageMapper;

    @Override
    public IPage<SysMessage> getMessagePage(Integer page, Integer size, Long userId, String type, Boolean isRead) {
        Page<SysMessage> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<SysMessage> wrapper = new LambdaQueryWrapper<>();
        
        // 查询用户作为接收者或发送者的消息
        wrapper.and(w -> w.eq(SysMessage::getReceiverId, userId).or().eq(SysMessage::getSenderId, userId));
        
        if (type != null && !type.isEmpty()) {
            wrapper.eq(SysMessage::getMessageType, type);
        }
        
        if (isRead != null) {
            wrapper.eq(SysMessage::getIsRead, isRead ? 1 : 0);
        }
        
        wrapper.orderByDesc(SysMessage::getCreateTime);
        
        return messageMapper.selectPage(pageInfo, wrapper);
    }

    @Override
    public IPage<SysMessage> getChatPage(Integer page, Integer size, Long userId, Long contactId) {
        Page<SysMessage> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<SysMessage> wrapper = new LambdaQueryWrapper<>();
        
        // 查询用户与联系人之间的消息
        // 条件：(用户发送给联系人) 或 (联系人发送给用户)
        wrapper.and(w -> w.eq(SysMessage::getSenderId, userId).eq(SysMessage::getReceiverId, contactId));
        wrapper.or(w -> w.eq(SysMessage::getSenderId, contactId).eq(SysMessage::getReceiverId, userId));
        
        wrapper.orderByAsc(SysMessage::getCreateTime);
        
        return messageMapper.selectPage(pageInfo, wrapper);
    }

    @Override
    public SysMessage getMessageById(Long id) {
        return messageMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean sendMessage(SysMessage message) {
        message.setIsRead(0); // 未读
        message.setCreateTime(LocalDateTime.now());
        int rows = messageMapper.insert(message);
        log.info("发送消息成功：接收者ID={}, 类型={}", message.getReceiverId(), message.getMessageType());
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAsRead(Long messageId) {
        SysMessage message = messageMapper.selectById(messageId);
        if (message == null) {
            return false;
        }
        
        message.setIsRead(1);
        message.setReadTime(LocalDateTime.now());
        
        int rows = messageMapper.updateById(message);
        log.info("标记消息为已读：ID={}", messageId);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAllAsRead(Long userId) {
        LambdaQueryWrapper<SysMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMessage::getReceiverId, userId)
               .eq(SysMessage::getIsRead, 0);
        
        List<SysMessage> messages = messageMapper.selectList(wrapper);
        for (SysMessage message : messages) {
            message.setIsRead(1);
            message.setReadTime(LocalDateTime.now());
            messageMapper.updateById(message);
        }
        
        log.info("批量标记为已读：用户ID={}, 数量={}", userId, messages.size());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMessage(Long messageId, Long userId) {
        SysMessage message = messageMapper.selectById(messageId);
        if (message == null || !message.getReceiverId().equals(userId)) {
            return false;
        }
        
        int rows = messageMapper.deleteById(messageId);
        log.info("删除消息：ID={}", messageId);
        return rows > 0;
    }

    @Override
    public long getUnreadCount(Long userId) {
        LambdaQueryWrapper<SysMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMessage::getReceiverId, userId)
               .eq(SysMessage::getIsRead, 0);
        return messageMapper.selectCount(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendSystemAnnouncement(String title, String content, List<Long> targetUserIds) {
        for (Long userId : targetUserIds) {
            SysMessage message = new SysMessage();
            message.setReceiverId(userId);
            message.setMessageType("SYSTEM");
            message.setTitle(title);
            message.setContent(content);
            message.setIsRead(0);
            message.setCreateTime(LocalDateTime.now());
            
            messageMapper.insert(message);
        }
        
        log.info("发送系统公告：标题={}, 接收者数量={}", title, targetUserIds.size());
    }
}