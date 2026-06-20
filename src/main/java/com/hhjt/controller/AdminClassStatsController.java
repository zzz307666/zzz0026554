package com.hhjt.controller;

import com.hhjt.entity.SportRecord;
import com.hhjt.entity.Student;
import com.hhjt.entity.SysClass;
import com.hhjt.entity.StudentPoints;
import com.hhjt.mapper.ClassMapper;
import com.hhjt.mapper.SportRecordMapper;
import com.hhjt.mapper.StudentMapper;
import com.hhjt.mapper.StudentPointsMapper;
import com.hhjt.service.ClassStatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理员 - 班级统计控制器
 */
@Slf4j
@Controller
@RequestMapping("/admin/class-stats")
public class AdminClassStatsController {

    @Autowired
    private ClassStatsService classStatsService;

    @Autowired
    private ClassMapper classMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private SportRecordMapper sportRecordMapper;

    @Autowired
    private StudentPointsMapper pointsMapper;

    /**
     * 班级统计页面
     */
    @GetMapping("/dashboard")
    public String classStatsDashboard(Model model) {
        return "admin/class_stats";
    }

    /**
     * 获取全校总体统计数据
     */
    @GetMapping("/overview")
    @ResponseBody
    public Map<String, Object> getOverview() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 班级总数
            int totalClasses = classMapper.selectCount(null).intValue();
            result.put("totalClasses", totalClasses);

            // 学生总数
            int totalStudents = studentMapper.selectCount(null).intValue();
            result.put("totalStudents", totalStudents);

            // 活跃学生数（近30天有运动记录的学生）
            LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
            List<SportRecord> recentRecords = sportRecordMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SportRecord>()
                    .ge(SportRecord::getRecordDate, thirtyDaysAgo)
            );
            long activeStudents = recentRecords.stream()
                .map(SportRecord::getStudentId)
                .distinct()
                .count();
            result.put("activeStudents", activeStudents);

            // 参与率
            double participationRate = totalStudents > 0 ?
                Math.round(activeStudents * 10000.0 / totalStudents) / 100.0 : 0;
            result.put("participationRate", participationRate);

            // 运动记录总数
            long totalRecords = sportRecordMapper.selectCount(null);
            result.put("totalRecords", totalRecords);

            // 本月新增记录数
            LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
            long monthRecords = sportRecordMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SportRecord>()
                    .ge(SportRecord::getRecordDate, firstDayOfMonth)
            );
            result.put("monthRecords", monthRecords);

            // 计算参与率变化（与上月对比）
            LocalDate twoMonthsAgo = LocalDate.now().minusMonths(1).withDayOfMonth(1);
            LocalDate lastMonthEnd = firstDayOfMonth.minusDays(1);

            List<SportRecord> lastMonthRecords = sportRecordMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SportRecord>()
                    .ge(SportRecord::getRecordDate, twoMonthsAgo)
                    .lt(SportRecord::getRecordDate, firstDayOfMonth)
            );
            long lastMonthActiveStudents = lastMonthRecords.stream()
                .map(SportRecord::getStudentId)
                .distinct()
                .count();
            double lastMonthRate = totalStudents > 0 ?
                lastMonthActiveStudents * 100.0 / totalStudents : 0;
            double rateChange = Math.round((participationRate - lastMonthRate) * 100.0) / 100.0;
            result.put("rateChange", rateChange);

            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("获取总体统计失败", e);
        }
        return result;
    }

    /**
     * 获取班级参与率数据
     */
    @GetMapping("/participation-rate")
    @ResponseBody
    public Map<String, Object> getParticipationRate() {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("success", true);
            result.put("data", classStatsService.getClassParticipationRate());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("获取参与率失败", e);
        }
        return result;
    }

    /**
     * 获取班级积分排名
     */
    @GetMapping("/points-ranking")
    @ResponseBody
    public Map<String, Object> getPointsRanking() {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("success", true);
            result.put("data", classStatsService.getClassPointsRanking());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("获取积分排名失败", e);
        }
        return result;
    }

    /**
     * 获取运动类型分布
     */
    @GetMapping("/sport-distribution")
    @ResponseBody
    public Map<String, Object> getSportDistribution() {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("success", true);
            result.put("data", classStatsService.getSportTypeDistribution());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("获取运动分布失败", e);
        }
        return result;
    }

    /**
     * 获取时间趋势分析
     */
    @GetMapping("/time-trend")
    @ResponseBody
    public Map<String, Object> getTimeTrend(@RequestParam(defaultValue = "semester") String period) {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("success", true);
            result.put("data", classStatsService.getTimeTrendAnalysis(period));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("获取趋势分析失败", e);
        }
        return result;
    }

    /**
     * 获取班级详细统计
     */
    @GetMapping("/detail/{classId}")
    @ResponseBody
    public Map<String, Object> getClassDetail(@PathVariable Long classId) {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("success", true);
            result.put("data", classStatsService.getClassDetailStats(classId));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("获取班级详情失败", e);
        }
        return result;
    }

    /**
     * 获取所有班级详细统计列表（用于表格展示）
     */
    @GetMapping("/all-class-stats")
    @ResponseBody
    public Map<String, Object> getAllClassStats() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 获取所有班级
            List<SysClass> classes = classMapper.selectList(null);
            List<Map<String, Object>> classStatsList = new ArrayList<>();
            
            for (int i = 0; i < classes.size(); i++) {
                SysClass sysClass = classes.get(i);
                Map<String, Object> stats = classStatsService.getClassDetailStats(sysClass.getId());
                stats.put("rank", i + 1); // 添加排名
                classStatsList.add(stats);
            }
            
            // 按平均积分降序排序
            classStatsList.sort((a, b) -> Double.compare(
                ((Number) b.getOrDefault("avgPoints", 0)).doubleValue(),
                ((Number) a.getOrDefault("avgPoints", 0)).doubleValue()
            ));
            
            // 重新设置排名
            for (int i = 0; i < classStatsList.size(); i++) {
                classStatsList.get(i).put("rank", i + 1);
            }
            
            result.put("success", true);
            result.put("data", classStatsList);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("获取所有班级统计失败", e);
        }
        return result;
    }
}
