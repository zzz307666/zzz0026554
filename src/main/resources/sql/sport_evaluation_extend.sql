-- ========================================
-- 运动评估系统 - 数据库扩展脚本
-- ========================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 运动类型表
-- ----------------------------
DROP TABLE IF EXISTS `sport_type`;
CREATE TABLE `sport_type` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '运动类型ID',
  `type_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '运动类型名称',
  `type_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '类型编码',
  `base_points` decimal(5,2) NOT NULL DEFAULT 10.00 COMMENT '基础积分',
  `coefficient` decimal(3,2) NOT NULL DEFAULT 1.00 COMMENT '积分系数',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '分钟' COMMENT '计量单位',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '图标',
  `sort_order` int(0) NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态:0-禁用 1-启用',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '描述',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_type_code`(`type_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '运动类型表' ROW_FORMAT = Dynamic;

-- 初始化运动类型数据
INSERT INTO `sport_type` (`id`, `type_name`, `type_code`, `base_points`, `coefficient`, `unit`, `icon`, `sort_order`, `status`, `description`) VALUES 
(1, '跑步', 'RUNNING', 10.00, 1.00, '分钟', '🏃', 1, 1, '户外或室内跑步运动'),
(2, '跳绳', 'JUMP_ROPE', 8.00, 1.20, '分钟', '⚡', 2, 1, '跳绳运动'),
(3, '健身', 'FITNESS', 12.00, 1.10, '分钟', '💪', 3, 1, '健身房训练'),
(4, '游泳', 'SWIMMING', 15.00, 1.30, '分钟', '🏊', 4, 1, '游泳运动'),
(5, '篮球', 'BASKETBALL', 10.00, 1.15, '分钟', '🏀', 5, 1, '篮球运动'),
(6, '足球', 'FOOTBALL', 10.00, 1.15, '分钟', '⚽', 6, 1, '足球运动'),
(7, '羽毛球', 'BADMINTON', 9.00, 1.10, '分钟', '🏸', 7, 1, '羽毛球运动'),
(8, '乒乓球', 'TABLE_TENNIS', 8.00, 1.05, '分钟', '🏓', 8, 1, '乒乓球运动'),
(9, '瑜伽', 'YOGA', 10.00, 1.00, '分钟', '🧘', 9, 1, '瑜伽练习'),
(10, '骑行', 'CYCLING', 11.00, 1.20, '分钟', '🚴', 10, 1, '自行车骑行');

-- ----------------------------
-- 2. 运动打卡记录表
-- ----------------------------
DROP TABLE IF EXISTS `sport_record`;
CREATE TABLE `sport_record` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `student_id` bigint(0) NOT NULL COMMENT '学生ID',
  `sport_type_id` bigint(0) NOT NULL COMMENT '运动类型ID',
  `record_date` date NOT NULL COMMENT '运动日期',
  `duration` int(0) NULL DEFAULT 0 COMMENT '运动时长(分钟)',
  `distance` decimal(10,2) NULL DEFAULT 0.00 COMMENT '运动距离(米)',
  `calories` int(0) NULL DEFAULT 0 COMMENT '消耗卡路里',
  `steps` int(0) NULL DEFAULT 0 COMMENT '步数',
  `heart_rate` int(0) NULL DEFAULT 0 COMMENT '平均心率',
  `data_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '扩展数据(JSON格式)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '备注',
  `status` tinyint(0) NOT NULL DEFAULT 0 COMMENT '状态:0-待审核 1-已通过 2-已驳回',
  `audit_teacher_id` bigint(0) NULL DEFAULT NULL COMMENT '审核教师ID',
  `audit_time` datetime(0) NULL DEFAULT NULL COMMENT '审核时间',
  `audit_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '审核备注',
  `earned_points` decimal(8,2) NULL DEFAULT 0.00 COMMENT '获得积分',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_id`(`student_id`) USING BTREE,
  INDEX `idx_record_date`(`record_date`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_sport_type`(`sport_type_id`) USING BTREE,
  CONSTRAINT `fk_record_student` FOREIGN KEY (`student_id`) REFERENCES `sys_student` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_record_sport_type` FOREIGN KEY (`sport_type_id`) REFERENCES `sport_type` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_record_audit_teacher` FOREIGN KEY (`audit_teacher_id`) REFERENCES `sys_teacher` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '运动打卡记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 3. 5维评价指标表
-- ----------------------------
DROP TABLE IF EXISTS `evaluation_dimension`;
CREATE TABLE `evaluation_dimension` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '维度ID',
  `dimension_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '维度名称',
  `dimension_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '维度编码',
  `weight` decimal(3,2) NOT NULL DEFAULT 0.20 COMMENT '权重(0-1)',
  `max_score` decimal(5,2) NOT NULL DEFAULT 100.00 COMMENT '满分',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '描述',
  `sort_order` int(0) NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态:0-禁用 1-启用',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dimension_code`(`dimension_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '评价维度表' ROW_FORMAT = Dynamic;

-- 初始化5维评价指标
INSERT INTO `evaluation_dimension` (`id`, `dimension_name`, `dimension_code`, `weight`, `max_score`, `description`, `sort_order`, `status`) VALUES 
(1, '耐力', 'ENDURANCE', 0.25, 100.00, '持续运动的能力', 1, 1),
(2, '力量', 'STRENGTH', 0.20, 100.00, '肌肉力量和爆发力', 2, 1),
(3, '速度', 'SPEED', 0.20, 100.00, '快速运动的能力', 3, 1),
(4, '柔韧', 'FLEXIBILITY', 0.15, 100.00, '关节活动范围', 4, 1),
(5, '协调', 'COORDINATION', 0.20, 100.00, '身体协调控制能力', 5, 1);

-- ----------------------------
-- 4. 学生评价记录表
-- ----------------------------
DROP TABLE IF EXISTS `student_evaluation`;
CREATE TABLE `student_evaluation` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `student_id` bigint(0) NOT NULL COMMENT '学生ID',
  `teacher_id` bigint(0) NOT NULL COMMENT '评价教师ID',
  `evaluation_period` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '评价周期(如:2024-2025-1)',
  `endurance_score` decimal(5,2) NULL DEFAULT 0.00 COMMENT '耐力评分',
  `strength_score` decimal(5,2) NULL DEFAULT 0.00 COMMENT '力量评分',
  `speed_score` decimal(5,2) NULL DEFAULT 0.00 COMMENT '速度评分',
  `flexibility_score` decimal(5,2) NULL DEFAULT 0.00 COMMENT '柔韧评分',
  `coordination_score` decimal(5,2) NULL DEFAULT 0.00 COMMENT '协调评分',
  `total_score` decimal(5,2) NULL DEFAULT 0.00 COMMENT '综合总分',
  `grade_level` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '等级(优秀/良好/中等/及格/不及格)',
  `teacher_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '教师评语',
  `status` tinyint(0) NOT NULL DEFAULT 0 COMMENT '状态:0-草稿 1-已发布',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_id`(`student_id`) USING BTREE,
  INDEX `idx_teacher_id`(`teacher_id`) USING BTREE,
  INDEX `idx_period`(`evaluation_period`) USING BTREE,
  CONSTRAINT `fk_eval_student` FOREIGN KEY (`student_id`) REFERENCES `sys_student` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_eval_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `sys_teacher` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学生评价记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 5. 学生积分表
-- ----------------------------
DROP TABLE IF EXISTS `student_points`;
CREATE TABLE `student_points` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '积分记录ID',
  `student_id` bigint(0) NOT NULL COMMENT '学生ID',
  `points_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '积分类型(MOTION:运动/EVALUATION:评价/BONUS:奖励/PENALTY:扣分)',
  `points_value` decimal(8,2) NOT NULL COMMENT '积分值(正数为增加，负数为减少)',
  `related_id` bigint(0) NULL DEFAULT NULL COMMENT '关联业务ID',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '积分说明',
  `operator_id` bigint(0) NULL DEFAULT NULL COMMENT '操作人ID',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_id`(`student_id`) USING BTREE,
  INDEX `idx_points_type`(`points_type`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  CONSTRAINT `fk_points_student` FOREIGN KEY (`student_id`) REFERENCES `sys_student` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学生积分表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 6. 积分规则配置表
-- ----------------------------
DROP TABLE IF EXISTS `points_rule`;
CREATE TABLE `points_rule` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `rule_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '规则名称',
  `rule_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '规则编码',
  `rule_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '规则类型(BASE:基础/BONUS:奖励/PENALTY:扣分)',
  `points_value` decimal(8,2) NOT NULL DEFAULT 0.00 COMMENT '积分值',
  `condition_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '触发条件(JSON)',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '规则描述',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态:0-禁用 1-启用',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_rule_code`(`rule_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '积分规则配置表' ROW_FORMAT = Dynamic;

-- 初始化积分规则
INSERT INTO `points_rule` (`id`, `rule_name`, `rule_code`, `rule_type`, `points_value`, `condition_json`, `description`, `status`) VALUES 
(1, '运动打卡基础积分', 'MOTION_BASE', 'BASE', 10.00, '{"min_duration": 30}', '每次运动打卡基础积分（至少30分钟）', 1),
(2, '连续打卡奖励', 'CONTINUOUS_BONUS', 'BONUS', 5.00, '{"continuous_days": 7}', '连续打卡7天额外奖励5积分', 1),
(3, '月度运动达人', 'MONTHLY_EXCELLENT', 'BONUS', 20.00, '{"monthly_records": 20}', '单月打卡超过20次奖励20积分', 1),
(4, '数据造假惩罚', 'DATA_FRAUD', 'PENALTY', -50.00, '{}', '发现数据造假扣50积分', 1),
(5, '评价优秀奖励', 'EVALUATION_EXCELLENT', 'BONUS', 30.00, '{"grade_level": "优秀"}', '五维评价获得优秀等级奖励30积分', 1),
(6, '评价良好奖励', 'EVALUATION_GOOD', 'BONUS', 20.00, '{"grade_level": "良好"}', '五维评价获得良好等级奖励20积分', 1),
(7, '评价中等奖励', 'EVALUATION_MEDIUM', 'BONUS', 10.00, '{"grade_level": "中等"}', '五维评价获得中等等级奖励10积分', 1),
(8, '评价及格奖励', 'EVALUATION_PASS', 'BONUS', 5.00, '{"grade_level": "及格"}', '五维评价获得及格等级奖励5积分', 1);

-- ----------------------------
-- 7. 系统操作日志表
-- ----------------------------
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint(0) NULL DEFAULT NULL COMMENT '操作用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '用户名',
  `operation` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '操作描述',
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '模块名称',
  `method` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '请求方法',
  `params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '请求参数',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT 'IP地址',
  `result` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '操作结果(SUCCESS/FAIL)',
  `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '错误信息',
  `duration` bigint(0) NULL DEFAULT 0 COMMENT '执行时长(毫秒)',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_module`(`module`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 8. 系统公告表
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告内容',
  `notice_type` tinyint(0) NOT NULL DEFAULT 1 COMMENT '公告类型:1-系统公告 2-活动通知 3-重要提醒',
  `priority` tinyint(0) NOT NULL DEFAULT 0 COMMENT '优先级:0-普通 1-重要 2-紧急',
  `publisher_id` bigint(0) NOT NULL COMMENT '发布人ID',
  `publish_time` datetime(0) NULL DEFAULT NULL COMMENT '发布时间',
  `expire_time` datetime(0) NULL DEFAULT NULL COMMENT '过期时间',
  `status` tinyint(0) NOT NULL DEFAULT 0 COMMENT '状态:0-草稿 1-已发布 2-已下架',
  `view_count` int(0) NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_publish_time`(`publish_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 9. 学期配置表
-- ----------------------------
DROP TABLE IF EXISTS `sys_semester`;
CREATE TABLE `sys_semester` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '学期ID',
  `semester_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '学期名称(如:2024-2025学年第一学期)',
  `semester_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '学期编码(如:2024-2025-1)',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `is_current` tinyint(0) NOT NULL DEFAULT 0 COMMENT '是否当前学期:0-否 1-是',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态:0-禁用 1-启用',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_semester_code`(`semester_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学期配置表' ROW_FORMAT = Dynamic;

-- 初始化学期数据
INSERT INTO `sys_semester` (`id`, `semester_name`, `semester_code`, `start_date`, `end_date`, `is_current`, `status`) VALUES 
(1, '2024-2025学年第一学期', '2024-2025-1', '2024-09-01', '2025-01-15', 0, 1),
(2, '2024-2025学年第二学期', '2024-2025-2', '2025-02-17', '2025-07-10', 1, 1);

SET FOREIGN_KEY_CHECKS = 1;
