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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学生端 - 徽章控制器
 */
@Slf4j
@Controller
@RequestMapping("/student")
public class StudentBadgeController {

    @Autowired
    private AchievementBadgeService badgeService;

    @Autowired
    private StudentMapper studentMapper;

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
        
        // 提取已获得的徽章ID列表
        List<Long> earnedBadgeIds = myBadges.getRecords().stream()
                .map(StudentBadge::getBadgeId)
                .collect(Collectors.toList());
        
        model.addAttribute("page", myBadges);
        model.addAttribute("allBadges", allBadges);
        model.addAttribute("studentId", student.getId());
        model.addAttribute("earnedBadgeIds", earnedBadgeIds);
        
        return "student/my_badges";
    }
}