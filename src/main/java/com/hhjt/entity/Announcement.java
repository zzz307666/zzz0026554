package com.hhjt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统公告实体
 */
@Data
@TableName("sys_announcement")
public class Announcement {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;           // 公告标题
    private String content;         // 公告内容(支持HTML)
    private String type;            // 公告类型(INFO/IMPORTANT/URGENT)
    private Integer priority;       // 优先级
    private LocalDateTime startTime; // 生效开始时间
    private LocalDateTime endTime;   // 生效结束时间
    private Long publisherId;       // 发布人ID
    private Integer status;         // 状态(0:草稿/1:已发布/2:已下架)
    private Integer viewCount;      // 浏览次数
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
