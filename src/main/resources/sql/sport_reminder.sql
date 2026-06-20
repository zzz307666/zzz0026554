-- ============================================
-- 运动提醒功能表结构
-- ============================================

CREATE TABLE IF NOT EXISTS sport_reminder (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '提醒ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    reminder_type VARCHAR(20) NOT NULL COMMENT '提醒类型：DAILY-每日提醒，WEEKLY-每周提醒',
    reminder_time VARCHAR(5) NOT NULL COMMENT '提醒时间 HH:mm',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    reminder_days VARCHAR(50) COMMENT '提醒日期（周几，逗号分隔）1,2,3,4,5,6,7',
    message VARCHAR(500) COMMENT '提醒消息内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运动提醒设置表';

-- ============================================
-- 插入示例数据
-- ============================================

-- 为学生用户添加默认每日提醒
INSERT INTO sport_reminder (user_id, reminder_type, reminder_time, is_active, reminder_days, message) 
VALUES 
(1, 'DAILY', '07:00', 1, '1,2,3,4,5,6,7', '早上好！别忘了今天的运动打卡哦~'),
(2, 'DAILY', '07:30', 1, '1,2,3,4,5,6,7', '新的一天开始了，运动起来吧！'),
(3, 'DAILY', '08:00', 1, '1,2,3,4,5,6,7', '早安！坚持运动，健康生活！');
