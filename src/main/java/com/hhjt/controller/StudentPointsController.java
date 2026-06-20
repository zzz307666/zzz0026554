package com.hhjt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hhjt.entity.Student;
import com.hhjt.entity.StudentPoints;
import com.hhjt.mapper.StudentMapper;
import com.hhjt.service.EvaluationService;
import com.hhjt.service.PointsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学生积分和评价控制器
 */
@Slf4j
@Controller
@RequestMapping("/student")
public class StudentPointsController {

    @Autowired
    private PointsService pointsService;

    @Autowired
    private EvaluationService evaluationService;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private com.hhjt.mapper.SportRecordMapper sportRecordMapper;

    /**
     * 我的积分页面
     */
    @GetMapping("/points")
    public String myPoints(Model model,
                          @RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "10") Integer size,
                          @RequestParam(required = false) String pointsType) {
        // 获取当前学生ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = ((com.hhjt.entity.User) authentication.getPrincipal()).getId();
        Student student = studentMapper.selectByUserId(userId);

        // 查询总积分
        BigDecimal totalPoints = pointsService.getStudentTotalPoints(student.getId());
        model.addAttribute("totalPoints", totalPoints);

        // 分页查询积分明细
        IPage<StudentPoints> pointsPage = pointsService.getPointsPage(page, size, student.getId(), pointsType);
        model.addAttribute("page", pointsPage);
        model.addAttribute("pointsType", pointsType);

        return "student/points";
    }

    /**
     * 评价结果页面
     */
    @GetMapping("/evaluation")
    public String evaluation(Model model) {
        // 获取当前学生ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = ((com.hhjt.entity.User) authentication.getPrincipal()).getId();
        Student student = studentMapper.selectByUserId(userId);

        // 查询评价列表
        model.addAttribute("evaluations", evaluationService.getStudentEvaluations(student.getId()));

        return "student/evaluation";
    }

    /**
     * 班级排名页面（已废弃，重定向到通用排名页面）
     */
    @GetMapping("/ranking")
    public String ranking() {
        return "redirect:/sport/ranking";
    }

    /**
     * 获取班级排名数据（API接口）
     */
    @GetMapping("/ranking/data")
    @ResponseBody
    public Map<String, Object> getClassRankingData(@RequestParam Long classId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Map<String, Object>> ranking = sportRecordMapper.getClassRanking(classId);
            result.put("success", true);
            result.put("data", ranking);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }
}
