package com.hhjt.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hhjt.entity.SportReminder;
import com.hhjt.entity.SysMessage;
import com.hhjt.mapper.SportReminderMapper;
import com.hhjt.service.SportReminderService;
import com.hhjt.service.SysMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 运动提醒定时任务
 */
@Slf4j
@Component
public class SportReminderTask {

    @Autowired
    private SportReminderMapper reminderMapper;

    @Autowired
    private SysMessageService messageService;

    @Autowired
    private SportReminderService reminderService;

    /**
     * 每分钟检查一次是否有需要发送的提醒
     */
    @Scheduled(cron = "0 * * * * ?")
    public void checkAndSendReminders() {
        try {
            log.debug("开始检查运动提醒...");
            
            // 获取当前时间
            LocalTime now = LocalTime.now();
            String currentTime = now.format(DateTimeFormatter.ofPattern("HH:mm"));
            int currentDayOfWeek = LocalDate.now().getDayOfWeek().getValue(); // 1=Monday, 7=Sunday
            
            // 查询所有启用的提醒
            List<SportReminder> activeReminders = reminderMapper.selectActiveReminders();
            
            int sentCount = 0;
            
            for (SportReminder reminder : activeReminders) {
                // 检查时间是否匹配
                if (!currentTime.equals(reminder.getReminderTime())) {
                    continue;
                }
                
                // 检查日期是否匹配
                if ("WEEKLY".equals(reminder.getReminderType())) {
                    String[] days = reminder.getReminderDays().split(",");
                    boolean matchDay = false;
                    for (String day : days) {
                        if (Integer.parseInt(day.trim()) == currentDayOfWeek) {
                            matchDay = true;
                            break;
                        }
                    }
                    if (!matchDay) {
                        continue;
                    }
                    // 每周提醒调用专门的总结生成方法
                    reminderService.sendWeeklySummary();
                    sentCount++;
                    continue;
                }
                
                // DAILY 和 INTERRUPT 类型使用原有逻辑发送
                sendReminderMessage(reminder);
                sentCount++;
            }
            
            if (sentCount > 0) {
                log.info("已发送 {} 条运动提醒", sentCount);
            }
            
        } catch (Exception e) {
            log.error("检查运动提醒失败", e);
        }
    }

    /**
     * 每天早上9点检查连续打卡中断预警
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void checkInterruptReminders() {
        try {
            log.debug("开始检查连续打卡中断预警...");
            reminderService.checkInterruptReminders();
        } catch (Exception e) {
            log.error("检查中断预警失败", e);
        }
    }

    /**
     * 发送提醒消息
     */
    private void sendReminderMessage(SportReminder reminder) {
        try {
            SysMessage message = new SysMessage();
            message.setReceiverId(reminder.getUserId());
            message.setMessageType("REMINDER");
            message.setTitle("⏰ 运动提醒");
            message.setContent(reminder.getMessage() != null ? 
                reminder.getMessage() : "是时候运动啦！保持健康，从现在开始！💪");
            message.setIsRead(0);
            message.setCreateTime(LocalDateTime.now());
            
            messageService.sendMessage(message);
            
            log.debug("发送提醒给用户 {}: {}", reminder.getUserId(), message.getContent());
            
        } catch (Exception e) {
            log.error("发送提醒消息失败", e);
        }
    }
}