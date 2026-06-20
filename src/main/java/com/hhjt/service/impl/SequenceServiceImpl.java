package com.hhjt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hhjt.entity.SysSequence;
import com.hhjt.mapper.SysSequenceMapper;
import com.hhjt.service.SequenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * 自增序列服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SequenceServiceImpl implements SequenceService {

    private final SysSequenceMapper sequenceMapper;

    // 序列编码常量
    private static final String SEQ_TEACHER = "TEACHER";
    private static final String SEQ_STUDENT = "STUDENT";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 获取教师工号（事务保证原子性，避免并发重复）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String getTeacherNo() {
        try {
            String teacherNo = sequenceMapper.getNextSeqByDate(SEQ_TEACHER);
            log.info("自动生成教师工号：{}", teacherNo);
            return teacherNo;
        } catch (Exception e) {
            log.error("数据库函数生成教师工号失败，启用本地兜底方案", e);
            return getLocalSeqByDate(SEQ_TEACHER);
        }
    }

    /**
     * 获取学生学号（事务保证原子性，避免并发重复）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String getStudentNo() {
        try {
            String studentNo = sequenceMapper.getNextSeqByDate(SEQ_STUDENT);
            log.info("自动生成学生学号：{}", studentNo);
            return studentNo;
        } catch (Exception e) {
            log.error("数据库函数生成学生学号失败，启用本地兜底方案", e);
            return getLocalSeqByDate(SEQ_STUDENT);
        }
    }

    /**
     * 本地兜底序列生成方案，防止数据库函数异常，保证业务可用
     */
    @Transactional(rollbackFor = Exception.class)
    public String getLocalSeqByDate(String seqCode) {
        String currentDate = LocalDateTime.now().format(DATE_FORMATTER);

        // 行锁查询当日序列
        SysSequence sequence = sequenceMapper.selectOne(
                new LambdaQueryWrapper<SysSequence>()
                        .eq(SysSequence::getSeqCode, seqCode)
                        .eq(SysSequence::getSeqDate, currentDate)
                        .last("LIMIT 1 FOR UPDATE")
        );

        long nextVal;
        String prefix;
        int digit;

        if (sequence == null) {
            // 当日无序列，读取基础配置初始化
            SysSequence baseConfig = sequenceMapper.selectOne(
                    new LambdaQueryWrapper<SysSequence>()
                            .eq(SysSequence::getSeqCode, seqCode)
                            .eq(SysSequence::getSeqDate, "")
                            .last("LIMIT 1")
            );
            if (baseConfig == null) {
                throw new RuntimeException("序列编码[" + seqCode + "]不存在，请先初始化序列数据");
            }

            nextVal = 1;
            prefix = baseConfig.getPrefix();
            digit = Optional.ofNullable(baseConfig.getDigit()).orElse(3);

            // 插入当日序列
            sequence = new SysSequence();
            sequence.setSeqCode(seqCode);
            sequence.setSeqDate(currentDate);
            sequence.setCurrentValue(nextVal);
            sequence.setPrefix(prefix);
            sequence.setDigit(digit);
            sequence.setUpdateTime(LocalDateTime.now());
            sequenceMapper.insert(sequence);
        } else {
            // 当日已有序列，自增
            nextVal = sequence.getCurrentValue() + 1;
            prefix = sequence.getPrefix();
            digit = Optional.ofNullable(sequence.getDigit()).orElse(3);

            sequence.setCurrentValue(nextVal);
            sequence.setUpdateTime(LocalDateTime.now());
            sequenceMapper.updateById(sequence);
        }

        // 拼接最终编号
        String result = prefix + currentDate + String.format("%0" + digit + "d", nextVal);
        log.info("本地兜底生成序列编号：{}", result);
        return result;
    }
}