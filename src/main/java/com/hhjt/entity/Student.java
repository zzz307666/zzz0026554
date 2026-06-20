package com.hhjt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sys_student")
public class Student {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId; // 关联sys_user.id
    private String studentNo; // 学生学号（唯一）
    private Long classId; // 关联班级ID（必选）
    private Integer gender; // 性别 0-未知 1-男 2-女
    private LocalDate birthDate; // 出生日期
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 关联班级信息
    @TableField(exist = false)
    private SysClass sysClass;
}