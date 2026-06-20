package com.hhjt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 运动打卡记录实体
 */
@Data
@TableName("sport_record")
public class SportRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;        // 学生ID
    private Long sportTypeId;      // 运动类型ID
    private LocalDate recordDate;  // 运动日期
    private Integer duration;      // 运动时长(分钟)
    private BigDecimal distance;   // 运动距离(米)
    private Integer calories;      // 消耗卡路里
    private Integer steps;         // 步数
    private Integer heartRate;     // 平均心率
    private String dataJson;       // 扩展数据(JSON)
    private String remark;         // 备注
    private Integer status;        // 状态:0-待审核 1-已通过 2-已驳回
    private Long auditTeacherId;   // 审核教师ID
    private LocalDateTime auditTime; // 审核时间
    private String auditRemark;    // 审核备注
    private BigDecimal earnedPoints; // 获得积分
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 关联字段（非数据库字段）
    @TableField(exist = false)
    private String studentName;    // 学生姓名
    @TableField(exist = false)
    private String studentNo;      // 学号
    @TableField(exist = false)
    private String className;      // 班级名称
    @TableField(exist = false)
    private String sportTypeName;  // 运动类型名称
    @TableField(exist = false)
    private String teacherName;    // 审核教师姓名
}
