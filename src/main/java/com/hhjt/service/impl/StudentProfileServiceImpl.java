package com.hhjt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hhjt.entity.SportRecord;
import com.hhjt.entity.Student;
import com.hhjt.entity.StudentEvaluation;
import com.hhjt.entity.StudentPoints;
import com.hhjt.entity.SysClass;
import com.hhjt.entity.User;
import com.hhjt.mapper.ClassMapper;
import com.hhjt.mapper.SportRecordMapper;
import com.hhjt.mapper.StudentEvaluationMapper;
import com.hhjt.mapper.StudentMapper;
import com.hhjt.mapper.StudentPointsMapper;
import com.hhjt.mapper.UserMapper;
import com.hhjt.service.StudentProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学生成长档案服务实现类 - 真实数据版本
 */
@Slf4j
@Service
public class StudentProfileServiceImpl implements StudentProfileService {

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private SportRecordMapper sportRecordMapper;

    @Autowired
    private StudentPointsMapper pointsMapper;

    @Autowired
    private StudentEvaluationMapper evaluationMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private ClassMapper classMapper;

    @Override
    public Map<String, Object> getStudentBasicInfo(Long studentId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 查询学生基本信息
            Student student = studentMapper.selectById(studentId);
            if (student == null) {
                return result;
            }
            
            // 获取关联的User信息
            User user = userMapper.selectById(student.getUserId());
            
            result.put("studentId", student.getId());
            result.put("studentNo", student.getStudentNo());
            result.put("studentName", user != null ? user.getRealName() : "未知");
            
            // 获取班级名称
            if (student.getClassId() != null) {
                SysClass sysClass = classMapper.selectById(student.getClassId());
                result.put("className", sysClass != null ? sysClass.getClassName() : "未知");
            } else {
                result.put("className", "未知");
            }
            
            result.put("gender", student.getGender() != null ? student.getGender() : "未知");
            result.put("birthDate", student.getBirthDate() != null ? student.getBirthDate().toString() : "");
            result.put("phone", user != null && user.getPhone() != null ? user.getPhone() : "");
            result.put("email", user != null && user.getEmail() != null ? user.getEmail() : "");
            result.put("enrollmentDate", ""); // Student实体中没有入学日期字段
            
            // 统计积分（累加所有积分值）
            QueryWrapper<StudentPoints> pointsWrapper = new QueryWrapper<>();
            pointsWrapper.eq("student_id", studentId);
            List<StudentPoints> pointsList = pointsMapper.selectList(pointsWrapper);
            double totalPoints = pointsList.stream()
                .mapToDouble(p -> p.getPointsValue() != null ? p.getPointsValue().doubleValue() : 0)
                .sum();
            result.put("totalPoints", totalPoints);
            
            // 统计运动记录
            QueryWrapper<SportRecord> recordWrapper = new QueryWrapper<>();
            recordWrapper.eq("student_id", studentId);
            long totalRecords = sportRecordMapper.selectCount(recordWrapper);
            result.put("totalSportRecords", totalRecords);
            
            // 计算平均时长
            List<SportRecord> records = sportRecordMapper.selectList(recordWrapper);
            double avgDuration = records.isEmpty() ? 0 :
                records.stream()
                    .mapToDouble(r -> r.getDuration() != null ? r.getDuration() : 0)
                    .average()
                    .orElse(0);
            result.put("avgDuration", Math.round(avgDuration * 10.0) / 10.0);
            
        } catch (Exception e) {
            log.error("获取学生基本信息失败", e);
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getSportRecordTimeline(Long studentId) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            // 查询最近20条运动记录
            QueryWrapper<SportRecord> wrapper = new QueryWrapper<>();
            wrapper.eq("student_id", studentId)
                   .orderByDesc("record_date")
                   .last("LIMIT 20");
            
            List<SportRecord> records = sportRecordMapper.selectList(wrapper);
            
            for (SportRecord record : records) {
                Map<String, Object> recordData = new HashMap<>();
                recordData.put("date", record.getRecordDate() != null ? record.getRecordDate().toString() : "");
                recordData.put("sportTypeId", record.getSportTypeId());
                recordData.put("duration", record.getDuration());
                recordData.put("distance", record.getDistance());
                recordData.put("calories", record.getCalories());
                recordData.put("points", record.getEarnedPoints());
                result.add(recordData);
            }
        } catch (Exception e) {
            log.error("获取运动记录时间线失败", e);
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getPointsChangeCurve(Long studentId) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            // 获取最近6个月的积分变化
            LocalDate now = LocalDate.now();
            
            for (int i = 5; i >= 0; i--) {
                LocalDate firstDayOfMonth = now.minusMonths(i).withDayOfMonth(1);
                
                // 查询该学生的累计积分
                QueryWrapper<StudentPoints> wrapper = new QueryWrapper<>();
                wrapper.eq("student_id", studentId);
                List<StudentPoints> pointsList = pointsMapper.selectList(wrapper);
                
                double totalPoints = pointsList.stream()
                    .mapToDouble(p -> p.getPointsValue() != null ? p.getPointsValue().doubleValue() : 0)
                    .sum();
                
                Map<String, Object> data = new HashMap<>();
                data.put("month", firstDayOfMonth.toString().substring(0, 7)); // yyyy-MM
                data.put("points", totalPoints);
                result.add(data);
            }
        } catch (Exception e) {
            log.error("获取积分变化曲线失败", e);
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getEvaluationRadarData(Long studentId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 查询学生的评价记录
            QueryWrapper<StudentEvaluation> wrapper = new QueryWrapper<>();
            wrapper.eq("student_id", studentId)
                   .orderByDesc("create_time")
                   .last("LIMIT 3");
            
            List<StudentEvaluation> evaluations = evaluationMapper.selectList(wrapper);
            
            List<Map<String, Object>> evalList = new ArrayList<>();
            Map<String, Object> latestEval = null;
            
            for (StudentEvaluation eval : evaluations) {
                Map<String, Object> evalData = new HashMap<>();
                evalData.put("period", eval.getEvaluationPeriod() != null ? eval.getEvaluationPeriod() : "");
                evalData.put("endurance", eval.getEnduranceScore() != null ? eval.getEnduranceScore() : 0);
                evalData.put("strength", eval.getStrengthScore() != null ? eval.getStrengthScore() : 0);
                evalData.put("speed", eval.getSpeedScore() != null ? eval.getSpeedScore() : 0);
                evalData.put("flexibility", eval.getFlexibilityScore() != null ? eval.getFlexibilityScore() : 0);
                evalData.put("coordination", eval.getCoordinationScore() != null ? eval.getCoordinationScore() : 0);
                evalData.put("totalScore", eval.getTotalScore() != null ? eval.getTotalScore() : 0);
                
                evalList.add(evalData);
                
                if (latestEval == null) {
                    latestEval = evalData;
                }
            }
            
            result.put("evaluations", evalList);
            result.put("latestEval", latestEval);
            
        } catch (Exception e) {
            log.error("获取评价雷达图数据失败", e);
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getGrowthTrendAnalysis(Long studentId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 统计总运动天数
            QueryWrapper<SportRecord> allWrapper = new QueryWrapper<>();
            allWrapper.eq("student_id", studentId);
            List<SportRecord> allRecords = sportRecordMapper.selectList(allWrapper);
            
            Set<LocalDate> uniqueDates = allRecords.stream()
                .filter(r -> r.getRecordDate() != null)
                .map(SportRecord::getRecordDate)
                .collect(Collectors.toSet());
            
            int totalSportDays = uniqueDates.size();
            result.put("totalSportDays", totalSportDays);
            
            // 计算连续打卡天数（简化处理）
            int consecutiveDays = calculateConsecutiveDays(uniqueDates);
            result.put("consecutiveDays", consecutiveDays);
            
            // 找出最喜欢的运动类型（按sportTypeId统计）
            Map<Long, Long> typeIdCountMap = allRecords.stream()
                .filter(r -> r.getSportTypeId() != null)
                .collect(Collectors.groupingBy(
                    SportRecord::getSportTypeId,
                    Collectors.counting()
                ));
            
            String favoriteSport = typeIdCountMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> "运动类型ID: " + entry.getKey())
                .orElse("无数据");
            result.put("favoriteSport", favoriteSport);
            
            // 计算运动频率
            if (!allRecords.isEmpty() && !uniqueDates.isEmpty()) {
                LocalDate earliestDate = uniqueDates.stream().min(LocalDate::compareTo).orElse(LocalDate.now());
                LocalDate latestDate = uniqueDates.stream().max(LocalDate::compareTo).orElse(LocalDate.now());
                long daysSpan = java.time.temporal.ChronoUnit.DAYS.between(earliestDate, latestDate) + 1;
                double frequency = daysSpan > 0 ? (totalSportDays * 7.0 / daysSpan) : 0;
                result.put("sportFrequency", String.format("每周%.1f次", frequency));
            } else {
                result.put("sportFrequency", "每周0次");
            }
            
            // 计算每周平均时长
            double totalDuration = allRecords.stream()
                .mapToDouble(r -> r.getDuration() != null ? r.getDuration() : 0)
                .sum();
            double weeksSpan = totalSportDays > 0 ? (uniqueDates.stream()
                .reduce((first, second) -> second)
                .orElse(LocalDate.now())
                .toEpochDay() - uniqueDates.stream()
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now())
                .toEpochDay()) / 7.0 : 1;
            
            result.put("avgDurationPerWeek", Math.round(totalDuration / Math.max(weeksSpan, 1)));
            
            // 班级排名
            Student student = studentMapper.selectById(studentId);
            if (student != null && student.getClassId() != null) {
                QueryWrapper<StudentPoints> classPointsWrapper = new QueryWrapper<>();
                classPointsWrapper.in("student_id", 
                    studentMapper.selectList(new QueryWrapper<Student>()
                        .eq("class_id", student.getClassId()))
                        .stream()
                        .map(Student::getId)
                        .collect(Collectors.toList())
                );
                
                List<StudentPoints> classPointsList = pointsMapper.selectList(classPointsWrapper);
                
                // 计算每个学生的总积分
                Map<Long, Double> studentPointsMap = new HashMap<>();
                for (StudentPoints p : classPointsList) {
                    studentPointsMap.merge(p.getStudentId(), 
                        p.getPointsValue() != null ? p.getPointsValue().doubleValue() : 0, 
                        Double::sum);
                }
                
                // 排序并找到当前学生的排名
                List<Map.Entry<Long, Double>> sortedStudents = studentPointsMap.entrySet().stream()
                    .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                    .collect(Collectors.toList());
                
                long rank = 1;
                for (int i = 0; i < sortedStudents.size(); i++) {
                    if (sortedStudents.get(i).getKey().equals(studentId)) {
                        rank = i + 1;
                        break;
                    }
                }
                
                result.put("rankInClass", rank);
                result.put("totalStudentsInClass", sortedStudents.size());
            }
            
            // 成长建议
            List<String> suggestions = generateSuggestions(allRecords, favoriteSport);
            result.put("suggestions", suggestions);
            
        } catch (Exception e) {
            log.error("获取成长趋势分析失败", e);
        }
        
        return result;
    }
    
    /**
     * 计算连续打卡天数
     */
    private int calculateConsecutiveDays(Set<LocalDate> dates) {
        if (dates.isEmpty()) return 0;
        
        List<LocalDate> sortedDates = dates.stream()
            .sorted(LocalDate::compareTo)
            .collect(Collectors.toList());
        
        int maxStreak = 1;
        int currentStreak = 1;
        
        for (int i = 1; i < sortedDates.size(); i++) {
            if (sortedDates.get(i).minusDays(1).equals(sortedDates.get(i - 1))) {
                currentStreak++;
                maxStreak = Math.max(maxStreak, currentStreak);
            } else {
                currentStreak = 1;
            }
        }
        
        return maxStreak;
    }
    
    /**
     * 生成成长建议
     */
    private List<String> generateSuggestions(List<SportRecord> records, String favoriteSport) {
        List<String> suggestions = new ArrayList<>();
        
        if (records.isEmpty()) {
            suggestions.add("开始你的第一次运动吧！");
            return suggestions;
        }
        
        // 根据运动类型分布给出建议
        Map<Long, Long> typeIdCount = records.stream()
            .filter(r -> r.getSportTypeId() != null)
            .collect(Collectors.groupingBy(SportRecord::getSportTypeId, Collectors.counting()));
        
        if (typeIdCount.size() <= 2) {
            suggestions.add("运动类型较为单一，建议尝试更多样化的运动");
        }
        
        // 根据平均时长给出建议
        double avgDuration = records.stream()
            .mapToDouble(r -> r.getDuration() != null ? r.getDuration() : 0)
            .average()
            .orElse(0);
        
        if (avgDuration < 30) {
            suggestions.add("每次运动时长较短，建议延长至30分钟以上");
        }
        
        suggestions.add("保持良好的运动习惯，定期参加体能测试");
        
        return suggestions;
    }
}
