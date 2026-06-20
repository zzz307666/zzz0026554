package com.hhjt.controller;

import com.hhjt.service.ClassRankingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 教师端 - 班级排行榜控制器
 */
@Slf4j
@Controller
@RequestMapping("/teacher/class")
public class TeacherClassRankingController {

    @Autowired
    private ClassRankingService rankingService;

    /**
     * 班级排行榜页面
     */
    @GetMapping("/ranking")
    public String classRanking(@RequestParam(defaultValue = "1") Long classId,
                                @RequestParam(defaultValue = "month") String period,
                                Model model) {
        model.addAttribute("classId", classId);
        model.addAttribute("period", period);
        return "teacher/class_ranking";
    }

    /**
     * 获取积分排行榜
     */
    @GetMapping("/ranking/points")
    @ResponseBody
    public Map<String, Object> getPointsRanking(@RequestParam Long classId,
                                                 @RequestParam(defaultValue = "month") String period) {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("success", true);
            result.put("data", rankingService.getPointsRanking(classId, period));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("获取积分排行榜失败", e);
        }
        return result;
    }

    /**
     * 获取运动次数排行榜
     */
    @GetMapping("/ranking/sport-count")
    @ResponseBody
    public Map<String, Object> getSportCountRanking(@RequestParam Long classId,
                                                     @RequestParam(defaultValue = "month") String period) {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("success", true);
            result.put("data", rankingService.getSportCountRanking(classId, period));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("获取运动次数排行榜失败", e);
        }
        return result;
    }

    /**
     * 获取最佳进步奖
     */
    @GetMapping("/ranking/most-improved")
    @ResponseBody
    public Map<String, Object> getMostImproved(@RequestParam Long classId,
                                                @RequestParam(defaultValue = "month") String period) {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("success", true);
            result.put("data", rankingService.getMostImprovedStudents(classId, period));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("获取最佳进步奖失败", e);
        }
        return result;
    }

    /**
     * 导出排行榜
     */
    @GetMapping("/ranking/export")
    public ResponseEntity<byte[]> exportRanking(@RequestParam Long classId,
                                                 @RequestParam String type,
                                                 @RequestParam(defaultValue = "month") String period) {
        try {
            String csv = rankingService.exportRanking(classId, type, period);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
            headers.setContentDispositionFormData("attachment", 
                "班级排行榜_" + System.currentTimeMillis() + ".csv");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(csv.getBytes(StandardCharsets.UTF_8));
                
        } catch (Exception e) {
            log.error("导出排行榜失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
