/*
 Navicat Premium Data Transfer

 Source Server         : my0001
 Source Server Type    : MySQL
 Source Server Version : 80040
 Source Host           : localhost:3306
 Source Schema         : sport_evaluation

 Target Server Type    : MySQL
 Target Server Version : 80040
 File Encoding         : 65001

 Date: 10/05/2026 23:42:16
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_admin
-- ----------------------------
DROP TABLE IF EXISTS `sys_admin`;
CREATE TABLE `sys_admin`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `user_id` bigint(0) NOT NULL COMMENT '关联系统用户ID',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE,
  CONSTRAINT `fk_admin_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '管理员信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_admin
-- ----------------------------
INSERT INTO `sys_admin` VALUES (1, 1, '2026-02-09 20:30:23', '2026-02-09 20:30:23');

-- ----------------------------
-- Table structure for sys_class
-- ----------------------------
DROP TABLE IF EXISTS `sys_class`;
CREATE TABLE `sys_class`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '班级ID',
  `class_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '班级名称（如：计算机1班）',
  `grade` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '所属年级（如：大一/高二）',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_class_name`(`class_name`) USING BTREE COMMENT '班级名称唯一',
  INDEX `idx_grade`(`grade`) USING BTREE COMMENT '按年级查询索引'
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '班级信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_class
-- ----------------------------
INSERT INTO `sys_class` VALUES (1, '计算机科学与技术1班', '大一', '2026-02-16 13:30:19', '2026-02-16 13:30:19');
INSERT INTO `sys_class` VALUES (2, '软件工程1班', '大二', '2026-02-16 13:30:19', '2026-02-16 13:30:19');
INSERT INTO `sys_class` VALUES (3, '网络工程1班', '大三', '2026-02-16 13:30:19', '2026-02-16 13:30:19');
INSERT INTO `sys_class` VALUES (4, '数据科学与大数据技术1班', '大四', '2026-02-16 13:30:19', '2026-02-16 13:30:19');

-- ----------------------------
-- Table structure for sys_comment
-- ----------------------------
DROP TABLE IF EXISTS `sys_comment`;
CREATE TABLE `sys_comment`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `user_id` bigint(0) NOT NULL COMMENT '评论人用户ID',
  `business_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'sport' COMMENT '关联业务类型(sport:运动评分/notice:公告)',
  `business_id` bigint(0) NOT NULL COMMENT '关联业务ID',
  `parent_id` bigint(0) NULL DEFAULT 0 COMMENT '父评论ID(0=一级评论)',
  `reply_user_id` bigint(0) NULL DEFAULT 0 COMMENT '被回复人用户ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '评论内容',
  `like_count` int(0) NOT NULL DEFAULT 0 COMMENT '点赞数',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态:0-隐藏 1-正常',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_business`(`business_type`, `business_id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_comment_like
-- ----------------------------
DROP TABLE IF EXISTS `sys_comment_like`;
CREATE TABLE `sys_comment_like`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `comment_id` bigint(0) NOT NULL COMMENT '评论ID',
  `user_id` bigint(0) NOT NULL COMMENT '点赞用户ID',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '点赞时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_comment_user`(`comment_id`, `user_id`) USING BTREE,
  INDEX `fk_like_user`(`user_id`) USING BTREE,
  CONSTRAINT `fk_like_comment` FOREIGN KEY (`comment_id`) REFERENCES `sys_comment` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_like_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '评论点赞表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色编码',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色名称',
  `role_desc` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '角色描述',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_code`(`role_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, 'ADMIN', '管理员', '系统唯一管理员', '2026-02-09 20:30:23', '2026-02-09 20:30:23');
INSERT INTO `sys_role` VALUES (2, 'TEACHER', '教师', '体育教师', '2026-02-09 20:30:23', '2026-02-09 20:30:23');
INSERT INTO `sys_role` VALUES (3, 'STUDENT', '学生', '普通学生', '2026-02-09 20:30:23', '2026-02-09 20:30:23');

-- ----------------------------
-- Table structure for sys_sequence
-- ----------------------------
DROP TABLE IF EXISTS `sys_sequence`;
CREATE TABLE `sys_sequence`  (
  `seq_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '序列编码（TEACHER/STUDENT）',
  `seq_date` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '序列日期(yyyyMMdd，空值为基础配置)',
  `current_value` bigint(0) NOT NULL DEFAULT 1 COMMENT '当前流水值',
  `prefix` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '编号前缀',
  `digit` int(0) NOT NULL DEFAULT 3 COMMENT '流水号位数（3位=001）',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_seq_code_date`(`seq_code`, `seq_date`) USING BTREE COMMENT '序列编码+日期联合唯一'
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统自增序列表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_sequence
-- ----------------------------
INSERT INTO `sys_sequence` VALUES ('TEACHER', '', 0, 'T', 3, '2026-02-21 20:44:29', 5);
INSERT INTO `sys_sequence` VALUES ('STUDENT', '', 0, 'S', 3, '2026-02-21 20:44:29', 6);
INSERT INTO `sys_sequence` VALUES ('TEACHER', '20260221', 2, 'T', 3, '2026-02-21 20:49:34', 7);
INSERT INTO `sys_sequence` VALUES ('STUDENT', '20260221', 2, 'S', 3, '2026-02-21 21:20:08', 8);
INSERT INTO `sys_sequence` VALUES ('STUDENT', '20260224', 1, 'S', 3, '2026-02-24 16:11:22', 9);

-- ----------------------------
-- Table structure for sys_student
-- ----------------------------
DROP TABLE IF EXISTS `sys_student`;
CREATE TABLE `sys_student`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '学生ID',
  `user_id` bigint(0) NOT NULL COMMENT '关联系统用户ID',
  `student_no` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '学生学号',
  `class_id` bigint(0) NOT NULL COMMENT '关联班级ID',
  `gender` tinyint(0) NULL DEFAULT 0 COMMENT '性别：0-未知 1-男 2-女',
  `birth_date` date NULL DEFAULT NULL COMMENT '出生日期',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_student_no`(`student_no`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE,
  INDEX `idx_student_no`(`student_no`) USING BTREE,
  INDEX `idx_student_userid`(`user_id`) USING BTREE,
  INDEX `idx_student_class_id`(`class_id`) USING BTREE,
  CONSTRAINT `fk_student_class` FOREIGN KEY (`class_id`) REFERENCES `sys_class` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_student_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学生信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_student
-- ----------------------------
INSERT INTO `sys_student` VALUES (1, 3, 'S20260209001', 1, 1, '2004-09-01', '2026-02-09 20:30:23', '2026-02-21 20:44:29');
INSERT INTO `sys_student` VALUES (4, 13, 'S20260221002', 2, 1, '2008-02-20', '2026-02-21 21:20:08', '2026-02-21 21:20:08');

-- ----------------------------
-- Table structure for sys_teacher
-- ----------------------------
DROP TABLE IF EXISTS `sys_teacher`;
CREATE TABLE `sys_teacher`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '教师ID',
  `user_id` bigint(0) NOT NULL COMMENT '关联系统用户ID',
  `teacher_no` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '教师工号',
  `subject` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '任教科目',
  `gender` tinyint(0) NULL DEFAULT 0 COMMENT '性别：0-未知 1-男 2-女',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_teacher_no`(`teacher_no`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE,
  INDEX `idx_teacher_no`(`teacher_no`) USING BTREE,
  INDEX `idx_teacher_userid`(`user_id`) USING BTREE,
  CONSTRAINT `fk_teacher_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '教师信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_teacher
-- ----------------------------
INSERT INTO `sys_teacher` VALUES (1, 2, 'T20260209001', '体育', 1, '2026-02-09 20:30:23', '2026-02-21 22:23:50');
INSERT INTO `sys_teacher` VALUES (3, 4, 'T20260211001', '物理', 1, '2026-02-11 00:14:25', '2026-02-21 20:44:29');
INSERT INTO `sys_teacher` VALUES (5, 12, 'T20260221002', '地理', 1, '2026-02-21 20:49:34', '2026-02-21 20:49:34');

-- ----------------------------
-- Table structure for sys_teacher_class
-- ----------------------------
DROP TABLE IF EXISTS `sys_teacher_class`;
CREATE TABLE `sys_teacher_class`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `teacher_id` bigint(0) NOT NULL COMMENT '关联教师ID',
  `class_id` bigint(0) NOT NULL COMMENT '关联班级ID',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_teacher_class`(`teacher_id`, `class_id`) USING BTREE COMMENT '教师-班级组合唯一',
  INDEX `idx_teacher`(`teacher_id`) USING BTREE,
  INDEX `idx_class`(`class_id`) USING BTREE,
  CONSTRAINT `fk_tc_class` FOREIGN KEY (`class_id`) REFERENCES `sys_class` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_tc_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `sys_teacher` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '教师-班级关联表（多对多）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_teacher_class
-- ----------------------------
INSERT INTO `sys_teacher_class` VALUES (3, 3, 3, '2026-02-16 13:30:19');
INSERT INTO `sys_teacher_class` VALUES (4, 3, 4, '2026-02-16 13:30:19');
INSERT INTO `sys_teacher_class` VALUES (7, 5, 2, '2026-02-21 20:49:34');
INSERT INTO `sys_teacher_class` VALUES (8, 1, 1, '2026-02-21 22:23:50');
INSERT INTO `sys_teacher_class` VALUES (9, 1, 2, '2026-02-21 22:23:50');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '登录用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '真实姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '邮箱',
  `role_id` bigint(0) NOT NULL COMMENT '关联角色ID',
  `status` tinyint(0) NULL DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '用户头像地址',
  `signature` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '这个人很懒，什么都没写~' COMMENT '个性签名（评论区展示）',
  `login_error_count` int(0) NULL DEFAULT 0 COMMENT '登录错误次数(单日)',
  `reset_pwd_error_count` int(0) NULL DEFAULT 0 COMMENT '重置密码错误次数(单日)',
  `first_error_time` datetime(0) NULL DEFAULT NULL COMMENT '首次错误时间(跨天重置计数)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE,
  INDEX `idx_role_id`(`role_id`) USING BTREE,
  INDEX `idx_user_role_status`(`role_id`, `status`) USING BTREE,
  INDEX `idx_user_realname`(`real_name`) USING BTREE,
  CONSTRAINT `fk_user_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$10$/MhhSLJepKB0nADEuoCime6p193Eu.KFckokt4fyhDhXtoov9Gz5i', '系统管理员', '13800138000', '', 1, 1, '2026-02-09 20:30:23', '2026-05-10 23:40:40', '/upload/avatar/20260223/3afb613a-9c91-4bcd-ad15-2c4172ac2615.jpg', '这个人很懒，什么都没写~', 0, 0, '2026-05-10 23:27:52');
INSERT INTO `sys_user` VALUES (2, 'teacher01', '$2a$10$O7LmNbPyZ6.0uaHxtQgaVOtJ4.ST02FlOBrqGNiczwK72H68Qw.ty', '张老师', '13900139000', '', 2, 1, '2026-02-09 20:30:23', '2026-02-24 15:55:23', '/image/default-avatar.png', '这个人很懒，什么都没写~', 0, 0, '2026-02-24 15:40:17');
INSERT INTO `sys_user` VALUES (3, 'student01', '$2a$10$IKHXL0beKBzEuI8bajjdSePqnGsCMEDf0O5UN9PNjen7UrezlaSAW', '小明', '13700137000', '', 3, 1, '2026-02-09 20:30:23', '2026-02-24 16:07:35', '/image/default-avatar.png', '这个人很懒，什么都没写~', 0, 0, NULL);
INSERT INTO `sys_user` VALUES (4, 'teacher02', '$2a$10$/MhhSLJepKB0nADEuoCime6p193Eu.KFckokt4fyhDhXtoov9Gz5i', '黑马', '', '', 2, 1, '2026-02-11 00:14:25', '2026-02-24 15:44:41', '/image/default-avatar.png', '这个人很懒，什么都没写~', 0, 0, NULL);
INSERT INTO `sys_user` VALUES (12, 'teacher03', '$2a$10$/MhhSLJepKB0nADEuoCime6p193Eu.KFckokt4fyhDhXtoov9Gz5i', '李小', '', '', 2, 1, '2026-02-21 20:49:34', '2026-02-24 15:44:43', '/image/default-avatar.png', '这个人很懒，什么都没写~', 0, 0, NULL);
INSERT INTO `sys_user` VALUES (13, 'student02', '$2a$10$/MhhSLJepKB0nADEuoCime6p193Eu.KFckokt4fyhDhXtoov9Gz5i', '李达', '', '', 3, 1, '2026-02-21 21:20:08', '2026-02-24 16:07:55', '/image/default-avatar.png', '这个人很懒，什么都没写~', 0, 0, NULL);

-- ----------------------------
-- Function structure for get_next_seq_by_date
-- ----------------------------
DROP FUNCTION IF EXISTS `get_next_seq_by_date`;
delimiter ;;
CREATE FUNCTION `get_next_seq_by_date`(seq_code_param VARCHAR(32))
 RETURNS varchar(32) CHARSET utf8mb4 COLLATE utf8mb4_general_ci
  MODIFIES SQL DATA 
  DETERMINISTIC
BEGIN
    -- 声明变量
    DECLARE seq_prefix VARCHAR(10);
    DECLARE seq_digit INT;
    DECLARE current_val BIGINT;
    DECLARE next_val BIGINT;
    DECLARE result_str VARCHAR(32);
    DECLARE current_date_str VARCHAR(8);
    
    -- 获取当前系统日期，格式yyyyMMdd
    SET current_date_str = DATE_FORMAT(NOW(), '%Y%m%d');
    
    -- 1. 行锁查询当日序列，彻底解决并发重复、多行返回问题
    SELECT prefix, digit, current_value INTO seq_prefix, seq_digit, current_val 
    FROM sys_sequence 
    WHERE seq_code = seq_code_param AND seq_date = current_date_str
    LIMIT 1 FOR UPDATE;
    
    -- 2. 当日无序列记录，初始化当日第一条数据
    IF ROW_COUNT() = 0 THEN
        -- 读取该序列的基础配置（前缀、位数，seq_date为空的模板数据）
        SELECT prefix, digit INTO seq_prefix, seq_digit 
        FROM sys_sequence 
        WHERE seq_code = seq_code_param AND seq_date = ''
        LIMIT 1;
        
        -- 序列编码不存在，抛出异常
        IF ROW_COUNT() = 0 THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '序列编码不存在，请先初始化TEACHER/STUDENT基础配置';
        END IF;
        
        -- 初始化当日序列，流水从1开始
        SET next_val = 1;
        -- 插入当日序列记录
        INSERT INTO sys_sequence (seq_code, seq_date, current_value, prefix, digit, update_time)
        VALUES (seq_code_param, current_date_str, next_val, seq_prefix, seq_digit, NOW());
    ELSE
        -- 3. 当日已有序列，流水号自增1
        SET next_val = current_val + 1;
        -- 更新当日序列值
        UPDATE sys_sequence 
        SET current_value = next_val, update_time = NOW()
        WHERE seq_code = seq_code_param AND seq_date = current_date_str;
    END IF;
    
    -- 4. 拼接最终编号：前缀 + 日期 + 指定位数流水号（自动补零）
    SET result_str = CONCAT(seq_prefix, current_date_str, LPAD(next_val, seq_digit, '0'));
    
    RETURN result_str;
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
