package com.hhjt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 运动分享实体
 */
@Data
@TableName("sport_share")
public class SportShare {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;          // 分享者ID
    private String userName;      // 分享者姓名
    private String content;       // 分享内容
    private String imageUrl;      // 图片URL
    private Integer likeCount;    // 点赞数
    private Integer commentCount; // 评论数
    private LocalDateTime createTime;
}
