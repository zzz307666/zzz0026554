package com.hhjt.service;

import java.util.Map;

/**
 * 系统监控服务接口
 */
public interface SystemMonitorService {
    
    /**
     * 获取系统资源使用情况
     * @return CPU、内存、磁盘等信息
     */
    Map<String, Object> getSystemResources();
    
    /**
     * 获取在线用户统计
     * @return 在线用户信息
     */
    Map<String, Object> getOnlineUsers();
    
    /**
     * 获取JVM信息
     * @return JVM运行时信息
     */
    Map<String, Object> getJvmInfo();
    
    /**
     * 获取数据库连接池状态
     * @return 连接池信息
     */
    Map<String, Object> getDatabasePoolStatus();
    
    /**
     * 获取错误日志统计
     * @return 错误日志统计数据
     */
    Map<String, Object> getErrorLogStats();
    
    /**
     * 获取接口响应时间统计
     * @return 接口性能数据
     */
    Map<String, Object> getApiPerformance();
}
