-- ============================================
-- 权限管理系统 - RBAC模型
-- ============================================

-- 1. 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
    permission_type VARCHAR(20) NOT NULL DEFAULT 'MENU' COMMENT '权限类型：MENU-菜单，BUTTON-按钮，API-接口',
    parent_id BIGINT DEFAULT 0 COMMENT '父权限ID',
    path VARCHAR(200) COMMENT '路由路径',
    icon VARCHAR(50) COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    description VARCHAR(500) COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统权限表';

-- 2. 角色权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id),
    FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES sys_permission(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- ============================================
-- 插入基础权限数据
-- ============================================

-- 管理员权限
INSERT INTO sys_permission (permission_name, permission_code, permission_type, parent_id, path, icon, sort_order, status, description) VALUES
('系统管理', 'system:manage', 'MENU', 0, '/admin/system', 'setting', 1, 1, '系统管理模块'),
('用户管理', 'system:user', 'MENU', 1, '/admin/users', 'user', 1, 1, '用户管理'),
('角色管理', 'system:role', 'MENU', 1, '/admin/roles', 'team', 2, 1, '角色管理'),
('权限管理', 'system:permission', 'MENU', 1, '/admin/permissions', 'lock', 3, 1, '权限管理'),
('班级管理', 'system:class', 'MENU', 1, '/admin/classes', 'bank', 4, 1, '班级管理'),
('教师管理', 'system:teacher', 'MENU', 1, '/admin/teachers', 'solution', 5, 1, '教师管理');

-- 运动管理权限
INSERT INTO sys_permission (permission_name, permission_code, permission_type, parent_id, path, icon, sort_order, status, description) VALUES
('运动管理', 'sport:manage', 'MENU', 0, '/admin/sport', 'fire', 2, 1, '运动管理模块'),
('运动审核', 'sport:audit', 'MENU', 6, '/admin/sport/audit', 'check', 1, 1, '运动记录审核'),
('运动类型', 'sport:type', 'MENU', 6, '/admin/sport/types', 'tags', 2, 1, '运动类型管理'),
('运动统计', 'sport:stats', 'MENU', 6, '/admin/sport/stats', 'bar-chart', 3, 1, '运动统计分析');

-- 评价管理权限
INSERT INTO sys_permission (permission_name, permission_code, permission_type, parent_id, path, icon, sort_order, status, description) VALUES
('评价管理', 'evaluation:manage', 'MENU', 0, '/admin/evaluation', 'form', 3, 1, '评价管理模块'),
('评价维度', 'evaluation:dimension', 'MENU', 9, '/admin/evaluation/dimensions', 'bars', 1, 1, '评价维度配置'),
('评价记录', 'evaluation:record', 'MENU', 9, '/admin/evaluation/records', 'file-text', 2, 1, '评价记录查询'),
('积分规则', 'evaluation:rule', 'MENU', 9, '/admin/evaluation/rules', 'calculator', 3, 1, '积分规则配置');

-- 数据统计权限
INSERT INTO sys_permission (permission_name, permission_code, permission_type, parent_id, path, icon, sort_order, status, description) VALUES
('数据统计', 'statistics:manage', 'MENU', 0, '/admin/statistics', 'pie-chart', 4, 1, '数据统计模块'),
('班级统计', 'statistics:class', 'MENU', 13, '/admin/class-stats', 'team', 1, 1, '班级统计分析'),
('学生排名', 'statistics:ranking', 'MENU', 13, '/admin/ranking', 'trophy', 2, 1, '学生排名统计'),
('数据报表', 'statistics:report', 'MENU', 13, '/admin/reports', 'file-pdf', 3, 1, '数据报表导出');

-- 系统功能权限
INSERT INTO sys_permission (permission_name, permission_code, permission_type, parent_id, path, icon, sort_order, status, description) VALUES
('系统功能', 'feature:manage', 'MENU', 0, '/admin/features', 'appstore', 5, 1, '系统功能模块'),
('公告管理', 'feature:announcement', 'MENU', 17, '/admin/announcements', 'notification', 1, 1, '系统公告管理'),
('消息中心', 'feature:message', 'MENU', 17, '/admin/messages', 'mail', 2, 1, '消息通知管理'),
('操作日志', 'feature:log', 'MENU', 17, '/admin/logs', 'history', 3, 1, '操作日志查询'),
('数据备份', 'feature:backup', 'MENU', 17, '/admin/backup', 'database', 4, 1, '数据备份恢复'),
('导入导出', 'feature:import-export', 'MENU', 17, '/admin/import-export', 'upload', 5, 1, '数据导入导出'),
('系统监控', 'feature:monitor', 'MENU', 17, '/admin/monitor', 'dashboard', 6, 1, '系统运行监控');

-- ============================================
-- 为管理员角色分配所有权限
-- ============================================
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission; -- 假设角色ID 1是管理员

-- ============================================
-- 为教师角色分配部分权限
-- ============================================
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 2, id FROM sys_permission WHERE permission_code IN (
    'sport:audit', 'sport:type', 'sport:stats',
    'evaluation:dimension', 'evaluation:record',
    'statistics:class', 'statistics:ranking'
); -- 假设角色ID 2是教师

-- ============================================
-- 为学生角色分配基础权限
-- ============================================
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 3, id FROM sys_permission WHERE permission_code IN (
    'sport:stats', 'statistics:ranking'
); -- 假设角色ID 3是学生
