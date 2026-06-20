package com.hhjt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学生积分实体
 */
@Data
@TableName("student_points")
public class StudentPoints {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;       // 学生ID
    private String pointsType;    // 积分类型
    private BigDecimal pointsValue; // 积分值
    private Long relatedId;       // 关联业务ID
    private String description;   // 积分说明
    private Long operatorId;      // 操作人ID
    private LocalDateTime createTime;

    // 关联字段
    @TableField(exist = false)
    private String studentName;   // 学生姓名
    @TableField(exist = false)
    private String studentNo;     // 学号
}
