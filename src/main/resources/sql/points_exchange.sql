-- ----------------------------
-- 积分兑换功能相关表
-- ----------------------------

-- 1. 奖品表
DROP TABLE IF EXISTS `points_gift`;
CREATE TABLE `points_gift` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '奖品ID',
  `gift_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '奖品名称',
  `gift_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '奖品图片URL',
  `points_cost` int(0) NOT NULL DEFAULT 0 COMMENT '所需积分',
  `stock` int(0) NOT NULL DEFAULT 0 COMMENT '库存数量',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '奖品描述',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '其他' COMMENT '奖品分类',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态:0-下架 1-上架',
  `sort_order` int(0) NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_sort`(`sort_order`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '积分奖品表' ROW_FORMAT = Dynamic;

-- 初始化奖品数据
INSERT INTO `points_gift` (`id`, `gift_name`, `gift_image`, `points_cost`, `stock`, `description`, `category`, `status`, `sort_order`) VALUES 
(1, '运动水杯', '', 50, 100, '高品质运动水杯，容量500ml', '生活用品', 1, 1),
(2, '运动毛巾', '', 30, 150, '吸汗速干运动毛巾', '运动装备', 1, 2),
(3, '笔记本套装', '', 40, 80, '精美笔记本+笔套装', '学习用品', 1, 3),
(4, '电影票', '', 80, 50, '热门电影兑换券一张', '娱乐票务', 1, 4),
(5, '体育明星签名照', '', 200, 10, '限量版体育明星签名照片', '收藏品', 1, 5),
(6, '运动手环', '', 300, 20, '智能运动手环，监测心率步数', '电子产品', 1, 6),
(7, '健身房月卡', '', 500, 5, '本地健身房月度会员卡', '健身服务', 1, 7),
(8, '定制T恤', '', 100, 60, '个性化定制运动T恤', '服装', 1, 8);

-- 2. 积分兑换记录表
DROP TABLE IF EXISTS `points_exchange`;
CREATE TABLE `points_exchange` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '兑换记录ID',
  `student_id` bigint(0) NOT NULL COMMENT '学生ID',
  `gift_id` bigint(0) NOT NULL COMMENT '奖品ID',
  `gift_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '奖品名称（快照）',
  `points_cost` int(0) NOT NULL COMMENT '消耗积分（快照）',
  `exchange_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '兑换时间',
  `status` tinyint(0) NOT NULL DEFAULT 0 COMMENT '状态:0-待领取 1-已领取 2-已取消',
  `receive_time` datetime(0) NULL DEFAULT NULL COMMENT '领取时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '备注',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_id`(`student_id`) USING BTREE,
  INDEX `idx_gift_id`(`gift_id`) USING BTREE,
  INDEX `idx_exchange_time`(`exchange_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '积分兑换记录表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
