package com.hhjt.controller;

import com.hhjt.entity.SportRecord;
import com.hhjt.entity.Student;
import com.hhjt.entity.SysClass;
import com.hhjt.entity.Teacher;
import com.hhjt.entity.User;
import com.hhjt.mapper.SportRecordMapper;
import com.hhjt.mapper.StudentMapper;
import com.hhjt.mapper.TeacherMapper;
import com.hhjt.service.ClassService;
import com.hhjt.service.TeacherClassService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 教师端 - 班级数据统计控制器
 */
@Slf4j
@Controller
@RequestMapping("/teacher/class")
public class TeacherClassStatsController {

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private ClassService classService;

    @Autowired
    private TeacherClassService teacherClassService;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private SportRecordMapper sportRecordMapper;
    
    @Autowired
    private com.hhjt.mapper.UserMapper userMapper;
    
    @Autowired
    private com.hhjt.mapper.SportTypeMapper sportTypeMapper;

    /**
     * 班级统计页面
     */
    @GetMapping("/stats")
    public String classStats(Model model) {
        // 获取当前教师信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = ((User) authentication.getPrincipal()).getId();
        Teacher teacher = teacherMapper.selectByUserId(userId);

        // 获取教师负责的班级列表（管理员可以查看所有班级）
        List<Long> classIds;
        if (teacher != null) {
            classIds = teacherClassService.getClassIdByTeacherId(teacher.getId());
        } else {
            // 管理员没有教师记录，查询所有班级
            classIds = null; // null表示查询所有
        }
        
        List<SysClass> classes;
        if (classIds != null && !classIds.isEmpty()) {
            classes = classService.getClassByIds(classIds);
        } else {
            // 查询所有班级
            classes = classService.getAllClass();
        }

        model.addAttribute("classes", classes);
        model.addAttribute("teacher", teacher);
        
        // 判断是否为管理员
        boolean isAdmin = (teacher == null); // 如果没有教师记录，说明是管理员
        model.addAttribute("isAdmin", isAdmin);

        return "teacher/class_stats";
    }

    /**
     * 获取班级统计数据
     */
    @GetMapping("/stats/data")
    @ResponseBody
    public Map<String, Object> getClassStatsData(@RequestParam Long classId, @RequestParam(required = false) String month) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 获取班级学生列表
            List<Student> students = studentMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Student>()
                    .eq(Student::getClassId, classId)
            );

            // 统计每个学生的运动数据
            List<Map<String, Object>> studentStats = new ArrayList<>();
            for (Student student : students) {
                Map<String, Object> stats;
                if (month != null && !month.isEmpty()) {
                    stats = sportRecordMapper.getStudentSportStatsByMonth(student.getId(), month);
                } else {
                    stats = sportRecordMapper.getStudentSportStats(student.getId());
                }
                Map<String, Object> studentData = new HashMap<>();
                studentData.put("studentId", student.getId());
                studentData.put("studentName", getStudentName(student.getUserId()));
                studentData.put("studentNo", student.getStudentNo());
                studentData.put("totalCount", stats.get("total_count"));
                studentData.put("totalDuration", stats.get("total_duration"));
                studentData.put("totalCalories", stats.get("total_calories"));
                studentData.put("totalPoints", stats.get("total_points"));
                studentStats.add(studentData);
            }

            // 按积分排序
            studentStats.sort((a, b) -> {
                double pointsA = Double.parseDouble(a.get("totalPoints").toString());
                double pointsB = Double.parseDouble(b.get("totalPoints").toString());
                return Double.compare(pointsB, pointsA);
            });

            result.put("success", true);
            result.put("data", studentStats);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取数据失败：" + e.getMessage());
            log.error("获取班级统计数据异常", e);
        }
        return result;
    }

    /**
     * 获取班级平均分对比数据
     */
    @GetMapping("/stats/average")
    @ResponseBody
    public Map<String, Object> getClassAverageStats(@RequestParam List<Long> classIds) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Map<String, Object>> averageData = new ArrayList<>();

            for (Long classId : classIds) {
                SysClass sysClass = classService.getClassById(classId);
                List<Student> students = studentMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Student>()
                        .eq(Student::getClassId, classId)
                );

                double totalPoints = 0;
                int studentCount = students.size();

                for (Student student : students) {
                    Map<String, Object> stats = sportRecordMapper.getStudentSportStats(student.getId());
                    totalPoints += Double.parseDouble(stats.get("total_points").toString());
                }

                double avgPoints = studentCount > 0 ? totalPoints / studentCount : 0;

                Map<String, Object> classData = new HashMap<>();
                classData.put("className", sysClass.getClassName());
                classData.put("avgPoints", Math.round(avgPoints * 100.0) / 100.0);
                classData.put("studentCount", studentCount);
                averageData.add(classData);
            }

            result.put("success", true);
            result.put("data", averageData);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取平均分数据失败：" + e.getMessage());
            log.error("获取平均分数据异常", e);
        }
        return result;
    }

    /**
     * 获取运动类型分布数据
     */
    @GetMapping("/stats/sport-distribution")
    @ResponseBody
    public Map<String, Object> getSportTypeDistribution(@RequestParam Long classId, @RequestParam(required = false) String month) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Student> students = studentMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Student>()
                    .eq(Student::getClassId, classId)
            );

            Map<String, Integer> typeCount = new HashMap<>();

            for (Student student : students) {
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SportRecord> queryWrapper = 
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SportRecord>()
                        .eq(SportRecord::getStudentId, student.getId())
                        .eq(SportRecord::getStatus, 1);
                
                // 如果有月份参数，添加时间筛选
                if (month != null && !month.isEmpty()) {
                    String startDate = month + "-01 00:00:00";
                    String endDate = month + "-31 23:59:59";
                    queryWrapper.between(SportRecord::getCreateTime, startDate, endDate);
                }
                
                List<SportRecord> records = sportRecordMapper.selectList(queryWrapper);

                for (SportRecord record : records) {
                    String typeName = getSportTypeName(record.getSportTypeId());
                    typeCount.merge(typeName, 1, Integer::sum);
                }
            }

            result.put("success", true);
            result.put("data", typeCount);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取分布数据失败：" + e.getMessage());
            log.error("获取分布数据异常", e);
        }
        return result;
    }

    // 辅助方法：获取学生姓名
    private String getStudentName(Long userId) {
        if (userId == null) return "未知";
        User user = userMapper.selectById(userId);
        return user != null ? user.getRealName() : "未知";
    }

    // 辅助方法：获取运动类型名称
    private String getSportTypeName(Long sportTypeId) {
        if (sportTypeId == null) return "未知";
        com.hhjt.entity.SportType sportType = sportTypeMapper.selectById(sportTypeId);
        return sportType != null ? sportType.getTypeName() : "未知";
    }
}