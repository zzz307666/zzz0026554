package com.hhjt.controller;

import com.hhjt.service.SystemMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员 - 系统监控控制器
 */
@Slf4j
@Controller
@RequestMapping("/admin/monitor")
public class SystemMonitorController {

    @Autowired
    private SystemMonitorService monitorService;

    /**
     * 监控面板页面
     */
    @GetMapping("/dashboard")
    public String monitorDashboard(Model model) {
        return "admin/monitor";
    }

    /**
     * 获取系统资源信息
     */
    @GetMapping("/system-resources")
    @ResponseBody
    public Map<String, Object> getSystemResources() {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("success", true);
            result.put("data", monitorService.getSystemResources());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("获取系统资源失败", e);
        }
        return result;
    }

    /**
     * 获取在线用户统计
     */
    @GetMapping("/online-users")
    @ResponseBody
    public Map<String, Object> getOnlineUsers() {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("success", true);
            result.put("data", monitorService.getOnlineUsers());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("获取在线用户失败", e);
        }
        return result;
    }

    /**
     * 获取JVM信息
     */
    @GetMapping("/jvm-info")
    @ResponseBody
    public Map<String, Object> getJvmInfo() {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("success", true);
            result.put("data", monitorService.getJvmInfo());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("获取JVM信息失败", e);
        }
        return result;
    }

    /**
     * 获取数据库连接池状态
     */
    @GetMapping("/db-pool")
    @ResponseBody
    public Map<String, Object> getDatabasePool() {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("success", true);
            result.put("data", monitorService.getDatabasePoolStatus());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("获取数据库连接池失败", e);
        }
        return result;
    }

    /**
     * 获取错误日志统计
     */
    @GetMapping("/error-stats")
    @ResponseBody
    public Map<String, Object> getErrorStats() {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("success", true);
            result.put("data", monitorService.getErrorLogStats());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("获取错误日志统计失败", e);
        }
        return result;
    }

    /**
     * 获取API性能数据
     */
    @GetMapping("/api-performance")
    @ResponseBody
    public Map<String, Object> getApiPerformance() {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("success", true);
            result.put("data", monitorService.getApiPerformance());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("获取API性能失败", e);
        }
        return result;
    }
}
