package com.hhjt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hhjt.entity.SportRecord;
import com.hhjt.entity.SportType;
import com.hhjt.entity.Student;
import com.hhjt.entity.StudentPoints;
import com.hhjt.entity.SysClass;
import com.hhjt.entity.User;
import com.hhjt.mapper.ClassMapper;
import com.hhjt.mapper.SportRecordMapper;
import com.hhjt.mapper.SportTypeMapper;
import com.hhjt.mapper.StudentMapper;
import com.hhjt.mapper.StudentPointsMapper;
import com.hhjt.mapper.UserMapper;
import com.hhjt.service.ClassStatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 班级统计服务实现类 - 真实数据版本
 */
@Slf4j
@Service
public class ClassStatsServiceImpl implements ClassStatsService {

    @Autowired
    private ClassMapper classMapper;

    @Autowired
    private SportRecordMapper sportRecordMapper;

    @Autowired
    private StudentPointsMapper pointsMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SportTypeMapper sportTypeMapper;

    @Override
    public List<Map<String, Object>> getClassParticipationRate() {
        // 获取所有班级
        List<SysClass> classes = classMapper.selectList(new QueryWrapper<>());
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (SysClass sysClass : classes) {
            Long classId = sysClass.getId();
            String className = sysClass.getClassName();
            
            // 统计该班级学生总数
            QueryWrapper<Student> studentWrapper = new QueryWrapper<>();
            studentWrapper.eq("class_id", classId);
            int totalStudents = studentMapper.selectCount(studentWrapper).intValue();
            
            // 统计活跃学生数（最近30天有运动记录）
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            QueryWrapper<SportRecord> recordWrapper = new QueryWrapper<>();
            recordWrapper.ge("record_date", thirtyDaysAgo);
            List<SportRecord> allRecords = sportRecordMapper.selectList(recordWrapper);
            
            // 过滤出该班级的活跃学生
            Set<Long> activeStudentIds = new HashSet<>();
            for (SportRecord record : allRecords) {
                Student student = studentMapper.selectById(record.getStudentId());
                if (student != null && student.getClassId() != null && student.getClassId().equals(classId)) {
                    activeStudentIds.add(record.getStudentId());
                }
            }
            int activeStudents = activeStudentIds.size();
            
            // 计算参与率
            double participationRate = totalStudents > 0 ? 
                (activeStudents * 100.0 / totalStudents) : 0;
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("className", className);
            stats.put("totalStudents", totalStudents);
            stats.put("activeStudents", activeStudents);
            stats.put("participationRate", Math.round(participationRate * 10.0) / 10.0);
            
            result.add(stats);
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getClassPointsRanking() {
        // 获取所有班级
        List<SysClass> classes = classMapper.selectList(new QueryWrapper<>());
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (SysClass sysClass : classes) {
            Long classId = sysClass.getId();
            String className = sysClass.getClassName();
            
            // 查询该班级所有学生
            List<Student> students = studentMapper.selectList(
                new QueryWrapper<Student>().eq("class_id", classId)
            );
            
            if (students == null || students.isEmpty()) {
                // 如果班级没有学生，添加默认数据
                Map<String, Object> stats = new HashMap<>();
                stats.put("className", className);
                stats.put("avgPoints", 0.0);
                result.add(stats);
                continue;
            }
            
            // 获取学生ID列表
            List<Long> studentIds = students.stream()
                .map(Student::getId)
                .collect(Collectors.toList());
            
            // 查询该班级所有学生的积分
            QueryWrapper<StudentPoints> pointsWrapper = new QueryWrapper<>();
            pointsWrapper.in("student_id", studentIds);
            
            List<StudentPoints> pointsList = pointsMapper.selectList(pointsWrapper);
            
            // 计算平均积分（累加每个学生的所有积分值）
            Map<Long, Double> studentPointsMap = new HashMap<>();
            for (StudentPoints p : pointsList) {
                studentPointsMap.merge(p.getStudentId(), 
                    p.getPointsValue() != null ? p.getPointsValue().doubleValue() : 0, 
                    Double::sum);
            }
            
            double avgPoints = studentPointsMap.isEmpty() ? 0 : 
                studentPointsMap.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("className", className);
            stats.put("avgPoints", Math.round(avgPoints * 10.0) / 10.0);
            
            result.add(stats);
        }
        
        // 按平均积分降序排序并添加排名
        result.sort((a, b) -> Double.compare(
            (Double) b.get("avgPoints"), 
            (Double) a.get("avgPoints")
        ));
        
        for (int i = 0; i < result.size(); i++) {
            result.get(i).put("rank", i + 1);
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getSportTypeDistribution() {
        // 查询所有运动记录
        List<SportRecord> allRecords = sportRecordMapper.selectList(new QueryWrapper<>());

        // 按运动类型ID分组统计
        Map<Long, Long> typeIdCountMap = allRecords.stream()
            .filter(r -> r.getSportTypeId() != null)
            .collect(Collectors.groupingBy(
                SportRecord::getSportTypeId,
                Collectors.counting()
            ));

        long totalCount = allRecords.size();

        // 获取所有运动类型名称
        Map<Long, String> typeIdNameMap = new HashMap<>();
        List<SportType> allTypes = sportTypeMapper.selectList(null);
        for (SportType type : allTypes) {
            typeIdNameMap.put(type.getId(), type.getTypeName());
        }

        List<Map<String, Object>> distribution = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : typeIdCountMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            Long typeId = entry.getKey();
            item.put("sportTypeId", typeId);
            item.put("sportType", typeIdNameMap.getOrDefault(typeId, "未知"));
            item.put("count", entry.getValue());
            item.put("percentage", totalCount > 0 ?
                Math.round(entry.getValue() * 10000.0 / totalCount) / 100.0 : 0);
            distribution.add(item);
        }

        // 按数量降序排序
        distribution.sort((a, b) -> Long.compare(
            (Long) b.get("count"),
            (Long) a.get("count")
        ));

        Map<String, Object> result = new HashMap<>();
        result.put("distribution", distribution);
        result.put("totalCount", totalCount);

        return result;
    }

    @Override
    public List<Map<String, Object>> getTimeTrendAnalysis(String period) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        if ("week".equals(period)) {
            // 周趋势 - 最近7天
            LocalDate today = LocalDate.now();
            for (int i = 6; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
                
                QueryWrapper<SportRecord> wrapper = new QueryWrapper<>();
                wrapper.ge("record_date", startOfDay)
                       .lt("record_date", endOfDay);
                
                long count = sportRecordMapper.selectCount(wrapper);
                
                Map<String, Object> data = new HashMap<>();
                data.put("label", date.getDayOfWeek().toString());
                data.put("count", count);
                result.add(data);
            }
        } else if ("month".equals(period)) {
            // 月趋势 - 最近30天
            LocalDate today = LocalDate.now();
            for (int i = 29; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
                
                QueryWrapper<SportRecord> wrapper = new QueryWrapper<>();
                wrapper.ge("record_date", startOfDay)
                       .lt("record_date", endOfDay);
                
                long count = sportRecordMapper.selectCount(wrapper);
                
                Map<String, Object> data = new HashMap<>();
                data.put("label", date.getDayOfMonth() + "日");
                data.put("count", count);
                result.add(data);
            }
        } else {
            // 学期趋势 - 最近6个月
            LocalDate today = LocalDate.now();
            for (int i = 5; i >= 0; i--) {
                LocalDate firstDayOfMonth = today.minusMonths(i).withDayOfMonth(1);
                LocalDate firstDayOfNextMonth = firstDayOfMonth.plusMonths(1);
                
                LocalDateTime startOfMonth = firstDayOfMonth.atStartOfDay();
                LocalDateTime startOfNextMonth = firstDayOfNextMonth.atStartOfDay();
                
                QueryWrapper<SportRecord> wrapper = new QueryWrapper<>();
                wrapper.ge("record_date", startOfMonth)
                       .lt("record_date", startOfNextMonth);
                
                long count = sportRecordMapper.selectCount(wrapper);
                
                Map<String, Object> data = new HashMap<>();
                data.put("label", firstDayOfMonth.getMonthValue() + "月");
                data.put("count", count);
                result.add(data);
            }
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getClassDetailStats(Long classId) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取班级信息
        SysClass classInfo = classMapper.selectById(classId);
        if (classInfo == null) {
            return result;
        }
        
        String className = classInfo.getClassName();
        
        // 统计学生总数
        QueryWrapper<Student> studentWrapper = new QueryWrapper<>();
        studentWrapper.eq("class_id", classId);
        int totalStudents = studentMapper.selectCount(studentWrapper).intValue();
        
        // 统计活跃学生数
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        QueryWrapper<SportRecord> recordWrapper = new QueryWrapper<>();
        recordWrapper.ge("record_date", thirtyDaysAgo);
        List<SportRecord> allRecords = sportRecordMapper.selectList(recordWrapper);
        
        Set<Long> activeStudentIds = new HashSet<>();
        for (SportRecord record : allRecords) {
            Student student = studentMapper.selectById(record.getStudentId());
            if (student != null && student.getClassId() != null && student.getClassId().equals(classId)) {
                activeStudentIds.add(record.getStudentId());
            }
        }
        int activeStudents = activeStudentIds.size();
        
        // 计算参与率
        double participationRate = totalStudents > 0 ? 
            (activeStudents * 100.0 / totalStudents) : 0;
        
        // 计算平均积分
        List<Student> students = studentMapper.selectList(studentWrapper);
        List<Long> studentIds = students.stream()
            .map(Student::getId)
            .collect(Collectors.toList());
        
        double avgPoints = 0;
        if (!studentIds.isEmpty()) {
            QueryWrapper<StudentPoints> pointsWrapper = new QueryWrapper<>();
            pointsWrapper.in("student_id", studentIds);
            List<StudentPoints> pointsList = pointsMapper.selectList(pointsWrapper);
            
            // 累加每个学生的积分
            Map<Long, Double> studentPointsMap = new HashMap<>();
            for (StudentPoints p : pointsList) {
                studentPointsMap.merge(p.getStudentId(), 
                    p.getPointsValue() != null ? p.getPointsValue().doubleValue() : 0, 
                    Double::sum);
            }
            
            avgPoints = studentPointsMap.isEmpty() ? 0 : 
                studentPointsMap.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0);
        }
        
        // 统计运动记录总数（通过学生ID关联）
        long totalSportRecords = 0;
        if (!studentIds.isEmpty()) {
            QueryWrapper<SportRecord> classRecordWrapper = new QueryWrapper<>();
            classRecordWrapper.in("student_id", studentIds);
            totalSportRecords = sportRecordMapper.selectCount(classRecordWrapper);
        }
        
        // 计算平均时长
        List<SportRecord> classRecords = new ArrayList<>();
        if (!studentIds.isEmpty()) {
            QueryWrapper<SportRecord> classRecordWrapper = new QueryWrapper<>();
            classRecordWrapper.in("student_id", studentIds);
            classRecords = sportRecordMapper.selectList(classRecordWrapper);
        }
        double avgDuration = classRecords.isEmpty() ? 0 :
            classRecords.stream()
                .mapToDouble(r -> r.getDuration() != null ? r.getDuration() : 0)
                .average()
                .orElse(0);
        
        // 找出最受欢迎的运动类型
        Map<Long, Long> typeIdCountMap = classRecords.stream()
            .filter(r -> r.getSportTypeId() != null)
            .collect(Collectors.groupingBy(
                SportRecord::getSportTypeId,
                Collectors.counting()
            ));
        
        String topSportType = "无数据";
        if (!typeIdCountMap.isEmpty()) {
            Long topTypeId = typeIdCountMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
            
            if (topTypeId != null) {
                SportType sportType = sportTypeMapper.selectById(topTypeId);
                if (sportType != null) {
                    topSportType = sportType.getTypeName();
                } else {
                    topSportType = "未知运动";
                }
            }
        }
        
        result.put("className", className);
        result.put("classId", classId);
        result.put("totalStudents", totalStudents);
        result.put("activeStudents", activeStudents);
        result.put("participationRate", Math.round(participationRate * 10.0) / 10.0);
        result.put("avgPoints", Math.round(avgPoints * 10.0) / 10.0);
        result.put("totalSportRecords", totalSportRecords);
        result.put("avgDuration", Math.round(avgDuration * 10.0) / 10.0);
        result.put("topSportType", topSportType);
        
        return result;
    }
}
