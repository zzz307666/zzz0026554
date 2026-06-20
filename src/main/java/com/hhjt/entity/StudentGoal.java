package com.hhjt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学生运动目标实体
 */
@Data
@TableName("student_goal")
public class StudentGoal {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;         // 学生ID
    private String goalType;        // 目标类型(COUNT/DURATION/CALORIES)
    private BigDecimal targetValue; // 目标值
    private BigDecimal currentValue; // 当前完成值
    private String periodType;      // 周期类型(DAY/WEEK/MONTH/SEMESTER)
    private LocalDate startDate;    // 开始日期
    private LocalDate endDate;      // 结束日期
    private Integer status;         // 状态(0:已取消/1:进行中/2:已完成/3:失败)
    private BigDecimal rewardPoints; // 达成奖励积分
    private LocalDateTime completedTime; // 完成时间
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
