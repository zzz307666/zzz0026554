package com.hhjt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hhjt.entity.TeacherClass;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TeacherClassMapper extends BaseMapper<TeacherClass> {

    // 根据教师ID删除所有关联
    @Delete("DELETE FROM sys_teacher_class WHERE teacher_id = #{teacherId}")
    int deleteByTeacherId(@Param("teacherId") Long teacherId);

    // 根据教师ID查询关联班级ID列表
    @Select("SELECT class_id FROM sys_teacher_class WHERE teacher_id = #{teacherId}")
    List<Long> selectClassIdByTeacherId(@Param("teacherId") Long teacherId);
}