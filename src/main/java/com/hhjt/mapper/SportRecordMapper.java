package com.hhjt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hhjt.entity.SportRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface SportRecordMapper extends BaseMapper<SportRecord> {
    
    /**
     * 查询学生运动统计
     */
    @Select("SELECT COUNT(*) as total_count, " +
            "COALESCE(SUM(duration), 0) as total_duration, " +
            "COALESCE(SUM(calories), 0) as total_calories, " +
            "COALESCE(SUM(earned_points), 0) as total_points " +
            "FROM sport_record WHERE student_id = #{studentId} AND status = 1")
    Map<String, Object> getStudentSportStats(Long studentId);
    
    /**
     * 按月份查询学生运动统计
     */
    @Select("SELECT COUNT(*) as total_count, " +
            "COALESCE(SUM(duration), 0) as total_duration, " +
            "COALESCE(SUM(calories), 0) as total_calories, " +
            "COALESCE(SUM(earned_points), 0) as total_points " +
            "FROM sport_record WHERE student_id = #{studentId} AND status = 1 " +
            "AND DATE_FORMAT(create_time, '%Y-%m') = #{month}")
    Map<String, Object> getStudentSportStatsByMonth(Long studentId, String month);
    
    /**
     * 查询班级运动排名
     */
    @Select("SELECT s.id as studentId, u.real_name as studentName, s.student_no as studentNo, " +
            "c.class_name as className, COUNT(sr.id) as recordCount, " +
            "COALESCE(SUM(sr.earned_points), 0) as totalPoints " +
            "FROM sys_student s " +
            "LEFT JOIN sys_user u ON s.user_id = u.id " +
            "LEFT JOIN sys_class c ON s.class_id = c.id " +
            "LEFT JOIN sport_record sr ON s.id = sr.student_id AND sr.status = 1 " +
            "WHERE s.class_id = #{classId} " +
            "GROUP BY s.id, u.real_name, s.student_no, c.class_name " +
            "ORDER BY totalPoints DESC")
    List<Map<String, Object>> getClassRanking(Long classId);
    
    /**
     * 查询全校运动排名
     */
    @Select("SELECT s.id as studentId, u.real_name as studentName, s.student_no as studentNo, " +
            "c.class_name as className, COUNT(sr.id) as recordCount, " +
            "COALESCE(SUM(sr.earned_points), 0) as totalPoints " +
            "FROM sys_student s " +
            "LEFT JOIN sys_user u ON s.user_id = u.id " +
            "LEFT JOIN sys_class c ON s.class_id = c.id " +
            "LEFT JOIN sport_record sr ON s.id = sr.student_id AND sr.status = 1 " +
            "GROUP BY s.id, u.real_name, s.student_no, c.class_name " +
            "ORDER BY totalPoints DESC")
    List<Map<String, Object>> getSchoolRanking();
    
    /**
     * 获取所有班级列表
     */
    @Select("SELECT id, class_name as className FROM sys_class ORDER BY grade, class_name")
    List<Map<String, Object>> getAllClasses();
    
    /**
     * 查询班级积分排名（基于累计获得的总积分，不扣除兑换消耗）
     */
    @Select("SELECT s.id as studentId, u.real_name as studentName, s.student_no as studentNo, " +
            "c.class_name as className, " +
            "COALESCE(SUM(CASE WHEN sp.points_value > 0 THEN sp.points_value ELSE 0 END), 0) as totalPoints " +
            "FROM sys_student s " +
            "LEFT JOIN sys_user u ON s.user_id = u.id " +
            "LEFT JOIN sys_class c ON s.class_id = c.id " +
            "LEFT JOIN student_points sp ON s.id = sp.student_id " +
            "WHERE s.class_id = #{classId} " +
            "GROUP BY s.id, u.real_name, s.student_no, c.class_name " +
            "ORDER BY totalPoints DESC")
    List<Map<String, Object>> getClassPointsRanking(Long classId);
    
    /**
     * 查询全校积分排名（基于累计获得的总积分，不扣除兑换消耗）
     */
    @Select("SELECT s.id as studentId, u.real_name as studentName, s.student_no as studentNo, " +
            "c.class_name as className, " +
            "COALESCE(SUM(CASE WHEN sp.points_value > 0 THEN sp.points_value ELSE 0 END), 0) as totalPoints " +
            "FROM sys_student s " +
            "LEFT JOIN sys_user u ON s.user_id = u.id " +
            "LEFT JOIN sys_class c ON s.class_id = c.id " +
            "LEFT JOIN student_points sp ON s.id = sp.student_id " +
            "GROUP BY s.id, u.real_name, s.student_no, c.class_name " +
            "ORDER BY totalPoints DESC")
    List<Map<String, Object>> getSchoolPointsRanking();
}