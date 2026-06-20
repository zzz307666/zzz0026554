package com.hhjt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hhjt.entity.Teacher;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TeacherMapper extends BaseMapper<Teacher> {

    // 根据userId删除教师记录
    @Delete("DELETE FROM sys_teacher WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    // 根据userId查询教师记录
    @Select("SELECT * FROM sys_teacher WHERE user_id = #{userId}")
    Teacher selectByUserId(@Param("userId") Long userId);
}