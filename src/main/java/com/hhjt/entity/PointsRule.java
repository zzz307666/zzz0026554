package com.hhjt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 积分规则实体
 */
@Data
@TableName("points_rule")
public class PointsRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String ruleName;      // 规则名称
    private String ruleCode;      // 规则编码
    private String ruleType;      // 规则类型(BASE/BONUS/PENALTY)
    private BigDecimal pointsValue; // 积分值
    private String conditionJson; // 触发条件(JSON)
    private String description;   // 规则描述
    private Integer status;       // 状态:0-禁用 1-启用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
