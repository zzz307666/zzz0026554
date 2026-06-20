package com.hhjt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 运动提醒设置实体类
 */
@Data
@TableName("sport_reminder")
public class SportReminder {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String reminderType; // DAILY-每日提醒，WEEKLY-每周提醒
    
    private String reminderTime; // 提醒时间 HH:mm
    
    private Integer isActive; // 是否启用 0-禁用 1-启用
    
    private String reminderDays; // 提醒日期（周几，逗号分隔）1,2,3,4,5,6,7
    
    private String message; // 提醒消息内容
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
