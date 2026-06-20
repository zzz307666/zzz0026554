package com.hhjt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分兑换记录实体
 */
@Data
@TableName("points_exchange")
public class PointsExchange {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;         // 学生ID
    private Long giftId;            // 奖品ID
    private String giftName;        // 奖品名称（快照）
    private Integer pointsCost;     // 消耗积分（快照）
    private LocalDateTime exchangeTime; // 兑换时间
    private Integer status;         // 状态:0-待领取 1-已领取 2-已取消
    private LocalDateTime receiveTime;  // 领取时间
    private String remark;          // 备注
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 关联字段（非数据库字段）
    @TableField(exist = false)
    private String studentName;     // 学生姓名
    @TableField(exist = false)
    private String studentNo;       // 学号
}
