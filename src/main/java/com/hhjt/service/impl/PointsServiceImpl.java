package com.hhjt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhjt.entity.SportRecord;
import com.hhjt.entity.StudentPoints;
import com.hhjt.mapper.SportRecordMapper;
import com.hhjt.mapper.StudentPointsMapper;
import com.hhjt.service.PointsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学生积分服务实现类
 */
@Slf4j
@Service
public class PointsServiceImpl implements PointsService {

    @Autowired
    private StudentPointsMapper pointsMapper;

    @Autowired
    private SportRecordMapper sportRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPoints(Long studentId, String pointsType, BigDecimal value, 
                          String description, Long relatedId) {
        StudentPoints points = new StudentPoints();
        points.setStudentId(studentId);
        points.setPointsType(pointsType);
        points.setPointsValue(value);
        points.setDescription(description);
        points.setRelatedId(relatedId);
        points.setCreateTime(LocalDateTime.now());

        pointsMapper.insert(points);
        log.info("添加积分：学生ID={}, 类型={}, 分值={}, 说明={}", 
                studentId, pointsType, value, description);
    }

    @Override
    public BigDecimal getStudentTotalPoints(Long studentId) {
        BigDecimal totalPoints = pointsMapper.getStudentTotalPoints(studentId);
        return totalPoints != null ? totalPoints : BigDecimal.ZERO;
    }

    @Override
    public IPage<StudentPoints> getPointsPage(Integer page, Integer size, 
                                              Long studentId, String pointsType) {
        Page<StudentPoints> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<StudentPoints> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(StudentPoints::getStudentId, studentId);
        
        if (pointsType != null && !pointsType.isEmpty()) {
            wrapper.eq(StudentPoints::getPointsType, pointsType);
        }

        wrapper.orderByDesc(StudentPoints::getCreateTime);

        return pointsMapper.selectPage(pageInfo, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyPointsRules(Long recordId) {
        SportRecord record = sportRecordMapper.selectById(recordId);
        if (record == null || record.getStatus() != 1) {
            return;
        }

        // 打卡通过，添加运动积分
        addPoints(
            record.getStudentId(),
            "MOTION",
            record.getEarnedPoints(),
            "运动打卡：" + record.getRemark(),
            recordId
        );

        // TODO: 这里可以添加更多积分规则判断
        // 例如：连续打卡奖励、月度达人奖励等
    }
}
