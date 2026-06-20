-- ============================================
-- 修复 sport_reminder 表结构
-- 添加缺失的 message 字段
-- ============================================

-- 检查并添加 message 字段
ALTER TABLE sport_reminder 
ADD COLUMN IF NOT EXISTS message VARCHAR(500) COMMENT '提醒消息内容' AFTER reminder_days;

-- 为现有数据设置默认消息
UPDATE sport_reminder SET message = '别忘了今天的运动打卡哦~' WHERE message IS NULL;