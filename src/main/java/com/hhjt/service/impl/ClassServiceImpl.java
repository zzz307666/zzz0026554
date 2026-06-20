package com.hhjt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhjt.entity.SysClass;
import com.hhjt.mapper.ClassMapper;
import com.hhjt.mapper.StudentMapper;
import com.hhjt.service.ClassService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClassServiceImpl implements ClassService {

    private final ClassMapper classMapper;
    private final StudentMapper studentMapper;

    @Override
    public IPage<SysClass> getClassPage(Integer pageNum, Integer pageSize, String className, String grade) {
        Page<SysClass> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysClass> wrapper = new LambdaQueryWrapper<>();
        if (className != null && !className.isEmpty()) {
            wrapper.like(SysClass::getClassName, className);
        }
        if (grade != null && !grade.isEmpty()) {
            wrapper.eq(SysClass::getGrade, grade);
        }
        // 修改：按创建时间升序，从早到晚
        wrapper.orderByAsc(SysClass::getCreateTime);
        return classMapper.selectPage(page, wrapper);
    }

    @Override
    public List<SysClass> getAllClass() {
        // 修改：按年级、创建时间升序，从早到晚
        LambdaQueryWrapper<SysClass> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysClass::getGrade, SysClass::getCreateTime);
        return classMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addClass(SysClass sysClass) {
        // 校验班级名称唯一性
        Long count = classMapper.selectCount(new LambdaQueryWrapper<SysClass>().eq(SysClass::getClassName, sysClass.getClassName()));
        if (count > 0) {
            throw new RuntimeException("班级名称已存在，不可重复添加");
        }
        sysClass.setCreateTime(LocalDateTime.now());
        sysClass.setUpdateTime(LocalDateTime.now());
        return classMapper.insert(sysClass) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateClass(SysClass sysClass) {
        // 校验班级名称唯一性（排除自身）
        Long count = classMapper.selectCount(new LambdaQueryWrapper<SysClass>()
                .eq(SysClass::getClassName, sysClass.getClassName())
                .ne(SysClass::getId, sysClass.getId()));
        if (count > 0) {
            throw new RuntimeException("班级名称已存在");
        }
        sysClass.setUpdateTime(LocalDateTime.now());
        return classMapper.updateById(sysClass) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteClass(Long id) {
        // 校验是否有学生关联
        int studentCount = studentMapper.countByClassId(id);
        if (studentCount > 0) {
            throw new RuntimeException("该班级下存在学生，无法删除");
        }
        return classMapper.deleteById(id) > 0;
    }

    @Override
    public SysClass getClassById(Long id) {
        return classMapper.selectById(id);
    }

    @Override
    public List<SysClass> getClassByTeacherId(Long teacherId) {
        return classMapper.selectClassByTeacherId(teacherId);
    }

    @Override
    public List<SysClass> getClassByIds(List<Long> classIds) {
        if (classIds == null || classIds.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        LambdaQueryWrapper<SysClass> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SysClass::getId, classIds);
        return classMapper.selectList(wrapper);
    }
}