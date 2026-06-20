package com.hhjt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分奖品实体
 */
@Data
@TableName("points_gift")
public class PointsGift {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String giftName;        // 奖品名称
    private String giftImage;       // 奖品图片URL
    private Integer pointsCost;     // 所需积分
    private Integer stock;          // 库存数量
    private String description;     // 奖品描述
    private String category;        // 奖品分类
    private Integer status;         // 状态:0-下架 1-上架
    private Integer sortOrder;      // 排序
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
