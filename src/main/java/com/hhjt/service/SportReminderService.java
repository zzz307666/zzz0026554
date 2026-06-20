package com.hhjt.service;

import java.util.List;
import java.util.Map;

/**
 * 运动打卡提醒服务接口
 */
public interface SportReminderService {
    
    /**
     * 获取用户的提醒配置
     * @param userId 用户ID
     * @return 提醒配置列表
     */
    List<Map<String, Object>> getUserReminders(Long userId);
    
    /**
     * 保存提醒配置
     * @param userId 用户ID
     * @param reminderType 提醒类型（daily/weekly/interrupt）
     * @param reminderTime 提醒时间
     * @param isEnabled 是否启用
     * @param messageTemplate 消息模板
     * @return 是否成功
     */
    boolean saveReminder(Long userId, String reminderType, String reminderTime, 
                        Boolean isEnabled, String messageTemplate);
    
    /**
     * 发送每日打卡提醒
     */
    void sendDailyReminders();
    
    /**
     * 检查连续打卡中断
     */
    void checkInterruptReminders();
    
    /**
     * 发送每周运动总结
     */
    void sendWeeklySummary();
    
    /**
     * 获取打卡统计
     * @param userId 用户ID
     * @return 打卡统计数据
     */
    Map<String, Object> getCheckInStats(Long userId);
}