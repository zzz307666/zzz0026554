package com.hhjt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学生评价记录实体
 */
@Data
@TableName("student_evaluation")
public class StudentEvaluation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;           // 学生ID
    private Long teacherId;           // 评价教师ID
    private String evaluationPeriod;  // 评价周期
    private BigDecimal enduranceScore;     // 耐力评分
    private BigDecimal strengthScore;      // 力量评分
    private BigDecimal speedScore;         // 速度评分
    private BigDecimal flexibilityScore;   // 柔韧评分
    private BigDecimal coordinationScore;  // 协调评分
    private BigDecimal totalScore;         // 综合总分
    private String gradeLevel;             // 等级
    private String teacherComment;         // 教师评语
    private Integer status;                // 状态:0-草稿 1-已发布
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 关联字段
    @TableField(exist = false)
    private String studentName;      // 学生姓名
    @TableField(exist = false)
    private String studentNo;        // 学号
    @TableField(exist = false)
    private String className;        // 班级名称
    @TableField(exist = false)
    private String teacherName;      // 教师姓名
}
