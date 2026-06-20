package com.hhjt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hhjt.entity.SportRecord;
import com.hhjt.entity.SportReminder;
import com.hhjt.mapper.SportRecordMapper;
import com.hhjt.mapper.SportReminderMapper;
import com.hhjt.entity.SysMessage;
import com.hhjt.service.SportReminderService;
import com.hhjt.service.SysMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 运动打卡提醒服务实现类 - 真实数据版本
 */
@Slf4j
@Service
public class SportReminderServiceImpl implements SportReminderService {

    @Autowired
    private SportReminderMapper reminderMapper;

    @Autowired
    private SportRecordMapper recordMapper;

    @Autowired
    private SysMessageService messageService;

    @Override
    public List<Map<String, Object>> getUserReminders(Long userId) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            // 查询用户的提醒设置
            QueryWrapper<SportReminder> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId);
            List<SportReminder> reminders = reminderMapper.selectList(wrapper);
            
            for (SportReminder reminder : reminders) {
                Map<String, Object> reminderData = new HashMap<>();
                reminderData.put("id", reminder.getId());
                reminderData.put("reminderType", reminder.getReminderType().toLowerCase());
                reminderData.put("reminderTime", reminder.getReminderTime());
                reminderData.put("isEnabled", reminder.getIsActive() == 1);
                reminderData.put("messageTemplate", reminder.getMessage());
                reminderData.put("reminderDays", reminder.getReminderDays());
                
                result.add(reminderData);
            }
        } catch (Exception e) {
            log.error("获取用户提醒设置失败", e);
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveReminder(Long userId, String reminderType, String reminderTime, 
                               Boolean isEnabled, String messageTemplate) {
        try {
            // 检查是否已存在同类型的提醒
            QueryWrapper<SportReminder> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId)
                   .eq("reminder_type", reminderType.toUpperCase());
            
            SportReminder existing = reminderMapper.selectOne(wrapper);
            
            if (existing != null) {
                // 更新现有提醒
                existing.setReminderTime(reminderTime);
                existing.setIsActive(isEnabled ? 1 : 0);
                existing.setMessage(messageTemplate);
                reminderMapper.updateById(existing);
            } else {
                // 创建新提醒
                SportReminder reminder = new SportReminder();
                reminder.setUserId(userId);
                reminder.setReminderType(reminderType.toUpperCase());
                reminder.setReminderTime(reminderTime);
                reminder.setIsActive(isEnabled ? 1 : 0);
                reminder.setMessage(messageTemplate);
                reminder.setReminderDays("1,2,3,4,5,6,7"); // 默认每天都提醒
                reminderMapper.insert(reminder);
            }
            
            log.info("保存提醒配置成功：userId={}, type={}", userId, reminderType);
            return true;
        } catch (Exception e) {
            log.error("保存提醒配置失败", e);
            return false;
        }
    }

    @Override
    public void sendDailyReminders() {
        // 这个方法由定时任务调用，不再手动调用
        log.debug("每日提醒由定时任务自动发送");
    }

    @Override
    public void checkInterruptReminders() {
        try {
            // 查询所有启用的中断预警提醒
            QueryWrapper<SportReminder> wrapper = new QueryWrapper<>();
            wrapper.eq("reminder_type", "INTERRUPT")
                   .eq("is_active", 1);
            List<SportReminder> reminders = reminderMapper.selectList(wrapper);
            
            for (SportReminder reminder : reminders) {
                Long userId = reminder.getUserId();
                
                // 获取用户最近的运动记录
                QueryWrapper<SportRecord> recordWrapper = new QueryWrapper<>();
                recordWrapper.eq("student_id", userId)
                           .orderByDesc("record_date");
                List<SportRecord> records = recordMapper.selectList(recordWrapper);
                
                if (!records.isEmpty()) {
                    LocalDate lastCheckInDate = records.get(0).getRecordDate();
                    LocalDate today = LocalDate.now();
                    long daysSinceLastCheckIn = ChronoUnit.DAYS.between(lastCheckInDate, today);
                    
                    // 如果连续2天未打卡，发送提醒
                    if (daysSinceLastCheckIn >= 2) {
                        String message = reminder.getMessage() != null ? 
                            reminder.getMessage() : "您已连续2天未打卡，坚持就是胜利！";
                        sendReminderMessage(userId, "INTERRUPT", message);
                        log.info("发送中断预警提醒给用户: {}", userId);
                    }
                }
            }
            
            log.debug("中断预警检查完成");
        } catch (Exception e) {
            log.error("检查中断预警失败", e);
        }
    }

    /**
     * 生成并发送每周运动总结
     */
    public void sendWeeklySummary() {
        try {
            // 查询所有启用的每周总结提醒
            QueryWrapper<SportReminder> wrapper = new QueryWrapper<>();
            wrapper.eq("reminder_type", "WEEKLY")
                   .eq("is_active", 1);
            List<SportReminder> reminders = reminderMapper.selectList(wrapper);
            
            for (SportReminder reminder : reminders) {
                Long userId = reminder.getUserId();
                String summary = generateWeeklySummary(userId);
                
                if (summary != null && !summary.isEmpty()) {
                    sendReminderMessage(userId, "WEEKLY", summary);
                    log.info("发送每周运动总结给用户: {}", userId);
                }
            }
            
            log.debug("每周运动总结发送完成");
        } catch (Exception e) {
            log.error("发送每周运动总结失败", e);
        }
    }

    /**
     * 生成用户的每周运动总结
     */
    private String generateWeeklySummary(Long userId) {
        try {
            // 获取上周的数据（周一到周日）
            LocalDate today = LocalDate.now();
            LocalDate lastMonday = today.minusWeeks(1).with(java.time.DayOfWeek.MONDAY);
            LocalDate lastSunday = lastMonday.plusDays(6);
            
            QueryWrapper<SportRecord> wrapper = new QueryWrapper<>();
            wrapper.eq("student_id", userId)
                   .ge("record_date", lastMonday)
                   .le("record_date", lastSunday);
            List<SportRecord> records = recordMapper.selectList(wrapper);
            
            if (records.isEmpty()) {
                return null;
            }
            
            // 计算统计数据
            int totalDays = (int) records.stream()
                .map(SportRecord::getRecordDate)
                .distinct()
                .count();
            
            int totalRecords = records.size();
            
            int totalDuration = records.stream()
                .mapToInt(r -> r.getDuration() != null ? r.getDuration() : 0)
                .sum();
            
            int totalCalories = records.stream()
                .mapToInt(r -> r.getCalories() != null ? r.getCalories() : 0)
                .sum();
            
            BigDecimal totalDistance = records.stream()
                .filter(r -> r.getDistance() != null)
                .map(SportRecord::getDistance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            long totalSteps = records.stream()
                .mapToLong(r -> r.getSteps() != null ? r.getSteps() : 0)
                .sum();
            
            // 生成总结消息
            StringBuilder summary = new StringBuilder();
            summary.append("📊 【本周运动总结】\n\n");
            summary.append(String.format("🏃 本周运动 %d 天，共 %d 次\n", totalDays, totalRecords));
            summary.append(String.format("⏱️ 累计运动时长：%d 分钟\n", totalDuration));
            summary.append(String.format("🔥 消耗热量：%d 千卡\n", totalCalories));
            
            if (totalDistance.compareTo(BigDecimal.ZERO) > 0) {
                summary.append(String.format("🚶 累计距离：%.2f 公里\n", totalDistance.doubleValue()));
            }
            
            if (totalSteps > 0) {
                summary.append(String.format("👣 累计步数：%d 步\n", totalSteps));
            }
            
            // 添加鼓励语
            if (totalDays >= 5) {
                summary.append("\n🌟 太棒了！本周运动非常规律，继续保持！");
            } else if (totalDays >= 3) {
                summary.append("\n💪 不错的一周！再多坚持几天会更好哦！");
            } else {
                summary.append("\n📝 下周加油！运动健康，从每一天开始！");
            }
            
            return summary.toString();
        } catch (Exception e) {
            log.error("生成每周总结失败", e);
            return null;
        }
    }

    /**
     * 发送提醒消息
     */
    private void sendReminderMessage(Long userId, String type, String content) {
        try {
            SysMessage message = new SysMessage();
            message.setReceiverId(userId);
            message.setMessageType("REMINDER");
            
            String title;
            if ("DAILY".equals(type.toUpperCase())) {
                title = "⏰ 每日运动提醒";
            } else if ("INTERRUPT".equals(type.toUpperCase())) {
                title = "⚠️ 打卡中断预警";
            } else if ("WEEKLY".equals(type.toUpperCase())) {
                title = "📊 每周运动总结";
            } else {
                title = "⏰ 运动提醒";
            }
            
            message.setTitle(title);
            message.setContent(content);
            message.setIsRead(0);
            message.setCreateTime(LocalDateTime.now());
            
            messageService.sendMessage(message);
            
            log.debug("发送{}提醒给用户 {}", type, userId);
        } catch (Exception e) {
            log.error("发送提醒消息失败", e);
        }
    }

    @Override
    public Map<String, Object> getCheckInStats(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 查询用户的所有运动记录
            QueryWrapper<SportRecord> wrapper = new QueryWrapper<>();
            wrapper.eq("student_id", userId)
                   .orderByDesc("record_date");
            
            List<SportRecord> records = recordMapper.selectList(wrapper);
            
            if (records.isEmpty()) {
                result.put("totalCheckIns", 0);
                result.put("currentStreak", 0);
                result.put("longestStreak", 0);
                result.put("thisMonthCheckIns", 0);
                result.put("lastCheckInDate", null);
                result.put("missedDays", 0);
                return result;
            }
            
            // 总打卡次数
            int totalCheckIns = records.size();
            result.put("totalCheckIns", totalCheckIns);
            
            // 最后一次打卡日期
            LocalDate lastCheckInDate = records.get(0).getRecordDate();
            result.put("lastCheckInDate", lastCheckInDate);
            
            // 计算连续打卡天数
            int currentStreak = calculateCurrentStreak(records);
            result.put("currentStreak", currentStreak);
            
            // 计算最长连续打卡
            int longestStreak = calculateLongestStreak(records);
            result.put("longestStreak", longestStreak);
            
            // 本月打卡次数
            LocalDate now = LocalDate.now();
            LocalDate firstDayOfMonth = now.withDayOfMonth(1);
            long thisMonthCheckIns = records.stream()
                .filter(r -> !r.getRecordDate().isBefore(firstDayOfMonth))
                .count();
            result.put("thisMonthCheckIns", thisMonthCheckIns);
            
            // 计算错过的天数（简化处理）
            if (!records.isEmpty()) {
                LocalDate earliestDate = records.get(records.size() - 1).getRecordDate();
                long totalDays = ChronoUnit.DAYS.between(earliestDate, now) + 1;
                long missedDays = totalDays - totalCheckIns;
                result.put("missedDays", Math.max(0, missedDays));
            } else {
                result.put("missedDays", 0);
            }
            
        } catch (Exception e) {
            log.error("获取打卡统计失败", e);
        }
        
        return result;
    }
    
    /**
     * 计算当前连续打卡天数
     */
    private int calculateCurrentStreak(List<SportRecord> records) {
        if (records.isEmpty()) return 0;
        
        Set<LocalDate> uniqueDates = records.stream()
            .map(SportRecord::getRecordDate)
            .collect(Collectors.toSet());
        
        List<LocalDate> sortedDates = uniqueDates.stream()
            .sorted(LocalDate::compareTo)
            .collect(Collectors.toList());
        
        int streak = 0;
        LocalDate today = LocalDate.now();
        LocalDate checkDate = today;
        
        // 从今天往前数连续的天数
        while (uniqueDates.contains(checkDate)) {
            streak++;
            checkDate = checkDate.minusDays(1);
        }
        
        return streak;
    }
    
    /**
     * 计算最长连续打卡天数
     */
    private int calculateLongestStreak(List<SportRecord> records) {
        if (records.isEmpty()) return 0;
        
        Set<LocalDate> uniqueDates = records.stream()
            .map(SportRecord::getRecordDate)
            .collect(Collectors.toSet());
        
        List<LocalDate> sortedDates = uniqueDates.stream()
            .sorted(LocalDate::compareTo)
            .collect(Collectors.toList());
        
        int maxStreak = 1;
        int currentStreak = 1;
        
        for (int i = 1; i < sortedDates.size(); i++) {
            if (sortedDates.get(i).minusDays(1).equals(sortedDates.get(i - 1))) {
                currentStreak++;
                maxStreak = Math.max(maxStreak, currentStreak);
            } else {
                currentStreak = 1;
            }
        }
        
        return maxStreak;
    }
}