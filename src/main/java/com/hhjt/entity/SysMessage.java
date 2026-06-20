package com.hhjt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息通知实体
 */
@Data
@TableName("sys_message")
public class SysMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long receiverId;        // 接收者ID
    private Long senderId;          // 发送者ID(NULL表示系统消息)
    private String messageType;     // 消息类型(SYSTEM/AUDIT/EVALUATION/ANNOUNCEMENT)
    private String title;           // 消息标题
    private String content;         // 消息内容
    private Long relatedId;         // 关联业务ID
    private String relatedType;     // 关联业务类型
    private Integer isRead;         // 是否已读(0:未读/1:已读)
    private LocalDateTime readTime; // 阅读时间
    private LocalDateTime createTime;
}
