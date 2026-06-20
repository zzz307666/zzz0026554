-- =============================================
-- 运动健康知识库表结构
-- =============================================

-- 1. 健康知识文章表
CREATE TABLE IF NOT EXISTS `health_knowledge` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title` VARCHAR(200) NOT NULL COMMENT '文章标题',
    `category` VARCHAR(50) NOT NULL COMMENT '分类（运动科普/健康饮食/损伤预防/专家问答）',
    `content` LONGTEXT COMMENT '文章内容（HTML格式）',
    `summary` VARCHAR(500) COMMENT '文章摘要',
    `cover_image` VARCHAR(500) COMMENT '封面图片URL',
    `view_count` INT DEFAULT 0 COMMENT '浏览次数',
    `status` TINYINT DEFAULT 1 COMMENT '状态（0:草稿 1:发布）',
    `author_id` BIGINT COMMENT '作者ID',
    `author_name` VARCHAR(50) COMMENT '作者姓名',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_category` (`category`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康知识文章表';

-- 2. 插入测试数据
INSERT INTO `health_knowledge` (`title`, `category`, `content`, `summary`, `view_count`, `status`, `author_name`) VALUES
('跑步的正确姿势与技巧', '运动科普', 
 '<h3>一、跑步前的准备</h3><p>跑步前需要进行充分的热身运动，包括动态拉伸和关节活动。</p><h3>二、正确姿势</h3><p>保持身体直立，目视前方，手臂自然摆动，脚步轻盈落地。</p><h3>三、呼吸方法</h3><p>采用腹式呼吸，两步一吸两步一呼的节奏。</p>',
 '详细介绍跑步的正确姿势、呼吸方法和注意事项，帮助初学者掌握科学跑步技巧。',
 1256, 1, '体育教研组'),

('运动后的营养补充指南', '健康饮食',
 '<h3>一、补充时机</h3><p>运动后30分钟内是营养补充的黄金窗口期。</p><h3>二、补充内容</h3><p>碳水化合物和蛋白质的比例为3:1或4:1。</p><h3>三、推荐食物</h3><p>香蕉、牛奶、鸡胸肉、全麦面包等。</p>',
 '运动后如何科学补充营养，促进肌肉恢复和体能重建。',
 987, 1, '营养师李老师'),

('常见运动损伤的预防与处理', '损伤预防',
 '<h3>一、常见损伤类型</h3><p>肌肉拉伤、韧带扭伤、关节磨损等。</p><h3>二、预防措施</h3><p>充分热身、佩戴护具、循序渐进增加强度。</p><h3>三、应急处理</h3><p>RICE原则：休息、冰敷、加压、抬高。</p>',
 '介绍常见运动损伤的预防方法和应急处理措施，保障运动安全。',
 856, 1, '校医室王医生'),

('如何制定个性化运动计划？', '专家问答',
 '<h3>问：如何根据自己的情况制定运动计划？</h3><p>答：需要考虑年龄、体质、目标等因素...</p><h3>问：每周运动几次最合适？</h3><p>答：建议每周3-5次，每次30-60分钟...</p><h3>问：运动强度如何控制？</h3><p>答：通过心率监测，保持在最大心率的60%-80%...</p>',
 '体育专家解答学生关于运动计划的常见问题，提供专业建议。',
 723, 1, '张教授'),

('游泳的好处与技巧', '运动科普',
 '<h3>一、游泳的好处</h3><p>全身性运动，对关节冲击小，增强心肺功能。</p><h3>二、基本技巧</h3><p>自由泳、蛙泳、仰泳、蝶泳四种泳姿详解。</p><h3>三、注意事项</h3><p>下水前热身、注意安全、避免空腹游泳。</p>',
 '全面介绍游泳运动的益处、技巧和注意事项。',
 645, 1, '游泳教练刘老师');

-- 3. 打卡提醒配置表
CREATE TABLE IF NOT EXISTS `sport_reminder` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `reminder_type` VARCHAR(50) NOT NULL COMMENT '提醒类型（daily/weekly/interrupt）',
    `reminder_time` TIME COMMENT '提醒时间',
    `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用（0:禁用 1:启用）',
    `reminder_days` VARCHAR(50) COMMENT '提醒日期（周一到周日，逗号分隔）',
    `message_template` VARCHAR(500) COMMENT '消息模板',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    UNIQUE KEY `uk_user_type` (`user_id`, `reminder_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运动打卡提醒配置表';

-- 4. 插入默认提醒配置
INSERT INTO `sport_reminder` (`user_id`, `reminder_type`, `reminder_time`, `is_enabled`, `message_template`) VALUES
(1, 'daily', '07:00:00', 1, '早上好！别忘了今天的运动打卡哦~'),
(1, 'interrupt', '20:00:00', 1, '您已连续2天未打卡，坚持就是胜利！');
