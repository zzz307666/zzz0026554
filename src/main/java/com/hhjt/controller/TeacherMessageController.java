package com.hhjt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hhjt.entity.SysMessage;
import com.hhjt.entity.Teacher;
import com.hhjt.entity.Student;
import com.hhjt.entity.User;
import com.hhjt.mapper.TeacherMapper;
import com.hhjt.mapper.StudentMapper;
import com.hhjt.mapper.UserMapper;
import com.hhjt.service.SysMessageService;
import com.hhjt.service.TeacherClassService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 教师端 - 消息通知控制器
 */
@Slf4j
@Controller
@RequestMapping("/teacher/messages")
public class TeacherMessageController {

    @Autowired
    private SysMessageService messageService;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TeacherClassService teacherClassService;

    /**
     * 消息列表页面
     */
    @GetMapping
    public String messageList(@RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "20") Integer size,
                               HttpSession session,
                               Model model) {
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return "redirect:/login";
        }
        
        IPage<SysMessage> messagePage = messageService.getMessagePage(page, size, userId, null, null);
        long unreadCount = messageService.getUnreadCount(userId);
        
        model.addAttribute("page", messagePage);
        model.addAttribute("unreadCount", unreadCount);
        
        return "teacher/messages";
    }

    /**
     * 获取消息列表（JSON格式）
     */
    @GetMapping("/list")
    @ResponseBody
    public Map<String, Object> getMessageList(@RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer size,
                                               @RequestParam(required = false) String type,
                                               @RequestParam(required = false) String status,
                                               HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            result.put("success", false);
            result.put("message", "用户未登录");
            return result;
        }

        try {
            Boolean isRead = null;
            if ("unread".equals(status)) {
                isRead = false;
            } else if ("read".equals(status)) {
                isRead = true;
            }

            String messageType = null;
            if (type != null && !type.isEmpty()) {
                messageType = type.toUpperCase();
            }

            IPage<SysMessage> messagePage = messageService.getMessagePage(page, size, userId, messageType, isRead);
            List<Map<String, Object>> messageList = messagePage.getRecords().stream()
                .map(msg -> convertMessageToMap(msg, userId))
                .collect(java.util.stream.Collectors.toList());
            result.put("success", true);
            result.put("data", messageList);
            Map<String, Object> pageMap = new HashMap<>();
            pageMap.put("current", messagePage.getCurrent());
            pageMap.put("size", messagePage.getSize());
            pageMap.put("total", messagePage.getTotal());
            pageMap.put("pages", messagePage.getPages());
            result.put("page", pageMap);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("获取消息列表失败", e);
        }

        return result;
    }

    @GetMapping("/chat")
    @ResponseBody
    public Map<String, Object> getChatMessages(@RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "20") Integer size,
                                                @RequestParam Long contactId,
                                                HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            result.put("success", false);
            result.put("message", "用户未登录");
            return result;
        }

        try {
            IPage<SysMessage> messagePage = messageService.getChatPage(page, size, userId, contactId);
            List<Map<String, Object>> messageList = messagePage.getRecords().stream()
                .map(msg -> convertMessageToMap(msg, userId))
                .collect(java.util.stream.Collectors.toList());
            result.put("success", true);
            result.put("data", messageList);
            Map<String, Object> pageMap = new HashMap<>();
            pageMap.put("current", messagePage.getCurrent());
            pageMap.put("size", messagePage.getSize());
            pageMap.put("total", messagePage.getTotal());
            pageMap.put("pages", messagePage.getPages());
            result.put("page", pageMap);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("获取聊天消息失败", e);
        }

        return result;
    }

    private Map<String, Object> convertMessageToMap(SysMessage msg, Long userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", msg.getId());
        map.put("title", msg.getTitle());
        map.put("content", msg.getContent());
        map.put("type", msg.getMessageType());
        map.put("isRead", msg.getIsRead());
        map.put("createTime", msg.getCreateTime());
        map.put("receiverId", msg.getReceiverId());
        map.put("senderId", msg.getSenderId());
        
        boolean isSent = userId.equals(msg.getSenderId());
        map.put("isSent", isSent);
        
        String senderName = "未知";
        if (isSent) {
            senderName = "我";
        } else if (msg.getSenderId() != null) {
            User sender = userMapper.selectById(msg.getSenderId());
            if (sender != null) {
                senderName = sender.getRealName();
            }
        }
        map.put("senderName", senderName);
        
        return map;
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread-count")
    @ResponseBody
    public Map<String, Object> getUnreadCount(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            result.put("success", false);
            result.put("message", "用户未登录");
            return result;
        }
        
        try {
            long count = messageService.getUnreadCount(userId);
            result.put("success", true);
            result.put("count", count);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("获取未读数失败", e);
        }
        
        return result;
    }

    /**
     * 标记消息为已读
     */
    @PostMapping("/mark-read/{id}")
    @ResponseBody
    public Map<String, Object> markAsRead(@PathVariable Long id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            result.put("success", false);
            result.put("message", "用户未登录");
            return result;
        }
        
        try {
            boolean success = messageService.markAsRead(id);
            result.put("success", success);
            result.put("message", "已标记为已读");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("标记已读失败", e);
        }
        
        return result;
    }

    /**
     * 批量标记为已读
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
    @DeleteMapping("/delete/{id}")
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
            result.put("message", "删除成功");
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
    public Map<String, Object> getMessageDetail(@PathVariable Long id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            result.put("success", false);
            result.put("message", "用户未登录");
            return result;
        }
        
        try {
            SysMessage message = messageService.getMessageById(id);
            
            if (message == null || !message.getReceiverId().equals(userId)) {
                result.put("success", false);
                result.put("message", "消息不存在或无权查看");
                return result;
            }
            
            // 自动标记为已读
            if (message.getIsRead() == 0) {
                messageService.markAsRead(id);
            }
            
            result.put("success", true);
            result.put("data", message);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("获取消息详情失败", e);
        }
        
        return result;
    }

    /**
     * 回复消息
     */
    @PostMapping("/reply/{id}")
    @ResponseBody
    public Map<String, Object> replyMessage(@PathVariable Long id, 
                                            @RequestParam String content, 
                                            HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            result.put("success", false);
            result.put("message", "用户未登录");
            return result;
        }
        
        try {
            // 获取原消息
            SysMessage originalMessage = messageService.getMessageById(id);
            if (originalMessage == null || !originalMessage.getReceiverId().equals(userId)) {
                result.put("success", false);
                result.put("message", "消息不存在或无权回复");
                return result;
            }
            
            // 获取原消息发送者（学生）
            Long senderId = originalMessage.getSenderId();
            if (senderId == null) {
                result.put("success", false);
                result.put("message", "无法回复系统消息");
                return result;
            }
            
            // 创建回复消息
            SysMessage replyMessage = new SysMessage();
            replyMessage.setReceiverId(senderId);  // 发送给原发送者（学生）
            replyMessage.setSenderId(userId);       // 教师作为发送者
            replyMessage.setMessageType("REPLY");  // 回复类型
            replyMessage.setTitle("教师回复");
            replyMessage.setContent(content);
            replyMessage.setRelatedId(id);          // 关联原消息ID
            replyMessage.setRelatedType("REPLY");
            
            boolean success = messageService.sendMessage(replyMessage);
            result.put("success", success);
            result.put("message", success ? "回复成功" : "回复失败");
            
            log.info("教师{}回复消息{}", userId, id);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("回复消息失败", e);
        }
        
        return result;
    }

    /**
     * 发送消息给学生或其他教师
     */
    @PostMapping("/send")
    @ResponseBody
    public Map<String, Object> sendMessage(@RequestParam Long receiverId,
                                           @RequestParam String receiverType,
                                           @RequestParam String title,
                                           @RequestParam String content,
                                           HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            result.put("success", false);
            result.put("message", "用户未登录");
            return result;
        }

        try {
            SysMessage message = new SysMessage();
            message.setReceiverId(receiverId);
            message.setSenderId(userId);
            message.setMessageType("TEACHER");
            message.setTitle(title);
            message.setContent(content);

            boolean success = messageService.sendMessage(message);
            result.put("success", success);
            result.put("message", success ? "发送成功" : "发送失败");

            log.info("教师{}发送消息给{} {}", userId, receiverType, receiverId);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("发送消息失败", e);
        }

        return result;
    }

    /**
     * 获取教师的联系人列表（学生+其他教师）
     */
    @GetMapping("/contacts")
    @ResponseBody
    public Map<String, Object> getContacts(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            result.put("success", false);
            result.put("message", "用户未登录");
            return result;
        }
        
        try {
            Teacher teacher = teacherMapper.selectByUserId(userId);
            if (teacher == null) {
                result.put("success", false);
                result.put("message", "教师信息不存在");
                return result;
            }
            
            // 获取教师负责的班级学生
            List<Long> classIds = teacherClassService.getClassIdByTeacherId(teacher.getId());
            List<Student> students = studentMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Student>()
                    .in(Student::getClassId, classIds)
                    .orderByAsc(Student::getStudentNo)
            );
            
            List<Map<String, Object>> studentList = students.stream()
                .map(s -> {
                    Map<String, Object> map = new HashMap<>();
                    User user = userMapper.selectById(s.getUserId());
                    map.put("id", s.getUserId());
                    map.put("name", user != null ? user.getRealName() : "未知");
                    map.put("no", s.getStudentNo());
                    map.put("type", "student");
                    return map;
                })
                .collect(Collectors.toList());
            
            // 获取其他教师
            List<Teacher> teachers = teacherMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Teacher>()
                    .ne(Teacher::getId, teacher.getId())
            );
            
            List<Map<String, Object>> teacherList = teachers.stream()
                .map(t -> {
                    Map<String, Object> map = new HashMap<>();
                    User user = userMapper.selectById(t.getUserId());
                    map.put("id", t.getUserId());
                    map.put("name", user != null ? user.getRealName() : "未知");
                    map.put("no", t.getSubject());
                    map.put("type", "teacher");
                    return map;
                })
                .collect(Collectors.toList());
            
            Map<String, Object> data = new HashMap<>();
            data.put("students", studentList);
            data.put("teachers", teacherList);
            
            result.put("success", true);
            result.put("data", data);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("获取联系人列表失败", e);
        }
        
        return result;
    }
}