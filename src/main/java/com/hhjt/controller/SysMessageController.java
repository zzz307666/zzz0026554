package com.hhjt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hhjt.entity.SysMessage;
import com.hhjt.service.SysMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息通知控制器
 */
@Slf4j
@Controller
@RequestMapping("/message")
public class SysMessageController {

    @Autowired
    private SysMessageService messageService;

    /**
     * 消息列表页面
     */
    @GetMapping("/list")
    public String messageList(@RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "20") Integer size,
                               @RequestParam(required = false) String type,
                               @RequestParam(required = false) Boolean isRead,
                               HttpSession session,
                               Model model) {
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return "redirect:/login";
        }
        
        String messageType = null;
        if (type != null && !type.isEmpty()) {
            messageType = type.toUpperCase();
        }
        
        IPage<SysMessage> messagePage = messageService.getMessagePage(page, size, userId, messageType, isRead);
        long unreadCount = messageService.getUnreadCount(userId);
        
        model.addAttribute("page", messagePage);
        model.addAttribute("unreadCount", unreadCount);
        
        return "student/messages";
    }

    /**
     * 获取未读消息数量（AJAX）
     */
    @GetMapping("/unread-count")
    @ResponseBody
    public Map<String, Object> getUnreadCount(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            result.put("success", false);
            result.put("count", 0);
            return result;
        }
        
        long count = messageService.getUnreadCount(userId);
        result.put("success", true);
        result.put("count", count);
        
        return result;
    }

    /**
     * 标记消息为已读
     */
    @PostMapping("/mark-read/{id}")
    @ResponseBody
    public Map<String, Object> markAsRead(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean success = messageService.markAsRead(id);
            result.put("success", success);
            result.put("message", success ? "操作成功" : "操作失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("标记已读失败", e);
        }
        
        return result;
    }

    /**
     * 全部标记为已读
     */
    @PostMapping("/mark-all-read")
    @ResponseBody
    public Map<String, Object> markAllAsRead(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            result.put("success", false);
            result.put("message", "用户未登录");
            return result;
        }
        
        try {
            boolean success = messageService.markAllAsRead(userId);
            result.put("success", success);
            result.put("message", "已全部标记为已读");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("批量标记已读失败", e);
        }
        
        return result;
    }

    /**
     * 删除消息
     */
    @PostMapping("/delete/{id}")
    @ResponseBody
    public Map<String, Object> deleteMessage(@PathVariable Long id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            result.put("success", false);
            result.put("message", "用户未登录");
            return result;
        }
        
        try {
            boolean success = messageService.deleteMessage(id, userId);
            result.put("success", success);
            result.put("message", success ? "删除成功" : "删除失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("删除消息失败", e);
        }
        
        return result;
    }

    /**
     * 查看消息详情
     */
    @GetMapping("/detail/{id}")
    @ResponseBody
    public SysMessage getMessageDetail(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        SysMessage message = messageService.getMessageById(id);
        
        // 验证权限
        if (message != null && message.getReceiverId().equals(userId)) {
            // 自动标记为已读
            if (message.getIsRead() == 0) {
                messageService.markAsRead(id);
            }
            return message;
        }
        
        return null;
    }
}