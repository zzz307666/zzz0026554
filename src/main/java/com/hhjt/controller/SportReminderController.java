package com.hhjt.controller;

import com.hhjt.service.SportReminderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 学生端 - 运动打卡提醒控制器
 */
@Slf4j
@Controller
@RequestMapping("/student/reminder")
public class SportReminderController {

    @Autowired
    private SportReminderService reminderService;

    /**
     * 提醒设置页面
     */
    @GetMapping("/settings")
    public String reminderSettings(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("reminders", reminderService.getUserReminders(userId));
        model.addAttribute("stats", reminderService.getCheckInStats(userId));
        
        return "student/reminder_settings";
    }

    /**
     * 保存提醒配置
     */
    @PostMapping("/save")
    @ResponseBody
    public Map<String, Object> saveReminder(@RequestParam String reminderType,
                                             @RequestParam String reminderTime,
                                             @RequestParam(required = false) Boolean isEnabled,
                                             @RequestParam(required = false) String messageTemplate,
                                             HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            result.put("success", false);
            result.put("message", "用户未登录");
            return result;
        }
        
        try {
            boolean success = reminderService.saveReminder(userId, reminderType, 
                                                          reminderTime, isEnabled, messageTemplate);
            result.put("success", success);
            result.put("message", success ? "保存成功" : "保存失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("保存提醒配置失败", e);
        }
        
        return result;
    }

    /**
     * 获取打卡统计
     */
    @GetMapping("/stats")
    @ResponseBody
    public Map<String, Object> getCheckInStats(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            result.put("success", false);
            result.put("message", "用户未登录");
            return result;
        }
        
        try {
            result.put("success", true);
            result.put("data", reminderService.getCheckInStats(userId));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("获取打卡统计失败", e);
        }
        
        return result;
    }
}
