-- 添加评价等级积分规则
-- 如果已存在则先删除
DELETE FROM `points_rule` WHERE `rule_code` IN ('EVALUATION_EXCELLENT', 'EVALUATION_GOOD', 'EVALUATION_MEDIUM', 'EVALUATION_PASS');

-- 插入新的评价等级积分规则
INSERT INTO `points_rule` (`id`, `rule_name`, `rule_code`, `rule_type`, `points_value`, `condition_json`, `description`, `status`) VALUES 
(5, '评价优秀奖励', 'EVALUATION_EXCELLENT', 'BONUS', 30.00, '{"grade_level": "优秀"}', '五维评价获得优秀等级奖励30积分', 1),
(6, '评价良好奖励', 'EVALUATION_GOOD', 'BONUS', 20.00, '{"grade_level": "良好"}', '五维评价获得良好等级奖励20积分', 1),
(7, '评价中等奖励', 'EVALUATION_MEDIUM', 'BONUS', 10.00, '{"grade_level": "中等"}', '五维评价获得中等等级奖励10积分', 1),
(8, '评价及格奖励', 'EVALUATION_PASS', 'BONUS', 5.00, '{"grade_level": "及格"}', '五维评价获得及格等级奖励5积分', 1);
