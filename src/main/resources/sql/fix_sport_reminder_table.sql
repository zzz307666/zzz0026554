-- ============================================
-- 修复 sport_reminder 表结构
-- 添加缺失的 is_active 字段
-- ============================================

-- 检查并添加 is_active 字段
ALTER TABLE sport_reminder 
ADD COLUMN IF NOT EXISTS is_active TINYINT DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用' AFTER reminder_time;

-- 添加索引
ALTER TABLE sport_reminder 
ADD INDEX IF NOT EXISTS idx_is_active (is_active);

-- 为现有数据设置默认值
UPDATE sport_reminder SET is_active = 1 WHERE is_active IS NULL;
