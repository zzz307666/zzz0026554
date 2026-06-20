package com.hhjt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hhjt.entity.SportRecord;
import com.hhjt.entity.Student;
import com.hhjt.entity.StudentPoints;
import com.hhjt.entity.User;
import com.hhjt.mapper.SportRecordMapper;
import com.hhjt.mapper.StudentMapper;
import com.hhjt.mapper.StudentPointsMapper;
import com.hhjt.mapper.UserMapper;
import com.hhjt.service.ClassRankingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 班级排行榜服务实现类 - 真实数据版本
 */
@Slf4j
@Service
public class ClassRankingServiceImpl implements ClassRankingService {

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private SportRecordMapper sportRecordMapper;

    @Autowired
    private StudentPointsMapper pointsMapper;
    
    @Autowired
    private UserMapper userMapper;

    @Override
    public List<Map<String, Object>> getPointsRanking(Long classId, String period) {
        // 获取该班级所有学生
        QueryWrapper<Student> studentWrapper = new QueryWrapper<>();
        if (classId != null) {
            studentWrapper.eq("class_id", classId);
        }
        List<Student> students = studentMapper.selectList(studentWrapper);
        
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Student student : students) {
            // 获取学生积分（累加所有积分值）
            QueryWrapper<StudentPoints> pointsWrapper = new QueryWrapper<>();
            pointsWrapper.eq("student_id", student.getId());
            List<StudentPoints> pointsList = pointsMapper.selectList(pointsWrapper);
            
            double totalPoints = pointsList.stream()
                .mapToDouble(p -> p.getPointsValue() != null ? p.getPointsValue().doubleValue() : 0)
                .sum();
            
            // 统计运动次数
            QueryWrapper<SportRecord> recordWrapper = new QueryWrapper<>();
            recordWrapper.eq("student_id", student.getId());
            
            // 根据时间范围过滤
            if ("week".equals(period)) {
                recordWrapper.ge("record_date", LocalDateTime.now().minusWeeks(1));
            } else if ("month".equals(period)) {
                recordWrapper.ge("record_date", LocalDateTime.now().minusMonths(1));
            } else if ("semester".equals(period)) {
                recordWrapper.ge("record_date", LocalDateTime.now().minusMonths(6));
            }
            
            long sportCount = sportRecordMapper.selectCount(recordWrapper);
            
            // 计算平均时长
            List<SportRecord> records = sportRecordMapper.selectList(recordWrapper);
            double avgDuration = records.isEmpty() ? 0 :
                records.stream()
                    .mapToDouble(r -> r.getDuration() != null ? r.getDuration() : 0)
                    .average()
                    .orElse(0);
            
            // 获取学生姓名（从User表）
            User user = userMapper.selectById(student.getUserId());
            String studentName = user != null ? user.getRealName() : "未知";
            
            Map<String, Object> studentData = new HashMap<>();
            studentData.put("rank", 0); // 稍后排序后设置
            studentData.put("studentNo", student.getStudentNo());
            studentData.put("studentName", studentName);
            studentData.put("points", totalPoints);
            studentData.put("sportCount", sportCount);
            studentData.put("avgDuration", Math.round(avgDuration));
            studentData.put("badge", "");
            
            result.add(studentData);
        }
        
        // 按积分降序排序
        result.sort((a, b) -> Double.compare(
            (Double) b.get("points"),
            (Double) a.get("points")
        ));
        
        // 设置排名和徽章
        for (int i = 0; i < result.size(); i++) {
            result.get(i).put("rank", i + 1);
            if (i == 0) {
                result.get(i).put("badge", "🥇");
            } else if (i == 1) {
                result.get(i).put("badge", "🥈");
            } else if (i == 2) {
                result.get(i).put("badge", "🥉");
            }
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getSportCountRanking(Long classId, String period) {
        // 获取该班级所有学生
        QueryWrapper<Student> studentWrapper = new QueryWrapper<>();
        if (classId != null) {
            studentWrapper.eq("class_id", classId);
        }
        List<Student> students = studentMapper.selectList(studentWrapper);
        
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Student student : students) {
            // 统计运动次数
            QueryWrapper<SportRecord> recordWrapper = new QueryWrapper<>();
            recordWrapper.eq("student_id", student.getId());
            
            // 根据时间范围过滤
            if ("week".equals(period)) {
                recordWrapper.ge("record_date", LocalDateTime.now().minusWeeks(1));
            } else if ("month".equals(period)) {
                recordWrapper.ge("record_date", LocalDateTime.now().minusMonths(1));
            } else if ("semester".equals(period)) {
                recordWrapper.ge("record_date", LocalDateTime.now().minusMonths(6));
            }
            
            long sportCount = sportRecordMapper.selectCount(recordWrapper);
            
            // 获取学生积分（累加所有积分值）
            QueryWrapper<StudentPoints> pointsWrapper = new QueryWrapper<>();
            pointsWrapper.eq("student_id", student.getId());
            List<StudentPoints> pointsList = pointsMapper.selectList(pointsWrapper);
            
            double totalPoints = pointsList.stream()
                .mapToDouble(p -> p.getPointsValue() != null ? p.getPointsValue().doubleValue() : 0)
                .sum();
            
            // 计算平均时长
            List<SportRecord> records = sportRecordMapper.selectList(recordWrapper);
            double avgDuration = records.isEmpty() ? 0 :
                records.stream()
                    .mapToDouble(r -> r.getDuration() != null ? r.getDuration() : 0)
                    .average()
                    .orElse(0);
            
            // 获取学生姓名（从User表）
            User user = userMapper.selectById(student.getUserId());
            String studentName = user != null ? user.getRealName() : "未知";
            
            Map<String, Object> studentData = new HashMap<>();
            studentData.put("rank", 0);
            studentData.put("studentNo", student.getStudentNo());
            studentData.put("studentName", studentName);
            studentData.put("sportCount", sportCount);
            studentData.put("points", totalPoints);
            studentData.put("avgDuration", Math.round(avgDuration));
            studentData.put("badge", "");
            
            result.add(studentData);
        }
        
        // 按运动次数降序排序
        result.sort((a, b) -> Long.compare(
            (Long) b.get("sportCount"),
            (Long) a.get("sportCount")
        ));
        
        // 设置排名和徽章
        for (int i = 0; i < result.size(); i++) {
            result.get(i).put("rank", i + 1);
            if (i == 0) {
                result.get(i).put("badge", "🥇");
            } else if (i == 1) {
                result.get(i).put("badge", "🥈");
            } else if (i == 2) {
                result.get(i).put("badge", "🥉");
            }
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getMostImprovedStudents(Long classId, String period) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 定义时间范围
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthAgo = now.minusMonths(1);
        LocalDateTime twoMonthsAgo = now.minusMonths(2);
        
        // 获取最近一个月的运动记录
        QueryWrapper<SportRecord> recentWrapper = new QueryWrapper<>();
        recentWrapper.ge("record_date", oneMonthAgo);
        if (classId != null) {
            recentWrapper.eq("class_id", classId);
        }
        List<SportRecord> recentRecords = sportRecordMapper.selectList(recentWrapper);
        
        // 获取上一个月的运动记录
        QueryWrapper<SportRecord> previousWrapper = new QueryWrapper<>();
        previousWrapper.ge("record_date", twoMonthsAgo);
        previousWrapper.lt("record_date", oneMonthAgo);
        if (classId != null) {
            previousWrapper.eq("class_id", classId);
        }
        List<SportRecord> previousRecords = sportRecordMapper.selectList(previousWrapper);
        
        // 按学生分组统计
        Map<Long, Long> recentCountMap = recentRecords.stream()
            .collect(Collectors.groupingBy(SportRecord::getStudentId, Collectors.counting()));
        
        Map<Long, Long> previousCountMap = previousRecords.stream()
            .collect(Collectors.groupingBy(SportRecord::getStudentId, Collectors.counting()));
        
        // 计算每个学生的进步率
        Map<Long, Double> improvementRateMap = new HashMap<>();
        for (Long studentId : recentCountMap.keySet()) {
            long recentCount = recentCountMap.getOrDefault(studentId, 0L);
            long previousCount = previousCountMap.getOrDefault(studentId, 0L);
            
            double improvementRate;
            if (previousCount > 0) {
                improvementRate = ((recentCount - previousCount) * 100.0 / previousCount);
            } else {
                improvementRate = recentCount > 0 ? 100.0 : 0.0; // 新开始运动的学生进步100%
            }
            improvementRateMap.put(studentId, improvementRate);
        }
        
        // 取进步率最高的前3名
        List<Map.Entry<Long, Double>> topStudents = improvementRateMap.entrySet().stream()
            .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
            .limit(3)
            .collect(Collectors.toList());
        
        for (Map.Entry<Long, Double> entry : topStudents) {
            Long studentId = entry.getKey();
            double improvementRate = entry.getValue();
            long recentCount = recentCountMap.getOrDefault(studentId, 0L);
            long previousCount = previousCountMap.getOrDefault(studentId, 0L);
            
            Student student = studentMapper.selectById(studentId);
            if (student == null) continue;
            
            // 获取学生姓名（从User表）
            User user = userMapper.selectById(student.getUserId());
            String studentName = user != null ? user.getRealName() : "未知";
            
            Map<String, Object> studentData = new HashMap<>();
            studentData.put("studentNo", student.getStudentNo());
            studentData.put("studentName", studentName);
            studentData.put("improvementRate", Math.round(improvementRate * 10.0) / 10.0);
            studentData.put("reason", String.format("上月运动%d次，本月运动%d次", previousCount, recentCount));
            studentData.put("award", "🏆 最佳进步奖");
            
            result.add(studentData);
        }
        
        return result;
    }

    @Override
    public String exportRanking(Long classId, String rankingType, String period) {
        StringBuilder csv = new StringBuilder();
        
        if ("points".equals(rankingType)) {
            csv.append("排名,学号,姓名,积分,运动次数,平均时长(分钟)\n");
            List<Map<String, Object>> ranking = getPointsRanking(classId, period);
            for (Map<String, Object> student : ranking) {
                csv.append(String.format("%d,%s,%s,%.1f,%d,%d\n",
                    student.get("rank"),
                    student.get("studentNo"),
                    student.get("studentName"),
                    student.get("points"),
                    student.get("sportCount"),
                    student.get("avgDuration")
                ));
            }
        } else {
            csv.append("排名,学号,姓名,运动次数,积分,平均时长(分钟)\n");
            List<Map<String, Object>> ranking = getSportCountRanking(classId, period);
            for (Map<String, Object> student : ranking) {
                csv.append(String.format("%d,%s,%s,%d,%.1f,%d\n",
                    student.get("rank"),
                    student.get("studentNo"),
                    student.get("studentName"),
                    student.get("sportCount"),
                    student.get("points"),
                    student.get("avgDuration")
                ));
            }
        }
        
        return csv.toString();
    }
}
