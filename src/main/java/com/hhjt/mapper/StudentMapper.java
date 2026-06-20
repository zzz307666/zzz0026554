package com.hhjt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hhjt.entity.Student;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {

    // 根据userId删除学生记录
    @Delete("DELETE FROM sys_student WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    // 根据userId查询学生记录
    @Select("SELECT * FROM sys_student WHERE user_id = #{userId}")
    Student selectByUserId(@Param("userId") Long userId);

    // 根据班级ID统计学生数量
    @Select("SELECT COUNT(*) FROM sys_student WHERE class_id = #{classId}")
    int countByClassId(@Param("classId") Long classId);

    // 根据班级ID查询学生列表（含用户真实姓名）
    @Select("SELECT s.*, u.real_name as realName FROM sys_student s " +
            "INNER JOIN sys_user u ON s.user_id = u.id " +
            "WHERE s.class_id = #{classId}")
    List<Student> selectByClassIdWithRealName(@Param("classId") Long classId);
}