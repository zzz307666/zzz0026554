package com.hhjt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体
 */
@Data
@TableName("sys_operation_log")
public class OperationLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String module;          // 模块名称
    private String operation;       // 操作类型
    private String method;          // 方法名
    private String params;          // 请求参数
    private Long userId;            // 操作用户ID
    private String username;        // 用户名
    private String ip;              // IP地址
    private String result;          // 操作结果(SUCCESS/FAIL)
    private String errorMsg;        // 错误信息
    private Long duration;          // 执行时长(毫秒)
    private LocalDateTime createTime;
}
