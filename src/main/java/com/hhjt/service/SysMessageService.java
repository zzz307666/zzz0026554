package com.hhjt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hhjt.entity.SysMessage;

/**
 * 消息通知服务接口
 */
public interface SysMessageService {
    
    /**
     * 分页查询消息列表
     */
    IPage<SysMessage> getMessagePage(Integer page, Integer size, Long userId, String type, Boolean isRead);
    
    /**
     * 分页查询与特定联系人的聊天消息
     */
    IPage<SysMessage> getChatPage(Integer page, Integer size, Long userId, Long contactId);
    
    /**
     * 根据ID查询消息
     */
    SysMessage getMessageById(Long id);
    
    /**
     * 发送消息
     */
    boolean sendMessage(SysMessage message);
    
    /**
     * 标记消息为已读
     */
    boolean markAsRead(Long messageId);
    
    /**
     * 批量标记为已读
     */
    boolean markAllAsRead(Long userId);
    
    /**
     * 删除消息
     */
    boolean deleteMessage(Long messageId, Long userId);
    
    /**
     * 获取未读消息数量
     */
    long getUnreadCount(Long userId);
    
    /**
     * 发送系统公告消息（批量）
     */
    void sendSystemAnnouncement(String title, String content, java.util.List<Long> targetUserIds);
}