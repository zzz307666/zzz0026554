package com.hhjt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hhjt.entity.SportRecord;
import com.hhjt.entity.Teacher;
import com.hhjt.entity.User;
import com.hhjt.mapper.SportRecordMapper;
import com.hhjt.mapper.TeacherMapper;
import com.hhjt.service.SportRecordService;
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
 * 教师运动审核控制器
 */
@Slf4j
@Controller
@RequestMapping("/teacher/sport")
public class TeacherSportController {

    @Autowired
    private SportRecordService sportRecordService;
    
    @Autowired
    private com.hhjt.mapper.StudentMapper studentMapper;
    
    @Autowired
    private com.hhjt.mapper.UserMapper userMapper;
    
    @Autowired
    private com.hhjt.mapper.SportTypeMapper sportTypeMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private SportRecordMapper sportRecordMapper;

    /**
     * 待审核列表页面
     */
    @GetMapping("/audit")
    public String auditList(Model model,
                           @RequestParam(defaultValue = "1") Integer page,
                           @RequestParam(defaultValue = "10") Integer size,
                           @RequestParam(required = false) Integer status,
                           @RequestParam(required = false) String startDate,
                           @RequestParam(required = false) String endDate) {
        // 获取当前用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = ((User) authentication.getPrincipal()).getId();
        User currentUser = (User) authentication.getPrincipal();
        
        // 判断是否为管理员
        boolean isAdmin = currentUser.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        // 如果是管理员，查询所有待审核记录；如果是教师，只查询该教师的待审核记录
        Long teacherId = null;
        Teacher teacher = teacherMapper.selectByUserId(userId);
        if (teacher != null) {
            teacherId = teacher.getId();
        }
        
        // 如果status为空，默认查询所有记录（不限制status）
        Integer queryStatus = (status != null) ? status : null;
        
        // 查询记录（支持状态、日期筛选）
        IPage<SportRecord> recordPage = sportRecordService.getRecordPage(
            page, size, null, queryStatus, startDate, endDate, teacherId, isAdmin
        );
        
        // 为运动记录填充学生姓名和运动类型名称
        if (recordPage.getRecords() != null) {
            for (SportRecord record : recordPage.getRecords()) {
                // 填充学生姓名
                if (record.getStudentId() != null) {
                    com.hhjt.entity.Student student = studentMapper.selectById(record.getStudentId());
                    if (student != null) {
                        record.setStudentNo(student.getStudentNo());
                        com.hhjt.entity.User user = userMapper.selectById(student.getUserId());
                        if (user != null) {
                            record.setStudentName(user.getRealName());
                        }
                    }
                }
                
                // 填充运动类型名称
                if (record.getSportTypeId() != null) {
                    com.hhjt.entity.SportType sportType = sportTypeMapper.selectById(record.getSportTypeId());
                    if (sportType != null) {
                        record.setSportTypeName(sportType.getTypeName());
                    }
                }
            }
        }

        model.addAttribute("page", recordPage);
        // 如果是教师，显示该教师的待审核数量；如果是管理员，显示所有待审核数量
        model.addAttribute("pendingCount", sportRecordService.getPendingCount(teacherId, isAdmin));
        model.addAttribute("status", status);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("isAdmin", isAdmin); // 添加isAdmin属性供模板使用

        return "teacher/sport_audit";
    }

    /**
     * 审核操作
     */
    @PostMapping("/audit")
    @ResponseBody
    public Map<String, Object> audit(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long recordId = Long.valueOf(params.get("recordId").toString());
            Integer status = Integer.valueOf(params.get("status").toString());
            String remark = params.getOrDefault("remark", "").toString();

            // 获取当前用户ID（教师或管理员）
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = ((User) authentication.getPrincipal()).getId();
            Teacher teacher = teacherMapper.selectByUserId(userId);
            
            // 如果是管理员但没有教师记录，使用userId作为审核人ID
            Long auditorId = (teacher != null) ? teacher.getId() : userId;

            boolean success = sportRecordService.auditRecord(recordId, status, remark, auditorId);
            result.put("success", success);
            result.put("message", success ? "审核成功" : "审核失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "审核失败：" + e.getMessage());
            log.error("审核异常", e);
        }
        return result;
    }

    /**
     * 批量审核
     */
    @PostMapping("/batch-audit")
    @ResponseBody
    public Map<String, Object> batchAudit(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            @SuppressWarnings("unchecked")
            List<Long> recordIds = (List<Long>) params.get("recordIds");
            Integer status = Integer.valueOf(params.get("status").toString());

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = ((User) authentication.getPrincipal()).getId();
            Teacher teacher = teacherMapper.selectByUserId(userId);
            
            // 如果是管理员但没有教师记录，使用userId作为审核人ID
            Long auditorId = (teacher != null) ? teacher.getId() : userId;

            boolean success = sportRecordService.batchAudit(recordIds, status, auditorId);
            result.put("success", success);
            result.put("message", success ? "批量审核成功" : "批量审核失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "批量审核失败：" + e.getMessage());
            log.error("批量审核异常", e);
        }
        return result;
    }

    /**
     * 班级排名
     */
    @GetMapping("/ranking")
    @ResponseBody
    public Map<String, Object> getClassRanking(@RequestParam Long classId) {
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
