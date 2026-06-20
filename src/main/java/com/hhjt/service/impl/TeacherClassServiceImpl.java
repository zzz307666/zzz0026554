package com.hhjt.service.impl;

import com.hhjt.entity.TeacherClass;
import com.hhjt.mapper.TeacherClassMapper;
import com.hhjt.service.TeacherClassService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherClassServiceImpl implements TeacherClassService {

    private final TeacherClassMapper teacherClassMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveTeacherClass(Long teacherId, List<Long> classIdList) {
        // 先删除原有关联
        teacherClassMapper.deleteByTeacherId(teacherId);
        // 批量新增新关联
        if (classIdList != null && !classIdList.isEmpty()) {
            for (Long classId : classIdList) {
                TeacherClass teacherClass = new TeacherClass();
                teacherClass.setTeacherId(teacherId);
                teacherClass.setClassId(classId);
                teacherClass.setCreateTime(LocalDateTime.now());
                teacherClassMapper.insert(teacherClass);
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByTeacherId(Long teacherId) {
        return teacherClassMapper.deleteByTeacherId(teacherId) >= 0;
    }

    @Override
    public List<Long> getClassIdByTeacherId(Long teacherId) {
        return teacherClassMapper.selectClassIdByTeacherId(teacherId);
    }
}