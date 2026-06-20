package com.hhjt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 运动类型实体
 */
@Data
@TableName("sport_type")
public class SportType {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String typeName;      // 运动类型名称
    private String typeCode;      // 类型编码
    private BigDecimal basePoints; // 基础积分
    private BigDecimal coefficient; // 积分系数
    private String unit;          // 计量单位
    private String icon;          // 图标
    private Integer sortOrder;    // 排序
    private Integer status;       // 状态:0-禁用 1-启用
    private String description;   // 描述
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
