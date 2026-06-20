package com.hhjt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hhjt.entity.StudentPoints;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface StudentPointsMapper extends BaseMapper<StudentPoints> {
    
    /**
     * 查询学生总积分（包含所有积分变动）
     */
    @Select("SELECT COALESCE(SUM(points_value), 0) FROM student_points WHERE student_id = #{studentId}")
    BigDecimal getStudentTotalPoints(Long studentId);
    
    /**
     * 查询学生累计获得积分（仅正向积分，用于排名）
     */
    @Select("SELECT COALESCE(SUM(CASE WHEN points_value > 0 THEN points_value ELSE 0 END), 0) " +
            "FROM student_points WHERE student_id = #{studentId}")
    BigDecimal getStudentEarnedPoints(Long studentId);
}
