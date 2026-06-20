package com.hhjt.service;

import java.util.List;

public interface TeacherClassService {
    // 保存教师-班级关联
    boolean saveTeacherClass(Long teacherId, List<Long> classIdList);

    // 删除教师所有班级关联
    boolean deleteByTeacherId(Long teacherId);

    // 查询教师关联的班级ID列表
    List<Long> getClassIdByTeacherId(Long teacherId);
}