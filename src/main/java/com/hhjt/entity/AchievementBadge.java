package com.hhjt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成就徽章定义实体
 */
@Data
@TableName("achievement_badge")
public class AchievementBadge {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String badgeCode;       // 徽章编码
    private String badgeName;       // 徽章名称
    private String badgeIcon;       // 徽章图标URL
    private String badgeLevel;      // 徽章等级(BRONZE/SILVER/GOLD/PLATINUM)
    private String description;     // 徽章描述
    private String conditionJson;   // 获取条件(JSON)
    private BigDecimal rewardPoints; // 获得奖励积分
    private Integer sortOrder;      // 排序
    private Integer isActive;       // 是否启用
    private LocalDateTime createTime;
}
