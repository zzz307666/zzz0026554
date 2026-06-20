package com.hhjt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hhjt.entity.Student;
import com.hhjt.entity.Teacher;
import com.hhjt.entity.User;
import com.hhjt.mapper.StudentMapper;
import com.hhjt.mapper.TeacherMapper;
import com.hhjt.mapper.UserMapper;
import com.hhjt.service.SysMessageService;
import com.hhjt.entity.SysMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/messages")
public class AdminMessageController {

    private static final Logger log = LoggerFactory.getLogger(AdminMessageController.class);

    @Autowired
    private SysMessageService messageService;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/index")
    public String messageList(@RequestParam(defaultValue = "1") Integer page,
                              Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        int size = 10;
        IPage<SysMessage> messagePage = messageService.getMessagePage(page, size, userId, null, null);
        long unreadCount = messageService.getUnreadCount(userId);

        model.addAttribute("page", messagePage);
        model.addAttribute("unreadCount", unreadCount);
        return "admin/messages";
    }

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
            result.put("message", success ? "已标记为已读" : "操作失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("标记已读失败", e);
        }

        return result;
    }

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
            result.put("message", success ? "已全部标记为已读" : "操作失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("标记全部已读失败", e);
        }

        return result;
    }

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
            SysMessage originalMessage = messageService.getMessageById(id);
            if (originalMessage == null || !originalMessage.getReceiverId().equals(userId)) {
                result.put("success", false);
                result.put("message", "消息不存在或无权回复");
                return result;
            }

            Long senderId = originalMessage.getSenderId();
            if (senderId == null) {
                result.put("success", false);
                result.put("message", "无法回复系统消息");
                return result;
            }

            SysMessage replyMessage = new SysMessage();
            replyMessage.setReceiverId(senderId);
            replyMessage.setSenderId(userId);
            replyMessage.setMessageType("ADMIN");
            replyMessage.setTitle("管理员回复");
            replyMessage.setContent(content);
            replyMessage.setRelatedId(id);
            replyMessage.setRelatedType("REPLY");

            boolean success = messageService.sendMessage(replyMessage);
            result.put("success", success);
            result.put("message", success ? "回复成功" : "回复失败");

            log.info("管理员{}回复消息{}", userId, id);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("回复消息失败", e);
        }

        return result;
    }

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
            message.setMessageType("ADMIN");
            message.setTitle(title);
            message.setContent(content);

            boolean success = messageService.sendMessage(message);
            result.put("success", success);
            result.put("message", success ? "发送成功" : "发送失败");

            log.info("管理员{}发送消息给{} {}", userId, receiverType, receiverId);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("发送消息失败", e);
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
            // 获取所有教师
            List<Teacher> teachers = teacherMapper.selectList(null);

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

            // 获取所有学生
            List<Student> students = studentMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Student>()
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

            Map<String, Object> data = new HashMap<>();
            data.put("teachers", teacherList);
            data.put("students", studentList);

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