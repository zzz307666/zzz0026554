package com.hhjt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生徽章获得记录实体
 */
@Data
@TableName("student_badge")
public class StudentBadge {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;         // 学生ID
    private Long badgeId;           // 徽章ID
    private LocalDateTime achievedTime; // 获得时间
    private Integer isDisplayed;    // 是否展示
    private LocalDateTime createTime;
}
