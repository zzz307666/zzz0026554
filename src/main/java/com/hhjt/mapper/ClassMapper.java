package com.hhjt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hhjt.entity.SysClass;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ClassMapper extends BaseMapper<SysClass> {

    // 根据教师ID查询任职班级
    @Select("SELECT c.* FROM sys_class c " +
            "INNER JOIN sys_teacher_class tc ON c.id = tc.class_id " +
            "WHERE tc.teacher_id = #{teacherId}")
    List<SysClass> selectClassByTeacherId(@Param("teacherId") Long teacherId);

    // 根据班级ID列表批量查询
    @Select("<script>" +
            "SELECT * FROM sys_class WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    List<SysClass> selectBatchByIds(@Param("ids") List<Long> ids);
}