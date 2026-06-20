package com.hhjt.service;

import java.util.List;
import java.util.Map;

/**
 * 数据备份服务接口
 */
public interface DataBackupService {
    
    /**
     * 手动备份数据库
     * @param backupName 备份名称
     * @return 备份文件路径
     */
    String manualBackup(String backupName);
    
    /**
     * 获取备份文件列表
     * @return 备份文件信息列表
     */
    List<Map<String, Object>> getBackupList();
    
    /**
     * 删除备份文件
     * @param backupId 备份ID
     * @return 是否成功
     */
    boolean deleteBackup(Long backupId);
    
    /**
     * 恢复数据库
     * @param backupId 备份ID
     * @return 是否成功
     */
    boolean restoreDatabase(Long backupId);
    
    /**
     * 下载备份文件
     * @param backupId 备份ID
     * @return 备份文件字节数组
     */
    byte[] downloadBackup(Long backupId);
}
