package com.hhjt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hhjt.dto.ClassDTO;
import com.hhjt.entity.SysClass;

import java.util.List;

public interface ClassService {
    // 分页查询班级
    IPage<SysClass> getClassPage(Integer pageNum, Integer pageSize, String className, String grade);

    // 查询所有班级（下拉框用）
    List<SysClass> getAllClass();

    // 新增班级
    boolean addClass(SysClass sysClass);

    // 编辑班级
    boolean updateClass(SysClass sysClass);

    // 删除班级
    boolean deleteClass(Long id);

    // 根据ID查询班级
    SysClass getClassById(Long id);

    // 根据教师ID查询任职班级
    List<SysClass> getClassByTeacherId(Long teacherId);

    // 根据ID列表查询班级
    List<SysClass> getClassByIds(List<Long> classIds);
}