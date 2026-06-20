package com.hhjt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hhjt.entity.StudentEvaluation;
import com.hhjt.entity.Teacher;
import com.hhjt.mapper.TeacherMapper;
import com.hhjt.service.EvaluationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * 教师端 - 评价历史管理控制器
 */
@Slf4j
@Controller
@RequestMapping("/teacher/evaluation")
public class TeacherEvaluationHistoryController {

    @Autowired
    private EvaluationService evaluationService;

    @Autowired
    private TeacherMapper teacherMapper;

    /**
     * 评价历史列表页面
     */
    @GetMapping("/history")
    public String evaluationHistory(@RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "20") Integer size,
                                     @RequestParam(required = false) String period,
                                     @RequestParam(required = false) Long classId,
                                     HttpSession session,
                                     Model model) {
        Long userId = (Long) session.getAttribute("userId");
        Teacher teacher = teacherMapper.selectByUserId(userId);
        
        if (teacher == null) {
            return "redirect:/login";
        }
        
        // 获取教师所教班级ID列表
        List<Long> classIds = null; // TODO: 从 TeacherClass 表查询
        
        IPage<StudentEvaluation> evalPage = evaluationService.getEvaluationPage(
            page, size, teacher.getId(), period, classIds, false
        );
        
        model.addAttribute("page", evalPage);
        model.addAttribute("currentPeriod", period);
        model.addAttribute("currentClassId", classId);
        
        return "teacher/evaluation_history";
    }

    /**
     * 查看评价详情
     */
    @GetMapping("/detail/{id}")
    public String evaluationDetail(@PathVariable Long id, Model model) {
        StudentEvaluation evaluation = evaluationService.getEvaluationById(id);
        model.addAttribute("evaluation", evaluation);
        return "teacher/evaluation_detail";
    }

    /**
     * 修改评价（仅限未锁定的评价）
     */
    @PostMapping("/update")
    @ResponseBody
    public String updateEvaluation(@RequestBody StudentEvaluation evaluation, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        Teacher teacher = teacherMapper.selectByUserId(userId);
        
        if (teacher == null) {
            return "{\"success\":false,\"message\":\"用户未登录\"}";
        }
        
        // 验证权限：只能修改自己的评价
        StudentEvaluation existing = evaluationService.getEvaluationById(evaluation.getId());
        if (existing == null || !existing.getTeacherId().equals(teacher.getId())) {
            return "{\"success\":false,\"message\":\"无权修改此评价\"}";
        }
        
        // 检查是否已发布（已发布的评价不能修改）
        if (existing.getStatus() != null && existing.getStatus() == 1) {
            return "{\"success\":false,\"message\":\"评价已发布，无法修改\"}";
        }
        
        boolean success = evaluationService.saveEvaluation(evaluation);
        return success ? "{\"success\":true,\"message\":\"修改成功\"}" : "{\"success\":false,\"message\":\"修改失败\"}";
    }

    /**
     * 导出评价结果（CSV格式）
     */
    @GetMapping("/export")
    public void exportEvaluations(@RequestParam(required = false) String period,
                                   @RequestParam(required = false) Long classId,
                                   HttpSession session,
                                   HttpServletResponse response) throws IOException {
        Long userId = (Long) session.getAttribute("userId");
        Teacher teacher = teacherMapper.selectByUserId(userId);
        
        if (teacher == null) {
            response.sendError(403, "未授权");
            return;
        }
        
        // 设置响应头
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=evaluations.csv");
        
        PrintWriter writer = response.getWriter();
        
        // CSV 表头
        writer.println("学号,姓名,班级,学期,耐力,力量,速度,柔韧,协调,总分,等级,评语");
        
        // TODO: 查询数据并写入CSV
        // 这里简化处理，实际应该查询数据库
        
        writer.flush();
        writer.close();
        
        log.info("教师{}导出评价结果", teacher.getTeacherNo());
    }
}
