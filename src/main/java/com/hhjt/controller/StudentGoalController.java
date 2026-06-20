package com.hhjt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hhjt.entity.Student;
import com.hhjt.entity.StudentGoal;
import com.hhjt.mapper.StudentMapper;
import com.hhjt.service.StudentGoalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 学生端 - 运动目标控制器
 */
@Slf4j
@Controller
@RequestMapping("/student/goal")
public class StudentGoalController {

    @Autowired
    private StudentGoalService goalService;

    @Autowired
    private StudentMapper studentMapper;

    /**
     * 目标管理页面
     */
    @GetMapping({"", "/", "/list"})
    public String goalList(@RequestParam(defaultValue = "1") Integer page,
                           @RequestParam(defaultValue = "10") Integer size,
                           @RequestParam(required = false) Integer status,
                           HttpSession session,
                           Model model) {
        // 获取当前学生ID
        Long userId = (Long) session.getAttribute("userId");
        Student student = studentMapper.selectByUserId(userId);
        
        if (student == null) {
            return "redirect:/login";
        }
        
        IPage<StudentGoal> goalPage = goalService.getGoalPage(page, size, student.getId(), status);
        model.addAttribute("page", goalPage);
        model.addAttribute("activeCount", goalService.getActiveGoalCount(student.getId()));

        return "student/goal";
    }

    /**
     * 创建目标
     */
    @PostMapping("/create")
    @ResponseBody
    public Map<String, Object> createGoal(@RequestBody StudentGoal goal, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Long userId = (Long) session.getAttribute("userId");
            Student student = studentMapper.selectByUserId(userId);
            
            if (student == null) {
                result.put("success", false);
                result.put("message", "用户未登录");
                return result;
            }
            
            goal.setStudentId(student.getId());
            boolean success = goalService.createGoal(goal);
            
            result.put("success", success);
            result.put("message", success ? "创建成功" : "创建失败");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("创建目标失败", e);
        }
        
        return result;
    }

    /**
     * 更新目标
     */
    @PostMapping("/update")
    @ResponseBody
    public Map<String, Object> updateGoal(@RequestBody StudentGoal goal) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean success = goalService.updateGoal(goal);
            result.put("success", success);
            result.put("message", success ? "更新成功" : "更新失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("更新目标失败", e);
        }
        
        return result;
    }

    /**
     * 取消目标
     */
    @PostMapping("/cancel/{id}")
    @ResponseBody
    public Map<String, Object> cancelGoal(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean success = goalService.cancelGoal(id);
            result.put("success", success);
            result.put("message", success ? "取消成功" : "取消失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("取消目标失败", e);
        }
        
        return result;
    }

    /**
     * 删除目标
     */
    @PostMapping("/delete/{id}")
    @ResponseBody
    public Map<String, Object> deleteGoal(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean success = goalService.deleteGoal(id);
            result.put("success", success);
            result.put("message", success ? "删除成功" : "删除失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("删除目标失败", e);
        }
        
        return result;
    }

    /**
     * 更新进度
     */
    @PostMapping("/progress/{id}")
    @ResponseBody
    public Map<String, Object> updateProgress(@PathVariable Long id, 
                                               @RequestParam BigDecimal currentValue) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean success = goalService.updateProgress(id, currentValue);
            result.put("success", success);
            result.put("message", success ? "进度更新成功" : "进度更新失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("更新进度失败", e);
        }
        
        return result;
    }

    /**
     * 获取目标详情
     */
    @GetMapping("/detail/{id}")
    @ResponseBody
    public StudentGoal getDetail(@PathVariable Long id) {
        return goalService.getGoalById(id);
    }
}