package com.hhjt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("sys_teacher")
public class Teacher {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId; // 关联sys_user.id
    private String teacherNo; // 教师工号（唯一）
    private String subject; // 任教科目
    private Integer gender; // 性别 0-未知 1-男 2-女
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 关联任职班级列表（多对多）
    @TableField(exist = false)
    private List<SysClass> classList;
}