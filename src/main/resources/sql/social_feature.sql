-- =============================================
-- 同伴社交功能表结构
-- =============================================

-- 1. 好友关系表
CREATE TABLE IF NOT EXISTS `user_friend` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `friend_id` BIGINT NOT NULL COMMENT '好友ID',
    `remark` VARCHAR(50) DEFAULT '' COMMENT '备注名',
    `status` TINYINT DEFAULT 1 COMMENT '状态（0:待确认 1:已好友 2:已拒绝）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_friend` (`user_id`, `friend_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友关系表';

-- 2. 运动挑战表
CREATE TABLE IF NOT EXISTS `sport_challenge` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `challenger_id` BIGINT NOT NULL COMMENT '挑战者ID',
    `challenged_id` BIGINT NOT NULL COMMENT '被挑战者ID',
    `challenge_type` VARCHAR(50) NOT NULL COMMENT '挑战类型（steps/distance/duration）',
    `target_value` DOUBLE NOT NULL COMMENT '目标值',
    `unit` VARCHAR(20) NOT NULL COMMENT '单位',
    `start_date` DATETIME NOT NULL COMMENT '开始时间',
    `end_date` DATETIME NOT NULL COMMENT '结束时间',
    `status` TINYINT DEFAULT 0 COMMENT '状态（0:进行中 1:已完成 2:已取消）',
    `winner_id` BIGINT COMMENT '获胜者ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_challenger` (`challenger_id`),
    INDEX `idx_challenged` (`challenged_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运动挑战表';

-- 3. 运动分享表
CREATE TABLE IF NOT EXISTS `sport_share` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '分享者ID',
    `user_name` VARCHAR(50) NOT NULL COMMENT '分享者姓名',
    `content` TEXT NOT NULL COMMENT '分享内容',
    `image_url` VARCHAR(500) COMMENT '图片URL',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `comment_count` INT DEFAULT 0 COMMENT '评论数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运动分享表';

-- 注意：测试数据已移除，请通过系统界面创建真实数据