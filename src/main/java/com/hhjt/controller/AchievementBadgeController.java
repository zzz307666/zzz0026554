package com.hhjt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hhjt.entity.AchievementBadge;
import com.hhjt.entity.Student;
import com.hhjt.entity.StudentBadge;
import com.hhjt.mapper.StudentMapper;
import com.hhjt.service.AchievementBadgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 成就徽章控制器
 */
@Slf4j
@Controller
@RequestMapping("/badge")
public class AchievementBadgeController {

    @Autowired
    private AchievementBadgeService badgeService;

    @Autowired
    private StudentMapper studentMapper;

    /**
     * 管理员 - 徽章管理页面
     */
    @GetMapping("/admin/list")
    public String adminBadgeList(@RequestParam(defaultValue = "1") Integer page,
                                  @RequestParam(defaultValue = "10") Integer size,
                                  Model model) {
        IPage<AchievementBadge> badgePage = badgeService.getBadgePage(page, size);
        model.addAttribute("page", badgePage);
        return "admin/badge";
    }

    /**
     * 学生端 - 我的徽章墙
     */
    @GetMapping("/my-badges")
    public String myBadges(@RequestParam(defaultValue = "1") Integer page,
                           @RequestParam(defaultValue = "20") Integer size,
                           HttpSession session,
                           Model model) {
        Long userId = (Long) session.getAttribute("userId");
        Student student = studentMapper.selectByUserId(userId);
        
        if (student == null) {
            return "redirect:/login";
        }
        
        IPage<StudentBadge> myBadges = badgeService.getStudentBadges(page, size, student.getId());
        List<AchievementBadge> allBadges = badgeService.getAllActiveBadges();
        
        model.addAttribute("page", myBadges);
        model.addAttribute("allBadges", allBadges);
        model.addAttribute("studentId", student.getId());
        
        return "student/my_badges";
    }

    /**
     * 创建徽章（管理员）
     */
    @PostMapping("/admin/create")
    @ResponseBody
    public Map<String, Object> createBadge(@RequestBody AchievementBadge badge) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean success = badgeService.createBadge(badge);
            result.put("success", success);
            result.put("message", success ? "创建成功" : "创建失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("创建徽章失败", e);
        }
        
        return result;
    }

    /**
     * 更新徽章（管理员）
     */
    @PostMapping("/admin/update")
    @ResponseBody
    public Map<String, Object> updateBadge(@RequestBody AchievementBadge badge) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean success = badgeService.updateBadge(badge);
            result.put("success", success);
            result.put("message", success ? "更新成功" : "更新失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("更新徽章失败", e);
        }
        
        return result;
    }

    /**
     * 删除徽章（管理员）
     */
    @PostMapping("/admin/delete/{id}")
    @ResponseBody
    public Map<String, Object> deleteBadge(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean success = badgeService.deleteBadge(id);
            result.put("success", success);
            result.put("message", success ? "删除成功" : "删除失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("删除徽章失败", e);
        }
        
        return result;
    }

    /**
     * 授予徽章给学生（管理员）
     */
    @PostMapping("/admin/award")
    @ResponseBody
    public Map<String, Object> awardBadge(@RequestParam Long studentId,
                                           @RequestParam Long badgeId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean success = badgeService.awardBadgeToStudent(studentId, badgeId);
            result.put("success", success);
            result.put("message", success ? "授予成功" : "该学生已获得此徽章");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("授予徽章失败", e);
        }
        
        return result;
    }

    /**
     * 检查并授予徽章（手动触发）
     */
    @PostMapping("/check/{studentId}")
    @ResponseBody
    public Map<String, Object> checkBadges(@PathVariable Long studentId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            badgeService.checkAndAwardBadges(studentId);
            result.put("success", true);
            result.put("message", "检查完成");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("检查徽章失败", e);
        }
        
        return result;
    }
}