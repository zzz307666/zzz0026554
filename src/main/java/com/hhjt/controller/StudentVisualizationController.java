package com.hhjt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hhjt.entity.SportRecord;
import com.hhjt.entity.Student;
import com.hhjt.entity.StudentPoints;
import com.hhjt.entity.User;
import com.hhjt.mapper.SportRecordMapper;
import com.hhjt.mapper.StudentMapper;
import com.hhjt.mapper.StudentPointsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学生端 - 个人数据可视化控制器
 */
@Slf4j
@Controller
@RequestMapping("/student/visualization")
public class StudentVisualizationController {

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private SportRecordMapper sportRecordMapper;

    @Autowired
    private StudentPointsMapper studentPointsMapper;

    /**
     * 个人数据可视化页面
     */
    @GetMapping("")
    public String visualization(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = ((User) authentication.getPrincipal()).getId();
        
        Student student = studentMapper.selectOne(
            new LambdaQueryWrapper<Student>()
                .eq(Student::getUserId, userId)
        );
        
        model.addAttribute("student", student);
        return "student/visualization";
    }

    /**
     * 获取运动趋势数据（最近30天）
     */
    @GetMapping("/trend-data")
    @ResponseBody
    public Map<String, Object> getTrendData() {
        Map<String, Object> result = new HashMap<>();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = ((User) authentication.getPrincipal()).getId();
            
            Student student = studentMapper.selectOne(
                new LambdaQueryWrapper<Student>()
                    .eq(Student::getUserId, userId)
            );

            // 获取最近30天的日期
            List<String> dates = new ArrayList<>();
            List<Integer> counts = new ArrayList<>();
            List<Double> durations = new ArrayList<>();
            
            LocalDate today = LocalDate.now();
            for (int i = 29; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                String dateStr = date.format(DateTimeFormatter.ofPattern("MM-dd"));
                dates.add(dateStr);
                
                // 查询当天的运动记录
                List<SportRecord> records = sportRecordMapper.selectList(
                    new LambdaQueryWrapper<SportRecord>()
                        .eq(SportRecord::getStudentId, student.getId())
                        .eq(SportRecord::getStatus, 1)
                        .eq(SportRecord::getRecordDate, date)
                );
                
                counts.add(records.size());
                double totalDuration = records.stream()
                    .mapToDouble(r -> r.getDuration() != null ? r.getDuration() : 0)
                    .sum();
                durations.add(totalDuration);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("dates", dates);
            data.put("counts", counts);
            data.put("durations", durations);

            result.put("success", true);
            result.put("data", data);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取数据失败：" + e.getMessage());
            log.error("获取趋势数据异常", e);
        }
        return result;
    }

    /**
     * 获取运动类型分布数据
     */
    @GetMapping("/sport-type-distribution")
    @ResponseBody
    public Map<String, Object> getSportTypeDistribution() {
        Map<String, Object> result = new HashMap<>();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = ((User) authentication.getPrincipal()).getId();
            
            Student student = studentMapper.selectOne(
                new LambdaQueryWrapper<Student>()
                    .eq(Student::getUserId, userId)
            );

            List<SportRecord> records = sportRecordMapper.selectList(
                new LambdaQueryWrapper<SportRecord>()
                    .eq(SportRecord::getStudentId, student.getId())
                    .eq(SportRecord::getStatus, 1)
            );

            // 按运动类型统计
            Map<Long, Long> typeCount = records.stream()
                .collect(Collectors.groupingBy(
                    SportRecord::getSportTypeId,
                    Collectors.counting()
                ));

            result.put("success", true);
            result.put("data", typeCount);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取数据失败：" + e.getMessage());
            log.error("获取分布数据异常", e);
        }
        return result;
    }

    /**
     * 获取月度统计数据
     */
    @GetMapping("/monthly-stats")
    @ResponseBody
    public Map<String, Object> getMonthlyStats() {
        Map<String, Object> result = new HashMap<>();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = ((User) authentication.getPrincipal()).getId();
            
            Student student = studentMapper.selectOne(
                new LambdaQueryWrapper<Student>()
                    .eq(Student::getUserId, userId)
            );

            Map<String, Object> stats = sportRecordMapper.getStudentSportStats(student.getId());

            result.put("success", true);
            result.put("data", stats);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取数据失败：" + e.getMessage());
            log.error("获取月度统计异常", e);
        }
        return result;
    }

    /**
     * 获取积分增长数据
     */
    @GetMapping("/points-growth")
    @ResponseBody
    public Map<String, Object> getPointsGrowth() {
        Map<String, Object> result = new HashMap<>();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = ((User) authentication.getPrincipal()).getId();
            
            Student student = studentMapper.selectOne(
                new LambdaQueryWrapper<Student>()
                    .eq(Student::getUserId, userId)
            );

            // 这里简化处理，实际应该从student_points表查询
            Map<String, Object> stats = sportRecordMapper.getStudentSportStats(student.getId());

            result.put("success", true);
            result.put("data", stats);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取数据失败：" + e.getMessage());
            log.error("获取积分增长数据异常", e);
        }
        return result;
    }

    /**
     * 获取积分周增长历史（最近4周）
     */
    @GetMapping("/points-weekly-history")
    @ResponseBody
    public Map<String, Object> getPointsWeeklyHistory() {
        Map<String, Object> result = new HashMap<>();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = ((User) authentication.getPrincipal()).getId();
            
            Student student = studentMapper.selectOne(
                new LambdaQueryWrapper<Student>()
                    .eq(Student::getUserId, userId)
            );

            // 获取最近4周的积分数据
            List<String> weeks = new ArrayList<>();
            List<Double> pointsData = new ArrayList<>();
            
            LocalDate today = LocalDate.now();
            double cumulativePoints = 0;
            
            for (int i = 3; i >= 0; i--) {
                LocalDate weekStart = today.minusWeeks(i).with(java.time.DayOfWeek.MONDAY);
                LocalDate weekEnd = weekStart.plusDays(6);
                String weekLabel = "第" + (4 - i) + "周";
                weeks.add(weekLabel);
                
                // 查询该周的积分记录
                List<StudentPoints> weeklyPoints = studentPointsMapper.selectList(
                    new LambdaQueryWrapper<StudentPoints>()
                        .eq(StudentPoints::getStudentId, student.getId())
                        .ge(StudentPoints::getCreateTime, weekStart.atStartOfDay())
                        .lt(StudentPoints::getCreateTime, weekEnd.plusDays(1).atStartOfDay())
                        .gt(StudentPoints::getPointsValue, 0) // 只统计正向积分
                );
                
                double weekPoints = weeklyPoints.stream()
                    .mapToDouble(p -> p.getPointsValue() != null ? p.getPointsValue().doubleValue() : 0)
                    .sum();
                
                cumulativePoints += weekPoints;
                pointsData.add(cumulativePoints);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("weeks", weeks);
            data.put("points", pointsData);

            result.put("success", true);
            result.put("data", data);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取数据失败：" + e.getMessage());
            log.error("获取积分周增长历史异常", e);
        }
        return result;
    }

    /**
     * 获取运动时段分布数据
     */
    @GetMapping("/time-distribution")
    @ResponseBody
    public Map<String, Object> getTimeDistribution() {
        Map<String, Object> result = new HashMap<>();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = ((User) authentication.getPrincipal()).getId();
            
            Student student = studentMapper.selectOne(
                new LambdaQueryWrapper<Student>()
                    .eq(Student::getUserId, userId)
            );

            // 查询所有运动记录
            List<SportRecord> records = sportRecordMapper.selectList(
                new LambdaQueryWrapper<SportRecord>()
                    .eq(SportRecord::getStudentId, student.getId())
                    .eq(SportRecord::getStatus, 1)
            );

            // 按时段统计运动时长
            Map<String, Double> timeSlots = new LinkedHashMap<>();
            timeSlots.put("早晨(6-9点)", 0.0);
            timeSlots.put("上午(9-12点)", 0.0);
            timeSlots.put("中午(12-14点)", 0.0);
            timeSlots.put("下午(14-17点)", 0.0);
            timeSlots.put("傍晚(17-19点)", 0.0);
            timeSlots.put("晚上(19-22点)", 0.0);

            for (SportRecord record : records) {
                if (record.getCreateTime() != null) {
                    int hour = record.getCreateTime().getHour();
                    double duration = record.getDuration() != null ? record.getDuration() : 0;
                    
                    if (hour >= 6 && hour < 9) {
                        timeSlots.merge("早晨(6-9点)", duration, Double::sum);
                    } else if (hour >= 9 && hour < 12) {
                        timeSlots.merge("上午(9-12点)", duration, Double::sum);
                    } else if (hour >= 12 && hour < 14) {
                        timeSlots.merge("中午(12-14点)", duration, Double::sum);
                    } else if (hour >= 14 && hour < 17) {
                        timeSlots.merge("下午(14-17点)", duration, Double::sum);
                    } else if (hour >= 17 && hour < 19) {
                        timeSlots.merge("傍晚(17-19点)", duration, Double::sum);
                    } else if (hour >= 19 && hour < 22) {
                        timeSlots.merge("晚上(19-22点)", duration, Double::sum);
                    }
                }
            }

            result.put("success", true);
            result.put("data", timeSlots);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取数据失败：" + e.getMessage());
            log.error("获取时段分布数据异常", e);
        }
        return result;
    }
}
