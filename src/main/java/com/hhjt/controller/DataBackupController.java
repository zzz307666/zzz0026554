package com.hhjt.controller;

import com.hhjt.service.DataBackupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员 - 数据备份控制器
 */
@Slf4j
@Controller
@RequestMapping("/admin/backup")
public class DataBackupController {

    @Autowired
    private DataBackupService backupService;

    /**
     * 备份管理页面
     */
    @GetMapping("/management")
    public String backupManagement(Model model) {
        return "admin/backup_management";
    }

    /**
     * 手动备份数据库
     */
    @PostMapping("/create")
    @ResponseBody
    public Map<String, Object> createBackup(@RequestParam(required = false) String backupName) {
        Map<String, Object> result = new HashMap<>();
        try {
            String filePath = backupService.manualBackup(backupName);
            result.put("success", true);
            result.put("message", "备份成功");
            result.put("filePath", filePath);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "备份失败: " + e.getMessage());
            log.error("创建备份失败", e);
        }
        return result;
    }

    /**
     * 获取备份列表
     */
    @GetMapping("/list")
    @ResponseBody
    public Map<String, Object> getBackupList() {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("success", true);
            result.put("data", backupService.getBackupList());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("获取备份列表失败", e);
        }
        return result;
    }

    /**
     * 删除备份文件
     */
    @DeleteMapping("/delete/{backupId}")
    @ResponseBody
    public Map<String, Object> deleteBackup(@PathVariable Long backupId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = backupService.deleteBackup(backupId);
            result.put("success", success);
            result.put("message", success ? "删除成功" : "删除失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("删除备份失败", e);
        }
        return result;
    }

    /**
     * 恢复数据库
     */
    @PostMapping("/restore/{backupId}")
    @ResponseBody
    public Map<String, Object> restoreDatabase(@PathVariable Long backupId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = backupService.restoreDatabase(backupId);
            result.put("success", success);
            result.put("message", success ? "恢复成功，请重新登录系统" : "恢复失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("恢复数据库失败", e);
        }
        return result;
    }

    /**
     * 下载备份文件
     */
    @GetMapping("/download/{backupId}")
    public ResponseEntity<byte[]> downloadBackup(@PathVariable Long backupId) {
        try {
            byte[] fileContent = backupService.downloadBackup(backupId);
            
            if (fileContent == null) {
                return ResponseEntity.notFound().build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "backup_" + backupId + ".sql");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(fileContent);
                
        } catch (Exception e) {
            log.error("下载备份文件失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
