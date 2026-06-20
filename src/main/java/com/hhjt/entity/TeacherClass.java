package com.hhjt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_teacher_class")
public class TeacherClass {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long teacherId; // 关联教师ID
    private Long classId; // 关联班级ID
    private LocalDateTime createTime;
}