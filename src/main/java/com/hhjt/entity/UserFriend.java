package com.hhjt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 好友关系实体
 */
@Data
@TableName("user_friend")
public class UserFriend {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;          // 用户ID
    private Long friendId;        // 好友ID
    private String remark;        // 备注名
    private Integer status;       // 状态（0:待确认 1:已好友 2:已拒绝）
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
