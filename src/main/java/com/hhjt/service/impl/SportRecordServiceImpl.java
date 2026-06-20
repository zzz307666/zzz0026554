package com.hhjt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhjt.entity.SportRecord;
import com.hhjt.entity.SportType;
import com.hhjt.entity.Student;
import com.hhjt.mapper.SportRecordMapper;
import com.hhjt.mapper.SportTypeMapper;
import com.hhjt.mapper.StudentMapper;
import com.hhjt.service.SportRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 运动打卡服务实现类
 */
@Slf4j
@Service
public class SportRecordServiceImpl implements SportRecordService {

    @Autowired
    private SportRecordMapper sportRecordMapper;

    @Autowired
    private SportTypeMapper sportTypeMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private com.hhjt.service.PointsService pointsService;

    @Autowired
    private com.hhjt.service.TeacherClassService teacherClassService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitRecord(SportRecord record, Long studentId) {
        // 数据校验
        if (record.getDuration() == null || record.getDuration() < 30) {
            throw new RuntimeException("运动时长不能少于30分钟");
        }

        // 获取学生信息
        Student student = studentMapper.selectByUserId(studentId);
        if (student == null) {
            throw new RuntimeException("学生信息不存在");
        }

        // 设置记录信息
        record.setStudentId(student.getId());
        record.setStatus(0); // 待审核
        record.setEarnedPoints(BigDecimal.ZERO);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());

        // 计算预估积分
        BigDecimal points = calculatePoints(record);
        record.setEarnedPoints(points);

        return sportRecordMapper.insert(record) > 0;
    }

    @Override
    public IPage<SportRecord> getRecordPage(Integer page, Integer size, Long studentId, 
                                            Integer status, String startDate, String endDate,
                                            Long teacherId, boolean isAdmin) {
        Page<SportRecord> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<SportRecord> wrapper = new LambdaQueryWrapper<>();

        // 条件查询
        if (studentId != null) {
            wrapper.eq(SportRecord::getStudentId, studentId);
        }
        if (status != null) {
            wrapper.eq(SportRecord::getStatus, status);
        }
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(SportRecord::getRecordDate, LocalDate.parse(startDate));
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(SportRecord::getRecordDate, LocalDate.parse(endDate));
        }

        // 如果不是管理员，需要过滤教师负责的班级
        if (!isAdmin && teacherId != null) {
            // 获取教师负责的所有班级ID
            List<Long> classIds = teacherClassService.getClassIdByTeacherId(teacherId);
            if (classIds != null && !classIds.isEmpty()) {
                // 子查询：只查询这些班级学生的记录
                wrapper.inSql(SportRecord::getStudentId, 
                    "SELECT id FROM sys_student WHERE class_id IN (" + 
                    String.join(",", classIds.stream().map(String::valueOf).toArray(String[]::new)) + ")"
                );
            } else {
                // 如果教师没有负责任何班级，返回空结果
                wrapper.eq(SportRecord::getId, -1); // 不可能存在的ID
            }
        }

        // 按日期倒序
        wrapper.orderByDesc(SportRecord::getRecordDate);

        IPage<SportRecord> result = sportRecordMapper.selectPage(pageInfo, wrapper);

        // 填充关联信息
        result.getRecords().forEach(this::fillRecordInfo);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditRecord(Long recordId, Integer status, String remark, Long teacherId) {
        SportRecord record = sportRecordMapper.selectById(recordId);
        if (record == null) {
            throw new RuntimeException("记录不存在");
        }

        if (record.getStatus() != 0) {
            throw new RuntimeException("该记录已审核");
        }

        // 更新审核信息
        record.setStatus(status);
        record.setAuditTeacherId(teacherId);
        record.setAuditTime(LocalDateTime.now());
        record.setAuditRemark(remark);
        record.setUpdateTime(LocalDateTime.now());

        boolean success = sportRecordMapper.updateById(record) > 0;

        // 如果审核通过，应用积分规则
        if (success && status == 1) {
            pointsService.applyPointsRules(recordId);
        }

        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchAudit(List<Long> recordIds, Integer status, Long teacherId) {
        if (recordIds == null || recordIds.isEmpty()) {
            return false;
        }

        for (Long recordId : recordIds) {
            try {
                auditRecord(recordId, status, "", teacherId);
            } catch (Exception e) {
                log.error("批量审核失败，记录ID: {}", recordId, e);
            }
        }

        return true;
    }

    @Override
    public BigDecimal calculatePoints(SportRecord record) {
        // 获取运动类型
        SportType sportType = sportTypeMapper.selectById(record.getSportTypeId());
        if (sportType == null) {
            throw new RuntimeException("运动类型不存在");
        }

        // 基础积分 = 基础分 × 系数 × (时长/30)
        BigDecimal basePoints = sportType.getBasePoints();
        BigDecimal coefficient = sportType.getCoefficient();
        BigDecimal durationFactor = new BigDecimal(record.getDuration())
                .divide(new BigDecimal(30), 2, RoundingMode.HALF_UP);

        BigDecimal points = basePoints.multiply(coefficient).multiply(durationFactor);

        return points.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public long getPendingCount(Long teacherId, boolean isAdmin) {
        LambdaQueryWrapper<SportRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SportRecord::getStatus, 0);
        
        // 如果不是管理员，只统计该教师负责班级的待审核记录
        if (!isAdmin && teacherId != null) {
            List<Long> classIds = teacherClassService.getClassIdByTeacherId(teacherId);
            if (classIds != null && !classIds.isEmpty()) {
                wrapper.inSql(SportRecord::getStudentId, 
                    "SELECT id FROM sys_student WHERE class_id IN (" + 
                    String.join(",", classIds.stream().map(String::valueOf).toArray(String[]::new)) + ")"
                );
            } else {
                return 0; // 没有负责任何班级，返回0
            }
        }
        
        return sportRecordMapper.selectCount(wrapper);
    }

    /**
     * 填充记录关联信息
     */
    private void fillRecordInfo(SportRecord record) {
        // 填充学生信息
        Student student = studentMapper.selectById(record.getStudentId());
        if (student != null) {
            record.setStudentNo(student.getStudentNo());
        }

        // 填充运动类型名称
        SportType sportType = sportTypeMapper.selectById(record.getSportTypeId());
        if (sportType != null) {
            record.setSportTypeName(sportType.getTypeName());
        }
    }
}
