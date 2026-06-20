package com.hhjt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhjt.entity.OperationLog;
import com.hhjt.mapper.OperationLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员 - 操作日志查询控制器
 */
@Slf4j
@Controller
@RequestMapping("/admin/log")
public class AdminOperationLogController {

    @Autowired
    private OperationLogMapper operationLogMapper;

    /**
     * 操作日志查询页面
     */
    @GetMapping("/list")
    public String logList(@RequestParam(defaultValue = "1") Integer pageNum,
                          @RequestParam(defaultValue = "20") Integer pageSize,
                          @RequestParam(required = false) String username,
                          @RequestParam(required = false) String module,
                          @RequestParam(required = false) Integer status,
                          @RequestParam(required = false) String startTime,
                          @RequestParam(required = false) String endTime,
                          Model model) {
        Page<OperationLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        
        // 筛选条件
        if (username != null && !username.isEmpty()) {
            wrapper.like(OperationLog::getUsername, username);
        }
        if (module != null && !module.isEmpty()) {
            wrapper.eq(OperationLog::getModule, module);
        }
        if (status != null) {
            wrapper.eq(OperationLog::getResult, status == 1 ? "SUCCESS" : "FAIL");
        }
        if (startTime != null && !startTime.isEmpty()) {
            wrapper.ge(OperationLog::getCreateTime, LocalDateTime.parse(startTime));
        }
        if (endTime != null && !endTime.isEmpty()) {
            wrapper.le(OperationLog::getCreateTime, LocalDateTime.parse(endTime));
        }
        
        wrapper.orderByDesc(OperationLog::getCreateTime);
        Page<OperationLog> result = operationLogMapper.selectPage(page, wrapper);

        model.addAttribute("page", result);
        return "admin/operation_log";
    }

    /**
     * 获取统计数据
     */
    @GetMapping("/stats")
    @ResponseBody
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 今日操作数
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        long todayCount = operationLogMapper.selectCount(
            new LambdaQueryWrapper<OperationLog>()
                .ge(OperationLog::getCreateTime, todayStart)
        );
        
        // 成功操作数
        long successCount = operationLogMapper.selectCount(
            new LambdaQueryWrapper<OperationLog>()
                .eq(OperationLog::getResult, "SUCCESS")
        );
        
        // 失败操作数
        long failedCount = operationLogMapper.selectCount(
            new LambdaQueryWrapper<OperationLog>()
                .eq(OperationLog::getResult, "FAIL")
        );
        
        // 总日志数
        long totalCount = operationLogMapper.selectCount(null);
        
        stats.put("todayCount", todayCount);
        stats.put("successCount", successCount);
        stats.put("failedCount", failedCount);
        stats.put("totalCount", totalCount);
        
        return stats;
    }

    /**
     * 查看日志详情
     */
    @GetMapping("/detail/{id}")
    @ResponseBody
    public OperationLog getDetail(@PathVariable Long id) {
        return operationLogMapper.selectById(id);
    }

    /**
     * 导出日志为CSV
     */
    @GetMapping("/export")
    public void exportLogs(@RequestParam(required = false) String username,
                           @RequestParam(required = false) String module,
                           @RequestParam(required = false) Integer status,
                           HttpServletResponse response) {
        try {
            // 设置响应头
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=operation_logs.csv");
            
            // 构建查询条件
            LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
            if (username != null && !username.isEmpty()) {
                wrapper.like(OperationLog::getUsername, username);
            }
            if (module != null && !module.isEmpty()) {
                wrapper.eq(OperationLog::getModule, module);
            }
            if (status != null) {
                wrapper.eq(OperationLog::getResult, status == 1 ? "SUCCESS" : "FAIL");
            }
            wrapper.orderByDesc(OperationLog::getCreateTime);
            
            List<OperationLog> logs = operationLogMapper.selectList(wrapper);
            
            // 写入CSV
            PrintWriter writer = response.getWriter();
            writer.println("ID,用户名,模块,操作,IP,执行时间(ms),状态,操作时间");
            
            for (OperationLog log : logs) {
                writer.printf("%d,%s,%s,%s,%s,%d,%s,%s\n",
                    log.getId(),
                    log.getUsername() != null ? log.getUsername() : "",
                    log.getModule() != null ? log.getModule() : "",
                    log.getOperation() != null ? log.getOperation() : "",
                    log.getIp() != null ? log.getIp() : "",
                    log.getDuration() != null ? log.getDuration() : 0,
                    "SUCCESS".equals(log.getResult()) ? "成功" : "失败",
                    log.getCreateTime() != null ? log.getCreateTime().toString() : ""
                );
            }
            
            writer.flush();
            log.info("导出操作日志 {} 条", logs.size());
            
        } catch (Exception e) {
            log.error("导出操作日志失败", e);
        }
    }

    /**
     * 清理旧日志（90天前）
     */
    @PostMapping("/clear")
    @ResponseBody
    public Map<String, Object> clearOldLogs() {
        Map<String, Object> result = new HashMap<>();
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(90);
            int deletedCount = operationLogMapper.delete(
                new LambdaQueryWrapper<OperationLog>()
                    .lt(OperationLog::getCreateTime, cutoffTime)
            );
            
            result.put("success", true);
            result.put("message", "成功清理 " + deletedCount + " 条旧日志");
            log.info("清理旧日志 {} 条", deletedCount);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "清理失败: " + e.getMessage());
            log.error("清理旧日志失败", e);
        }
        return result;
    }
}
