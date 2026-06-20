package com.hhjt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hhjt.entity.SportRecord;
import com.hhjt.entity.SportType;
import com.hhjt.entity.Student;
import com.hhjt.mapper.SportRecordMapper;
import com.hhjt.mapper.SportTypeMapper;
import com.hhjt.mapper.StudentMapper;
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
 * 学生运动打卡控制器
 */
@Slf4j
@Controller
@RequestMapping("/student/sport")
public class StudentSportController {

    @Autowired
    private SportRecordService sportRecordService;

    @Autowired
    private SportTypeMapper sportTypeMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private SportRecordMapper sportRecordMapper;

    /**
     * 运动打卡页面
     */
    @GetMapping("/checkin")
    public String checkinPage(Model model) {
        // 获取所有启用的运动类型
        List<SportType> sportTypes = sportTypeMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SportType>()
                .eq(SportType::getStatus, 1)
                .orderByAsc(SportType::getSortOrder)
        );
        model.addAttribute("sportTypes", sportTypes);
        
        // 获取当前学生信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = ((com.hhjt.entity.User) authentication.getPrincipal()).getId();
        Student student = studentMapper.selectByUserId(userId);
        
        // 查询今日已打卡记录
        java.time.LocalDate today = java.time.LocalDate.now();
        List<SportRecord> todayRecords = sportRecordMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SportRecord>()
                .eq(SportRecord::getStudentId, student.getId())
                .eq(SportRecord::getRecordDate, today)
                .orderByDesc(SportRecord::getCreateTime)
        );
        model.addAttribute("todayRecords", todayRecords);
        
        return "student/sport_checkin";
    }

    /**
     * 提交打卡记录
     */
    @PostMapping("/submit")
    @ResponseBody
    public Map<String, Object> submitRecord(@RequestBody SportRecord record) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = ((com.hhjt.entity.User) authentication.getPrincipal()).getId();

            boolean success = sportRecordService.submitRecord(record, userId);
            result.put("success", success);
            result.put("message", success ? "打卡成功，等待教师审核" : "打卡失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "打卡失败：" + e.getMessage());
            log.error("提交打卡记录异常", e);
        }
        return result;
    }

    /**
     * 我的打卡记录页面
     */
    @GetMapping("/records")
    public String myRecords(Model model,
                           @RequestParam(defaultValue = "1") Integer page,
                           @RequestParam(defaultValue = "10") Integer size,
                           @RequestParam(required = false) Integer status,
                           @RequestParam(required = false) String startDate,
                           @RequestParam(required = false) String endDate) {
        // 获取当前学生ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = ((com.hhjt.entity.User) authentication.getPrincipal()).getId();
        Student student = studentMapper.selectByUserId(userId);

        IPage<SportRecord> recordPage = sportRecordService.getRecordPage(
            page, size, student.getId(), status, startDate, endDate, null, false
        );

        model.addAttribute("page", recordPage);
        model.addAttribute("status", status);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "student/sport_records";
    }

    /**
     * 获取打卡统计
     */
    @GetMapping("/stats")
    @ResponseBody
    public Map<String, Object> getStats() {
        Map<String, Object> result = new HashMap<>();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = ((com.hhjt.entity.User) authentication.getPrincipal()).getId();
            Student student = studentMapper.selectByUserId(userId);

            Map<String, Object> stats = sportRecordMapper.getStudentSportStats(student.getId());
            result.put("success", true);
            result.put("data", stats);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }
}
