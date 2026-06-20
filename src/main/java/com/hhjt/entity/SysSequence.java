package com.hhjt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统自增序列表（用于生成教师工号、学生学号）
 */
@Data
@TableName("sys_sequence")
public class SysSequence {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String seqCode; // 序列编码：TEACHER/STUDENT
    private String seqDate; // 序列日期(yyyyMMdd)，按天隔离流水号
    private Long currentValue; // 当前值
    private String prefix; // 编号前缀
    private Integer digit; // 数字位数
    private LocalDateTime updateTime; // 更新时间
}