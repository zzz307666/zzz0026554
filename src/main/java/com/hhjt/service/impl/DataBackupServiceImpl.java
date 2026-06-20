package com.hhjt.service.impl;

import com.hhjt.service.DataBackupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据备份服务实现类
 */
@Slf4j
@Service
public class DataBackupServiceImpl implements DataBackupService {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    private static final String BACKUP_DIR = "backups";

    @Override
    public String manualBackup(String backupName) {
        Process process = null;
        try {
            // 创建备份目录
            Path backupPath = Paths.get(BACKUP_DIR);
            if (!Files.exists(backupPath)) {
                Files.createDirectories(backupPath);
            }

            // 生成备份文件名
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = (backupName != null ? backupName : "backup") + "_" + timestamp + ".sql";
            File backupFile = new File(BACKUP_DIR, fileName);

            // 解析数据库名称
            String dbName = dbUrl.substring(dbUrl.lastIndexOf("/") + 1).split("\\?")[0];
            
            log.info("开始备份数据库: {} 到: {}", dbName, backupFile.getAbsolutePath());
            
            // 构建mysqldump命令
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder processBuilder;
            
            if (os.contains("win")) {
                // Windows系统
                processBuilder = new ProcessBuilder(
                    "cmd.exe", "/c",
                    "mysqldump",
                    "-u" + dbUsername,
                    "-p" + dbPassword,
                    "--single-transaction",
                    "--routines",
                    "--triggers",
                    dbName
                );
            } else {
                // Linux/Mac系统
                processBuilder = new ProcessBuilder(
                    "mysqldump",
                    "-u" + dbUsername,
                    "-p" + dbPassword,
                    "--single-transaction",
                    "--routines",
                    "--triggers",
                    dbName
                );
            }
            
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();
            
            // 读取输出并写入文件
            try (InputStream inputStream = process.getInputStream();
                 OutputStream outputStream = new FileOutputStream(backupFile)) {
                
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }
            
            // 等待进程完成
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("mysqldump执行失败，退出码: " + exitCode);
            }

            log.info("数据库备份完成: {}, 文件大小: {}", backupFile.getAbsolutePath(), formatFileSize(backupFile.length()));
            return backupFile.getAbsolutePath();

        } catch (Exception e) {
            log.error("数据库备份失败", e);
            throw new RuntimeException("备份失败: " + e.getMessage());
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    @Override
    public List<Map<String, Object>> getBackupList() {
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            Path backupPath = Paths.get(BACKUP_DIR);
            if (!Files.exists(backupPath)) {
                return result;
            }

            // 获取所有备份文件
            List<File> backupFiles = Files.list(backupPath)
                .map(Path::toFile)
                .filter(file -> file.getName().endsWith(".sql"))
                .sorted((f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()))
                .collect(Collectors.toList());

            // 构建返回数据
            for (int i = 0; i < backupFiles.size(); i++) {
                File file = backupFiles.get(i);
                Map<String, Object> backup = new HashMap<>();
                backup.put("id", (long) (i + 1));
                backup.put("fileName", file.getName());
                backup.put("fileSize", formatFileSize(file.length()));
                backup.put("createTime", new Date(file.lastModified()).toString());
                backup.put("filePath", file.getAbsolutePath());
                result.add(backup);
            }

        } catch (Exception e) {
            log.error("获取备份列表失败", e);
        }

        return result;
    }

    @Override
    public boolean deleteBackup(Long backupId) {
        try {
            List<Map<String, Object>> backupList = getBackupList();
            if (backupId <= 0 || backupId > backupList.size()) {
                return false;
            }

            Map<String, Object> backup = backupList.get(backupId.intValue() - 1);
            String filePath = (String) backup.get("filePath");
            
            File file = new File(filePath);
            if (file.exists()) {
                boolean deleted = file.delete();
                log.info("删除备份文件: {}, 结果: {}", filePath, deleted);
                return deleted;
            }
            
            return false;

        } catch (Exception e) {
            log.error("删除备份文件失败", e);
            return false;
        }
    }

    @Override
    public boolean restoreDatabase(Long backupId) {
        Process process = null;
        try {
            List<Map<String, Object>> backupList = getBackupList();
            if (backupId <= 0 || backupId > backupList.size()) {
                return false;
            }

            Map<String, Object> backup = backupList.get(backupId.intValue() - 1);
            String filePath = (String) backup.get("filePath");

            File backupFile = new File(filePath);
            if (!backupFile.exists()) {
                log.error("备份文件不存在: {}", filePath);
                return false;
            }

            // 解析数据库名称
            String dbName = dbUrl.substring(dbUrl.lastIndexOf("/") + 1).split("\\?")[0];
            
            log.info("开始恢复数据库 from: {}", filePath);
            
            // 构建mysql命令
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder processBuilder;
            
            if (os.contains("win")) {
                // Windows系统
                processBuilder = new ProcessBuilder(
                    "cmd.exe", "/c",
                    "mysql",
                    "-u" + dbUsername,
                    "-p" + dbPassword,
                    dbName
                );
            } else {
                // Linux/Mac系统
                processBuilder = new ProcessBuilder(
                    "mysql",
                    "-u" + dbUsername,
                    "-p" + dbPassword,
                    dbName
                );
            }
            
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();
            
            // 将备份文件内容写入mysql进程
            try (OutputStream outputStream = process.getOutputStream();
                 InputStream inputStream = new FileInputStream(backupFile)) {
                
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }
            
            // 等待进程完成
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("mysql恢复失败，退出码: {}", exitCode);
                return false;
            }
            
            log.info("数据库恢复完成");
            return true;

        } catch (Exception e) {
            log.error("数据库恢复失败", e);
            return false;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    @Override
    public byte[] downloadBackup(Long backupId) {
        try {
            List<Map<String, Object>> backupList = getBackupList();
            if (backupId <= 0 || backupId > backupList.size()) {
                return null;
            }

            Map<String, Object> backup = backupList.get(backupId.intValue() - 1);
            String filePath = (String) backup.get("filePath");

            File file = new File(filePath);
            if (!file.exists()) {
                return null;
            }

            return Files.readAllBytes(file.toPath());

        } catch (Exception e) {
            log.error("下载备份文件失败", e);
            return null;
        }
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }
}
