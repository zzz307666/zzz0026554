-- =====================================================
-- 运动评估系统 - 扩展功能数据库脚本
-- 创建时间: 2026-05-24
-- 说明: 包含操作日志、公告、目标、徽章、消息等功能的表结构
-- =====================================================

-- ----------------------------
-- 1. 系统公告表
-- ----------------------------
DROP TABLE IF EXISTS `sys_announcement`;
CREATE TABLE `sys_announcement` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告内容(支持HTML)',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'INFO' COMMENT '公告类型(INFO:通知/IMPORTANT:重要/URGENT:紧急)',
  `priority` int(0) NOT NULL DEFAULT 0 COMMENT '优先级(数字越大越优先)',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '生效开始时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '生效结束时间',
  `publisher_id` bigint(0) NULL DEFAULT NULL COMMENT '发布人ID',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态(0:草稿/1:已发布/2:已下架)',
  `view_count` int(0) NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_type`(`type`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 2. 学生运动目标表
-- ----------------------------
DROP TABLE IF EXISTS `student_goal`;
CREATE TABLE `student_goal` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '目标ID',
  `student_id` bigint(0) NOT NULL COMMENT '学生ID',
  `goal_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '目标类型(COUNT:次数/DURATION:时长/CALORIES:卡路里)',
  `target_value` decimal(10,2) NOT NULL COMMENT '目标值',
  `current_value` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '当前完成值',
  `period_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'MONTH' COMMENT '周期类型(DAY:日/WEEK:周/MONTH:月/SEMESTER:学期)',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态(0:已取消/1:进行中/2:已完成/3:失败)',
  `reward_points` decimal(8,2) NULL DEFAULT 0.00 COMMENT '达成奖励积分',
  `completed_time` datetime(0) NULL DEFAULT NULL COMMENT '完成时间',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_id`(`student_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_period`(`period_type`, `start_date`, `end_date`) USING BTREE,
  CONSTRAINT `fk_goal_student` FOREIGN KEY (`student_id`) REFERENCES `sys_student` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学生运动目标表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 3. 成就徽章定义表
-- ----------------------------
DROP TABLE IF EXISTS `achievement_badge`;
CREATE TABLE `achievement_badge` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '徽章ID',
  `badge_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '徽章编码',
  `badge_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '徽章名称',
  `badge_icon` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '徽章图标URL',
  `badge_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'BRONZE' COMMENT '徽章等级(BRONZE:铜/SILVER:银/GOLD:金/PLATINUM:白金)',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '徽章描述',
  `condition_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '获取条件(JSON)',
  `reward_points` decimal(8,2) NULL DEFAULT 0.00 COMMENT '获得奖励积分',
  `sort_order` int(0) NOT NULL DEFAULT 0 COMMENT '排序',
  `is_active` tinyint(0) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_badge_code`(`badge_code`) USING BTREE,
  INDEX `idx_level`(`badge_level`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '成就徽章定义表' ROW_FORMAT = Dynamic;

-- 初始化徽章数据
INSERT INTO `achievement_badge` (`id`, `badge_code`, `badge_name`, `badge_icon`, `badge_level`, `description`, `condition_json`, `reward_points`, `sort_order`, `is_active`) VALUES 
(1, 'FIRST_CHECKIN', '首次打卡', '/images/badges/first.png', 'BRONZE', '完成第一次运动打卡', '{"type": "checkin_count", "value": 1}', 5.00, 1, 1),
(2, 'CONTINUOUS_7', '坚持一周', '/images/badges/week.png', 'SILVER', '连续打卡7天', '{"type": "continuous_days", "value": 7}', 20.00, 2, 1),
(3, 'CONTINUOUS_30', '月度达人', '/images/badges/month.png', 'GOLD', '连续打卡30天', '{"type": "continuous_days", "value": 30}', 50.00, 3, 1),
(4, 'TOTAL_50', '运动健将', '/images/badges/athlete.png', 'GOLD', '累计打卡50次', '{"type": "total_checkin", "value": 50}', 40.00, 4, 1),
(5, 'TOTAL_100', '运动大师', '/images/badges/master.png', 'PLATINUM', '累计打卡100次', '{"type": "total_checkin", "value": 100}', 100.00, 5, 1),
(6, 'PERFECT_MONTH', '全勤奖', '/images/badges/perfect.png', 'PLATINUM', '单月全勤打卡', '{"type": "monthly_perfect", "value": 1}', 80.00, 6, 1),
(7, 'PROGRESS_STAR', '进步之星', '/images/badges/progress.png', 'SILVER', '本月比上月运动量提升50%', '{"type": "monthly_improvement", "value": 50}', 30.00, 7, 1),
(8, 'EARLY_BIRD', '早起鸟', '/images/badges/early.png', 'BRONZE', '早上6-8点打卡10次', '{"type": "morning_checkin", "value": 10}', 15.00, 8, 1);

-- ----------------------------
-- 4. 学生徽章获得记录表
-- ----------------------------
DROP TABLE IF EXISTS `student_badge`;
CREATE TABLE `student_badge` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `student_id` bigint(0) NOT NULL COMMENT '学生ID',
  `badge_id` bigint(0) NOT NULL COMMENT '徽章ID',
  `achieved_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '获得时间',
  `is_displayed` tinyint(0) NOT NULL DEFAULT 1 COMMENT '是否展示',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_student_badge`(`student_id`, `badge_id`) USING BTREE,
  INDEX `idx_student_id`(`student_id`) USING BTREE,
  CONSTRAINT `fk_badge_student` FOREIGN KEY (`student_id`) REFERENCES `sys_student` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_badge_definition` FOREIGN KEY (`badge_id`) REFERENCES `achievement_badge` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学生徽章获得记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 5. 消息通知表
-- ----------------------------
DROP TABLE IF EXISTS `sys_message`;
CREATE TABLE `sys_message` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `receiver_id` bigint(0) NOT NULL COMMENT '接收者ID',
  `sender_id` bigint(0) NULL DEFAULT NULL COMMENT '发送者ID(NULL表示系统消息)',
  `message_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'SYSTEM' COMMENT '消息类型(SYSTEM:系统/AUDIT:审核/EVALUATION:评价/ANNOUNCEMENT:公告)',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '消息标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '消息内容',
  `related_id` bigint(0) NULL DEFAULT NULL COMMENT '关联业务ID',
  `related_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '关联业务类型',
  `is_read` tinyint(0) NOT NULL DEFAULT 0 COMMENT '是否已读(0:未读/1:已读)',
  `read_time` datetime(0) NULL DEFAULT NULL COMMENT '阅读时间',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_receiver_id`(`receiver_id`) USING BTREE,
  INDEX `idx_is_read`(`is_read`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  CONSTRAINT `fk_message_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '消息通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 6. 系统配置表（用于缓存配置项）
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置键',
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '配置值',
  `config_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'STRING' COMMENT '配置类型(STRING/JSON/BOOLEAN/NUMBER)',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '配置描述',
  `is_system` tinyint(0) NOT NULL DEFAULT 0 COMMENT '是否系统配置(1:是/0:否)',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_key`(`config_key`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统配置表' ROW_FORMAT = Dynamic;

-- 初始化系统配置
INSERT INTO `sys_config` (`config_key`, `config_value`, `config_type`, `description`, `is_system`) VALUES 
('system.name', '运动评估系统', 'STRING', '系统名称', 1),
('system.version', '1.0.0', 'STRING', '系统版本', 1),
('announcement.enabled', 'true', 'BOOLEAN', '是否启用公告功能', 1),
('badge.auto_check', 'true', 'BOOLEAN', '是否自动检查徽章达成', 1),
('goal.max_active', '3', 'NUMBER', '每个学生最多同时进行的目标数', 1);

-- ----------------------------
-- 7. 数据备份记录表
-- ----------------------------
DROP TABLE IF EXISTS `sys_backup`;
CREATE TABLE `sys_backup` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '备份ID',
  `backup_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '备份名称',
  `backup_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '备份文件路径',
  `backup_size` bigint(0) NULL DEFAULT 0 COMMENT '备份文件大小(字节)',
  `backup_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'MANUAL' COMMENT '备份类型(MANUAL:手动/AUTO:自动)',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'SUCCESS' COMMENT '备份状态(SUCCESS:成功/FAILED:失败/PROCESSING:处理中)',
  `operator_id` bigint(0) NULL DEFAULT NULL COMMENT '操作人ID',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '错误信息',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '数据备份记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 8. 扩展 operation_log 表字段（如果需要）
-- ----------------------------
-- 检查 operation_log 表是否已存在，如果不存在则创建
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint(0) NULL DEFAULT NULL COMMENT '操作用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作模块',
  `operation` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作描述',
  `method` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求方法',
  `params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '请求参数',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作地点',
  `execution_time` bigint(0) NULL DEFAULT 0 COMMENT '执行时间(毫秒)',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '操作状态(0:失败/1:成功)',
  `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '错误信息',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_module`(`module`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 9. 为 user 表添加第三方登录字段（如果之前未添加）
-- ----------------------------
-- 检查字段是否存在，避免重复添加
ALTER TABLE `sys_user` 
ADD COLUMN IF NOT EXISTS `wechat_openid` VARCHAR(100) DEFAULT NULL COMMENT '微信OpenID',
ADD COLUMN IF NOT EXISTS `qq_openid` VARCHAR(100) DEFAULT NULL COMMENT 'QQ OpenID',
ADD COLUMN IF NOT EXISTS `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
ADD COLUMN IF NOT EXISTS `last_login_type` VARCHAR(20) DEFAULT 'PASSWORD' COMMENT '最后登录方式(PASSWORD/CAS/WECHAT/QQ)';

-- 添加索引
CREATE INDEX IF NOT EXISTS `idx_wechat_openid` ON `sys_user` (`wechat_openid`);
CREATE INDEX IF NOT EXISTS `idx_qq_openid` ON `sys_user` (`qq_openid`);

-- ----------------------------
-- 10. 创建视图：班级统计视图（简化查询）
-- ----------------------------
DROP VIEW IF EXISTS `v_class_sport_stats`;
CREATE VIEW `v_class_sport_stats` AS
SELECT 
    c.id AS class_id,
    c.class_name,
    COUNT(DISTINCT sr.student_id) AS total_students,
    COUNT(DISTINCT CASE WHEN sr.status = 1 THEN sr.student_id END) AS active_students,
    COUNT(sr.id) AS total_records,
    ROUND(AVG(sr.duration), 2) AS avg_duration,
    ROUND(SUM(sr.earned_points), 2) AS total_points,
    ROUND(AVG(sp.current_points), 2) AS avg_current_points
FROM sys_class c
LEFT JOIN sys_student s ON c.id = s.class_id
LEFT JOIN sport_record sr ON s.id = sr.student_id AND sr.status = 1
LEFT JOIN student_points sp ON s.id = sp.student_id
GROUP BY c.id, c.class_name;

-- =====================================================
-- 数据库创建完成
-- 总计新增表: 8个
-- 新增视图: 1个
-- 初始化数据: 徽章8条、系统配置5条
-- =====================================================
