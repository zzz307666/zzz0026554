package com.hhjt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 运动挑战实体
 */
@Data
@TableName("sport_challenge")
public class SportChallenge {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long challengerId;    // 挑战者ID
    private Long challengedId;    // 被挑战者ID
    private String challengeType; // 挑战类型（steps/distance/duration）
    private Double targetValue;   // 目标值
    private String unit;          // 单位
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer status;       // 状态（0:进行中 1:已完成 2:已取消）
    private Long winnerId;        // 获胜者ID
    private LocalDateTime createTime;
}
