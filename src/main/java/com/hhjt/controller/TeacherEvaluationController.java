package com.hhjt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hhjt.entity.*;
import com.hhjt.mapper.StudentMapper;
import com.hhjt.mapper.TeacherMapper;
import com.hhjt.service.EvaluationService;
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
 * 教师评价控制器
 */
@Slf4j
@Controller
@RequestMapping("/teacher/evaluation")
public class TeacherEvaluationController {

    @Autowired
    private EvaluationService evaluationService;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private com.hhjt.service.TeacherClassService teacherClassService;
    
    @Autowired
    private com.hhjt.mapper.SportRecordMapper sportRecordMapper;
    
    @Autowired
    private com.hhjt.mapper.UserMapper userMapper;
    
    @Autowired
    private com.hhjt.mapper.SportTypeMapper sportTypeMapper;

    /**
     * 评价学生页面
     */
    @GetMapping("/evaluate/{studentId}")
    public String evaluatePage(@PathVariable Long studentId, Model model) {
        // 获取当前用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = ((User) authentication.getPrincipal()).getId();
        User currentUser = (User) authentication.getPrincipal();
        
        // 判断是否为管理员
        boolean isAdmin = currentUser.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new RuntimeException("学生不存在");
        }
        
        // 如果不是管理员，需要验证该学生是否在教师负责的班级中
        if (!isAdmin) {
            Teacher teacher = teacherMapper.selectByUserId(userId);
            if (teacher != null) {
                List<Long> classIds = teacherClassService.getClassIdByTeacherId(teacher.getId());
                if (classIds == null || !classIds.contains(student.getClassId())) {
                    throw new RuntimeException("您无权评价该学生（不在您负责的班级中）");
                }
            }
        }
        
        // 查询学生关联的用户信息
        User user = userMapper.selectById(student.getUserId());
        model.addAttribute("student", student);
        model.addAttribute("user", user);
        
        // 查询该学生已有的评价（用于编辑）
        List<StudentEvaluation> evaluations = evaluationService.getStudentEvaluations(studentId);
        if (!evaluations.isEmpty()) {
            model.addAttribute("existingEvaluation", evaluations.get(0));
        }
        
        // 查询该学生已审核通过的运动记录
        List<com.hhjt.entity.SportRecord> sportRecords = sportRecordMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.hhjt.entity.SportRecord>()
                .eq(com.hhjt.entity.SportRecord::getStudentId, studentId)
                .eq(com.hhjt.entity.SportRecord::getStatus, 1) // 只查询已通过的
                .orderByDesc(com.hhjt.entity.SportRecord::getRecordDate)
        );
        
        // 为运动记录填充类型名称
        if (sportRecords != null) {
            for (com.hhjt.entity.SportRecord record : sportRecords) {
                if (record.getSportTypeId() != null) {
                    com.hhjt.entity.SportType sportType = sportTypeMapper.selectById(record.getSportTypeId());
                    if (sportType != null) {
                        record.setSportTypeName(sportType.getTypeName());
                    }
                }
            }
        }
        
        model.addAttribute("sportRecords", sportRecords);
        
        // 查询运动统计信息
        Map<String, Object> sportStats = sportRecordMapper.getStudentSportStats(studentId);
        model.addAttribute("sportStats", sportStats);
        
        model.addAttribute("isAdmin", isAdmin); // 添加isAdmin属性供模板使用

        return "teacher/evaluate_student";
    }

    /**
     * 保存评价
     */
    @PostMapping("/save")
    @ResponseBody
    public Map<String, Object> saveEvaluation(@RequestBody StudentEvaluation evaluation) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 获取当前用户ID（教师或管理员）
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = ((User) authentication.getPrincipal()).getId();
            Teacher teacher = teacherMapper.selectByUserId(userId);
            
            // 如果是管理员但没有教师记录，使用userId作为评价人ID
            Long evaluatorId = (teacher != null) ? teacher.getId() : userId;
            evaluation.setTeacherId(evaluatorId);
            
            boolean success = evaluationService.saveEvaluation(evaluation);
            result.put("success", success);
            result.put("message", success ? "保存成功" : "保存失败");
            result.put("evaluationId", evaluation.getId());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "保存失败：" + e.getMessage());
            log.error("保存评价异常", e);
        }
        return result;
    }

    /**
     * 发布评价
     */
    @PostMapping("/publish/{id}")
    @ResponseBody
    public Map<String, Object> publishEvaluation(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = evaluationService.publishEvaluation(id);
            result.put("success", success);
            result.put("message", success ? "发布成功" : "发布失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "发布失败：" + e.getMessage());
            log.error("发布评价异常", e);
        }
        return result;
    }

    /**
     * 我的评价列表
     */
    @GetMapping("/list")
    public String evaluationList(Model model,
                                @RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer size,
                                @RequestParam(required = false) String period) {
        // 获取当前用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = ((User) authentication.getPrincipal()).getId();
        User currentUser = (User) authentication.getPrincipal();
        
        // 判断是否为管理员
        boolean isAdmin = currentUser.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        // 如果是教师，获取该教师负责的班级ID列表
        Teacher teacher = teacherMapper.selectByUserId(userId);
        List<Long> classIds = null;
        if (!isAdmin && teacher != null) {
            classIds = teacherClassService.getClassIdByTeacherId(teacher.getId());
        }

        // 查询评价列表（根据角色过滤）
        IPage<StudentEvaluation> evalPage = evaluationService.getEvaluationPage(
            page, size, teacher != null ? teacher.getId() : null, period, classIds, isAdmin
        );
        
        model.addAttribute("page", evalPage);
        model.addAttribute("period", period);
        model.addAttribute("isAdmin", isAdmin); // 添加isAdmin属性供模板使用

        return "teacher/evaluation_list";
    }
}
