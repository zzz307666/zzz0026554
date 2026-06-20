-- 测试数据插入脚本

-- 1. 插入操作日志测试数据
INSERT INTO `sys_operation_log` (`user_id`, `username`, `operation`, `module`, `method`, `params`, `ip`, `result`, `error_msg`, `duration`, `create_time`) VALUES 
(1, 'admin', '登录系统', '用户管理', 'UserController.login', '{}', '127.0.0.1', 'SUCCESS', NULL, 120, NOW()),
(1, 'admin', '查询学生列表', '学生管理', 'AdminController.studentList', '{"page":1,"size":10}', '127.0.0.1', 'SUCCESS', NULL, 85, NOW()),
(2, 'teacher1', '审核运动记录', '运动审核', 'TeacherSportController.audit', '{"recordId":1,"status":1}', '127.0.0.1', 'SUCCESS', NULL, 95, NOW()),
(3, 'student1', '提交运动打卡', '运动打卡', 'StudentSportController.submit', '{"sportTypeId":1,"duration":60}', '127.0.0.1', 'SUCCESS', NULL, 110, NOW());

-- 2. 插入评价维度测试数据（如果不存在）
INSERT INTO `evaluation_dimension` (`dimension_name`, `dimension_code`, `weight`, `description`, `sort_order`, `status`) VALUES 
('耐力', 'ENDURANCE', 0.25, '长跑、游泳等有氧运动能力', 1, 1),
('力量', 'STRENGTH', 0.20, '引体向上、俯卧撑等力量训练', 2, 1),
('速度', 'SPEED', 0.20, '短跑、折返跑等速度测试', 3, 1),
('柔韧', 'FLEXIBILITY', 0.15, '坐位体前屈等柔韧性测试', 4, 1),
('协调', 'COORDINATION', 0.20, '跳绳、篮球等协调性运动', 5, 1)
ON DUPLICATE KEY UPDATE dimension_name=VALUES(dimension_name);

-- 3. 插入学生评价测试数据（需要先有学生ID）
-- 注意：以下SQL需要根据实际的学生ID进行调整
-- INSERT INTO `student_evaluation` (`student_id`, `evaluation_period`, `endurance_score`, `strength_score`, `speed_score`, `flexibility_score`, `coordination_score`, `total_score`, `grade_level`, `teacher_comment`, `teacher_id`, `create_time`) VALUES 
-- (1, '2024-2025-1', 85.00, 78.00, 92.00, 70.00, 88.00, 83.20, '良好', '继续保持，表现不错！', 1, NOW());

-- 4. 检查运动记录是否有待审核的数据
-- SELECT * FROM sport_record WHERE status = 0 LIMIT 10;

-- 提示：如果页面仍然显示空白，请检查以下内容：
-- 1. 数据库中是否有对应的表和数据
-- 2. 用户是否有正确的角色和权限
-- 3. 教师是否关联了班级和学生
-- 4. 学生是否有运动打卡记录
