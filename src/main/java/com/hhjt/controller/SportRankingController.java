package com.hhjt.controller;

import com.hhjt.entity.Student;
import com.hhjt.mapper.SportRecordMapper;
import com.hhjt.mapper.StudentMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 运动排名控制器（所有用户可访问）
 */
@Slf4j
@Controller
@RequestMapping("/sport/ranking")
public class SportRankingController {

    @Autowired
    private SportRecordMapper sportRecordMapper;

    @Autowired
    private StudentMapper studentMapper;

    /**
     * 排名页面（所有认证用户可访问）
     */
    @GetMapping
    public String rankingPage(Model model, 
                             @RequestParam(required = false) Long classId,
                             @RequestParam(defaultValue = "points") String type) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        com.hhjt.entity.User currentUser = (com.hhjt.entity.User) authentication.getPrincipal();
        
        // 判断用户角色
        boolean isStudent = currentUser.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_STUDENT"));
        boolean isAdmin = currentUser.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        // 如果是学生，获取其班级ID
        if (isStudent) {
            Student student = studentMapper.selectByUserId(currentUser.getId());
            if (student != null && classId == null) {
                classId = student.getClassId();
            }
            model.addAttribute("currentStudent", student);
        }
        
        model.addAttribute("classId", classId);
        model.addAttribute("rankingType", type); // points 或 sport
        model.addAttribute("isStudent", isStudent);
        model.addAttribute("isAdmin", isAdmin);
        
        return "sport/ranking";
    }
    
    /**
     * 兼容旧路径 /student/ranking，重定向到新路径
     */
    @org.springframework.web.bind.annotation.GetMapping("/student-ranking")
    public String redirectFromOldPath() {
        return "redirect:/sport/ranking";
    }

    /**
     * 获取排名数据API（所有认证用户可访问）
     */
    @GetMapping("/data")
    @ResponseBody
    public Map<String, Object> getRankingData(@RequestParam(required = false) Long classId,
                                             @RequestParam(defaultValue = "points") String type) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Map<String, Object>> ranking;
            
            if ("sport".equals(type)) {
                // 运动排名（基于运动记录获得的积分）
                if (classId != null) {
                    ranking = sportRecordMapper.getClassRanking(classId);
                } else {
                    ranking = sportRecordMapper.getSchoolRanking();
                }
            } else {
                // 积分排名（基于累计获得的总积分，不扣除兑换）
                if (classId != null) {
                    ranking = sportRecordMapper.getClassPointsRanking(classId);
                } else {
                    ranking = sportRecordMapper.getSchoolPointsRanking();
                }
            }
            
            // 如果是学生，标记其排名位置
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            com.hhjt.entity.User currentUser = (com.hhjt.entity.User) authentication.getPrincipal();
            
            boolean isStudent = currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_STUDENT"));
            
            if (isStudent) {
                Student student = studentMapper.selectByUserId(currentUser.getId());
                if (student != null) {
                    // 查找当前学生的排名
                    for (int i = 0; i < ranking.size(); i++) {
                        Map<String, Object> item = ranking.get(i);
                        if (student.getId().equals(item.get("studentId"))) {
                            item.put("isCurrentUser", true);
                            break;
                        }
                    }
                }
            }
            
            result.put("success", true);
            result.put("data", ranking);
            result.put("type", type);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取排名失败：" + e.getMessage());
            log.error("获取排名数据异常", e);
        }
        return result;
    }

    /**
     * 获取所有班级列表（用于筛选）
     */
    @GetMapping("/classes")
    @ResponseBody
    public Map<String, Object> getClassList() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 查询所有班级
            List<Map<String, Object>> classes = sportRecordMapper.getAllClasses();
            log.info("查询到班级数量: {}", classes != null ? classes.size() : 0);
            if (classes != null) {
                classes.forEach(cls -> log.info("班级: {}", cls));
            }
            result.put("success", true);
            result.put("data", classes);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取班级列表失败：" + e.getMessage());
            log.error("获取班级列表异常", e);
        }
        return result;
    }
}
