-- ============================================
-- 系统操作日志测试数据
-- 用于测试系统监控功能
-- ============================================

-- 插入一些成功的操作日志
INSERT INTO sys_operation_log (user_id, username, operation, module, method, params, ip, result, duration, create_time) VALUES 
(1, 'admin', '登录系统', '用户管理', 'UserController.login', '{}', '127.0.0.1', 'SUCCESS', 120, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(1, 'admin', '查询学生列表', '学生管理', 'AdminController.studentList', '{"page":1,"size":10}', '127.0.0.1', 'SUCCESS', 85, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(2, 'teacher1', '审核运动记录', '运动审核', 'TeacherSportController.audit', '{"recordId":1,"status":1}', '127.0.0.1', 'SUCCESS', 95, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(3, 'student1', '提交运动打卡', '运动打卡', 'StudentSportController.submit', '{"sportTypeId":1,"duration":60}', '127.0.0.1', 'SUCCESS', 110, DATE_SUB(NOW(), INTERVAL 30 MINUTE));

-- 插入一些失败的操作日志（用于测试错误统计）
INSERT INTO sys_operation_log (user_id, username, operation, module, method, params, ip, result, error_msg, duration, create_time) VALUES 
(1, 'admin', '删除用户失败', '用户管理', 'AdminController.deleteUser', '{"userId":999}', '127.0.0.1', 'FAIL', '用户不存在', 50, DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(2, 'teacher1', '批量导入失败', '数据导入', 'TeacherController.importData', '{"file":"students.xlsx"}', '127.0.0.1', 'FAIL', '文件格式错误', 1200, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 'student1', '提交数据验证失败', '运动打卡', 'StudentSportController.submit', '{"sportTypeId":2,"duration":-10}', '127.0.0.1', 'FAIL', '运动时长不能为负数', 30, DATE_SUB(NOW(), INTERVAL 2 DAY));

-- 插入昨天的错误日志
INSERT INTO sys_operation_log (user_id, username, operation, module, method, params, ip, result, error_msg, duration, create_time) VALUES 
(1, 'admin', '数据库连接超时', '系统监控', 'MonitorController.getData', '{}', '127.0.0.1', 'FAIL', 'Connection timeout', 5000, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 插入本周的其他日志
INSERT INTO sys_operation_log (user_id, username, operation, module, method, params, ip, result, duration, create_time) VALUES 
(1, 'admin', '修改系统配置', '系统设置', 'AdminController.updateConfig', '{"key":"system.name","value":"运动评估系统"}', '127.0.0.1', 'SUCCESS', 75, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 'teacher1', '导出班级数据', '数据导出', 'TeacherController.exportClassData', '{"classId":1}', '127.0.0.1', 'SUCCESS', 200, DATE_SUB(NOW(), INTERVAL 4 DAY));
