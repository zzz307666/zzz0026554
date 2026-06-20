package com.hhjt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 评价维度实体
 */
@Data
@TableName("evaluation_dimension")
public class EvaluationDimension {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String dimensionName;  // 维度名称
    private String dimensionCode;  // 维度编码
    private BigDecimal weight;     // 权重
    private BigDecimal maxScore;   // 满分
    private String description;    // 描述
    private Integer sortOrder;     // 排序
    private Integer status;        // 状态
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
