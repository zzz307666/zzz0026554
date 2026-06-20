package com.hhjt.controller;

import com.hhjt.service.StudentProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 教师端 - 学生成长档案控制器
 */
@Slf4j
@Controller
@RequestMapping("/teacher/student")
public class TeacherStudentProfileController {

    @Autowired
    private StudentProfileService profileService;

    /**
     * 学生成长档案页面
     */
    @GetMapping("/profile/{studentId}")
    public String studentProfile(@PathVariable Long studentId, Model model) {
        model.addAttribute("studentId", studentId);
        return "teacher/student_profile";
    }

    /**
     * 获取学生基本信息
     */
    @GetMapping("/profile/{studentId}/basic-info")
    @ResponseBody
    public Map<String, Object> getBasicInfo(@PathVariable Long studentId) {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("success", true);
            result.put("data", profileService.getStudentBasicInfo(studentId));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("获取学生基本信息失败", e);
        }
        return result;
    }

    /**
     * 获取运动记录时间轴
     */
    @GetMapping("/profile/{studentId}/sport-timeline")
    @ResponseBody
    public Map<String, Object> getSportTimeline(@PathVariable Long studentId) {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("success", true);
            result.put("data", profileService.getSportRecordTimeline(studentId));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("获取运动时间轴失败", e);
        }
        return result;
    }

    /**
     * 获取积分变化曲线
     */
    @GetMapping("/profile/{studentId}/points-curve")
    @ResponseBody
    public Map<String, Object> getPointsCurve(@PathVariable Long studentId) {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("success", true);
            result.put("data", profileService.getPointsChangeCurve(studentId));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("获取积分曲线失败", e);
        }
        return result;
    }

    /**
     * 获取评价雷达图数据
     */
    @GetMapping("/profile/{studentId}/evaluation-radar")
    @ResponseBody
    public Map<String, Object> getEvaluationRadar(@PathVariable Long studentId) {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("success", true);
            result.put("data", profileService.getEvaluationRadarData(studentId));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("获取评价雷达数据失败", e);
        }
        return result;
    }

    /**
     * 获取成长趋势分析
     */
    @GetMapping("/profile/{studentId}/growth-trend")
    @ResponseBody
    public Map<String, Object> getGrowthTrend(@PathVariable Long studentId) {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("success", true);
            result.put("data", profileService.getGrowthTrendAnalysis(studentId));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("获取成长趋势失败", e);
        }
        return result;
    }
}
