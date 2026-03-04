/*
 Navicat Premium Data Transfer

 Source Server         : mysql
 Source Server Type    : MySQL
 Source Server Version : 80043 (8.0.43)
 Source Host           : localhost:3306
 Source Schema         : housekeeping_db

 Target Server Type    : MySQL
 Target Server Version : 80043 (8.0.43)
 File Encoding         : 65001

 Date: 04/11/2025 22:52:00
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for banner
-- ----------------------------
DROP TABLE IF EXISTS `banner`;
CREATE TABLE `banner`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '轮播图标题',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片URL',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片描述',
  `tag` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签（如：新人专享、热门活动、限时特惠等）',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '首页轮播图表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of banner
-- ----------------------------
INSERT INTO `banner` VALUES (1, '春节特惠活动', '/img/1740146308760.jpeg', '春节期间家政服务特惠，全场8折起', '限时特惠', 1, '2025-09-21 21:41:50', '2025-09-21 21:41:50');
INSERT INTO `banner` VALUES (2, '新人专享福利', '/img/1740146318415.jpeg', '新用户注册即送100元优惠券', '新人专享', 1, '2025-09-21 21:41:50', '2025-09-21 21:41:50');
INSERT INTO `banner` VALUES (3, '金牌月嫂推荐', '/img/1740146328774.jpg', '经验丰富的金牌月嫂团队', '热门服务', 1, '2025-09-21 21:41:50', '2025-09-21 21:41:50');
INSERT INTO `banner` VALUES (4, '家政保洁特惠', '/img/1740146340566.jpeg', '专业保洁服务，立减30元', '促销活动', 1, '2025-09-21 21:41:50', '2025-09-21 21:41:50');

-- ----------------------------
-- Table structure for browse_history
-- ----------------------------
DROP TABLE IF EXISTS `browse_history`;
CREATE TABLE `browse_history`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '浏览ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `service_id` bigint NOT NULL COMMENT '服务ID',
  `last_browse_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_browse_time`(`last_browse_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '浏览历史表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of browse_history
-- ----------------------------
INSERT INTO `browse_history` VALUES (12, 5, 1, '2025-11-03 15:57:08');
INSERT INTO `browse_history` VALUES (13, 5, 2, '2025-11-03 15:54:59');
INSERT INTO `browse_history` VALUES (14, 5, 15, '2025-05-09 23:47:00');
INSERT INTO `browse_history` VALUES (15, 5, 6, '2025-10-12 13:15:49');
INSERT INTO `browse_history` VALUES (16, 5, 4, '2025-10-12 13:11:17');
INSERT INTO `browse_history` VALUES (17, 5, 3, '2025-09-09 21:54:12');
INSERT INTO `browse_history` VALUES (18, 5, 13, '2025-10-12 10:37:26');
INSERT INTO `browse_history` VALUES (19, 5, 19, '2025-10-12 13:11:48');
INSERT INTO `browse_history` VALUES (20, 5, 5, '2025-11-04 22:24:18');

-- ----------------------------
-- Table structure for order_refund
-- ----------------------------
DROP TABLE IF EXISTS `order_refund`;
CREATE TABLE `order_refund`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '退款ID',
  `order_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `refund_amount` decimal(10, 2) NOT NULL COMMENT '退款金额',
  `refund_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '退款原因',
  `refund_status` tinyint NULL DEFAULT 1 COMMENT '退款状态(1:待审核,2:审核通过,3:审核拒绝,4:退款中,5:已退款,6:退款失败)',
  `refund_type` tinyint NULL DEFAULT NULL COMMENT '退款类型(1:用户取消,2:服务人员取消,3:超时未接单,4:服务纠纷)',
  `audit_user_id` bigint NULL DEFAULT NULL COMMENT '审核人ID',
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `audit_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核备注',
  `refund_time` datetime NULL DEFAULT NULL COMMENT '退款完成时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '是否删除(0:未删除,1:已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`refund_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单退款表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of order_refund
-- ----------------------------
INSERT INTO `order_refund` VALUES (1, '80c1c14328194887b5789e701ef14477', 5, 1000.00, '不需要了 不需要了 不需要了 不需要了 不需要了 不需要了 不需要了 ', 5, 1, 1, '2025-10-12 13:12:28', '', '2025-10-12 13:12:29', '2025-10-12 11:23:58', '2025-11-04 22:50:12', 0);

-- ----------------------------
-- Table structure for service_category
-- ----------------------------
DROP TABLE IF EXISTS `service_category`;
CREATE TABLE `service_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '类别ID',
  `category_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类别名称',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父类别ID',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `icon` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标URL',
  `sort_num` int NULL DEFAULT 1 COMMENT '排序号',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态(0:禁用,1:正常)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '是否删除(0:未删除,1:已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '服务类别表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of service_category
-- ----------------------------
INSERT INTO `service_category` VALUES (1, '家政服务', NULL, '各类家政服务', 'House', 1, 1, '2025-09-21 14:42:47', '2025-09-21 14:42:47', 0);
INSERT INTO `service_category` VALUES (2, '保洁服务', 1, '日常保洁、深度保洁等', 'Brush', 1, 1, '2025-09-21 14:42:47', '2025-09-21 14:42:47', 0);
INSERT INTO `service_category` VALUES (3, '护理服务', 1, '专业护工服务', 'FirstAidKit', 2, 1, '2025-09-21 14:42:47', '2025-09-21 14:42:47', 0);
INSERT INTO `service_category` VALUES (4, '母婴服务', 1, '月嫂、育儿嫂服务', 'Present', 3, 1, '2025-09-21 14:42:47', '2025-09-21 14:42:47', 0);
INSERT INTO `service_category` VALUES (5, '保姆服务', 1, '专业保姆服务', 'User', 4, 1, '2025-09-21 14:42:47', '2025-09-21 14:42:47', 0);
INSERT INTO `service_category` VALUES (6, '维修服务', 1, '家电维修、管道维修等', 'Tools', 5, 1, '2025-09-21 14:42:47', '2025-09-21 14:42:47', 0);
INSERT INTO `service_category` VALUES (7, '搬家服务', 1, '居民搬家、公司搬迁等', 'Van', 6, 1, '2025-09-21 14:42:47', '2025-09-21 14:42:47', 0);
INSERT INTO `service_category` VALUES (8, '教育培训', NULL, '各类教育培训服务1', 'Reading', 2, 1, '2025-09-21 14:42:47', '2025-09-21 14:42:47', 0);
INSERT INTO `service_category` VALUES (9, '课业辅导', 8, '中小学各科目辅导', 'Edit', 1, 1, '2025-09-21 14:42:47', '2025-09-21 14:42:47', 0);
INSERT INTO `service_category` VALUES (10, '艺术培训', 8, '音乐、美术等艺术培训', 'Brush', 2, 1, '2025-09-21 14:42:47', '2025-09-21 14:42:47', 0);
INSERT INTO `service_category` VALUES (11, '语言培训', 8, '英语、日语等语言培训', 'ChatLineRound', 3, 1, '2025-09-21 14:42:47', '2025-09-21 14:42:47', 0);
INSERT INTO `service_category` VALUES (12, '早教服务', 8, '0-6岁早期教育', 'School', 4, 1, '2025-09-21 14:42:47', '2025-09-21 14:42:47', 0);
INSERT INTO `service_category` VALUES (13, '特长培训', 8, '书法、棋艺等特长培训', 'Medal', 5, 1, '2025-09-21 14:42:47', '2025-09-21 14:42:47', 0);
INSERT INTO `service_category` VALUES (14, '健康养生', NULL, '各类健康养生服务', 'Chicken', 3, 1, '2025-09-21 14:42:47', '2025-09-21 14:42:47', 0);
INSERT INTO `service_category` VALUES (15, '营养咨询', 14, '专业营养师咨询服务', 'Apple', 1, 1, '2025-09-21 14:42:47', '2025-09-21 14:42:47', 0);
INSERT INTO `service_category` VALUES (16, '康复理疗', 14, '专业康复理疗服务', 'FirstAidKit', 2, 1, '2025-09-21 14:42:47', '2025-09-21 14:42:47', 0);
INSERT INTO `service_category` VALUES (17, '中医养生', 14, '中医推拿、艾灸等服务', 'MagicStick', 3, 1, '2025-09-21 14:42:47', '2025-09-21 14:42:47', 0);
INSERT INTO `service_category` VALUES (18, '健康管理', 14, '私人健康管理服务', 'User', 4, 1, '2025-09-21 14:42:47', '2025-09-21 14:42:47', 0);
INSERT INTO `service_category` VALUES (19, '运动指导', 14, '私人运动教练服务', 'Position', 5, 1, '2025-09-21 14:42:47', '2025-09-21 14:42:47', 0);
INSERT INTO `service_category` VALUES (20, '家装服务', NULL, '各类家装装修服务', 'HomeFilled', 4, 1, '2025-09-21 14:42:47', '2025-09-21 14:42:47', 0);
INSERT INTO `service_category` VALUES (21, '室内设计', 20, '室内装修设计服务', 'Crop', 1, 1, '2025-09-21 14:42:47', '2025-09-21 14:42:47', 0);
INSERT INTO `service_category` VALUES (22, '水电安装', 20, '水电线路安装维修', 'Lightning', 2, 1, '2025-09-21 14:42:47', '2025-09-21 14:42:47', 0);
INSERT INTO `service_category` VALUES (23, '木工服务', 20, '木工制作安装服务', 'ScaleToOriginal', 3, 1, '2025-09-21 14:42:47', '2025-09-21 14:42:47', 0);
INSERT INTO `service_category` VALUES (24, '油漆服务', 20, '室内外油漆服务', 'Brush', 4, 1, '2025-09-21 14:42:47', '2025-09-21 14:42:47', 0);
INSERT INTO `service_category` VALUES (25, '瓦工服务', 20, '泥瓦工贴砖服务', 'Stamp', 5, 1, '2025-09-21 14:42:47', '2025-09-21 14:42:47', 0);
INSERT INTO `service_category` VALUES (26, '家具安装', 20, '家具组装维修服务', 'Files', 6, 1, '2025-09-21 14:42:47', '2025-09-21 14:42:47', 0);

-- ----------------------------
-- Table structure for service_item
-- ----------------------------
DROP TABLE IF EXISTS `service_item`;
CREATE TABLE `service_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '服务ID',
  `category_id` bigint NOT NULL COMMENT '类别ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '服务标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '服务描述',
  `price` decimal(10, 2) NOT NULL COMMENT '服务价格',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态(0:下架,1:上架)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '是否删除(0:未删除,1:已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 46 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '服务项目表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of service_item
-- ----------------------------
INSERT INTO `service_item` VALUES (1, 2, '日常保洁', '室内日常保洁打扫，4小时起订1', 200.00, 1, '2025-09-21 14:25:07', '2025-09-21 14:25:07', 0);
INSERT INTO `service_item` VALUES (2, 2, '深度保洁', '室内深度保洁，8小时', 400.00, 1, '2025-09-21 14:25:07', NULL, 0);
INSERT INTO `service_item` VALUES (3, 2, '玻璃清洗', '专业擦玻璃服务，按面积计费', 150.00, 1, '2025-09-21 14:25:07', '2025-09-21 14:25:07', 0);
INSERT INTO `service_item` VALUES (4, 2, '油烟机清洗', '专业清洗油烟机', 300.00, 1, '2025-09-21 14:25:07', '2025-09-21 14:25:07', 0);
INSERT INTO `service_item` VALUES (5, 3, '专业护工', '专业护理服务，24小时', 500.00, 1, '2025-09-21 14:25:07', '2025-09-21 14:25:07', 0);
INSERT INTO `service_item` VALUES (6, 3, '医院陪护', '医院专业陪护服务，12小时', 300.00, 1, '2025-09-21 14:25:07', '2025-09-21 14:25:07', 0);
INSERT INTO `service_item` VALUES (7, 4, '金牌月嫂', '专业月嫂服务，26天起订', 15800.00, 1, '2025-09-21 14:25:07', '2025-09-21 14:25:07', 0);
INSERT INTO `service_item` VALUES (8, 4, '育儿嫂', '专业育儿服务，月付', 8000.00, 1, '2025-09-21 14:25:07', '2025-09-21 14:25:07', 0);
INSERT INTO `service_item` VALUES (9, 5, '住家保姆', '专业住家保姆，月付', 7000.00, 1, '2025-09-21 14:25:07', '2025-09-21 14:25:07', 0);
INSERT INTO `service_item` VALUES (10, 5, '钟点工', '小时工，4小时起订', 180.00, 1, '2025-09-21 14:25:07', '2025-09-21 14:25:07', 0);
INSERT INTO `service_item` VALUES (11, 6, '家电维修', '各类家电维修服务', 200.00, 1, '2025-09-21 14:25:07', '2025-09-21 14:25:07', 0);
INSERT INTO `service_item` VALUES (12, 7, '居民搬家', '居民搬家服务，包含打包装箱', 800.00, 1, '2025-09-21 14:25:07', '2025-09-21 14:25:07', 0);
INSERT INTO `service_item` VALUES (13, 9, '小学数学辅导', '小学数学一对一辅导，1.5小时/课', 200.00, 1, '2025-09-21 14:27:50', '2025-09-21 14:27:50', 0);
INSERT INTO `service_item` VALUES (14, 9, '初中英语辅导', '初中英语一对一辅导，1.5小时/课', 250.00, 1, '2025-09-21 14:27:50', '2025-09-21 14:27:50', 0);
INSERT INTO `service_item` VALUES (15, 10, '钢琴课程', '专业钢琴老师一对一教学，1小时/课', 300.00, 1, '2025-09-21 14:27:50', '2025-09-21 14:27:50', 0);
INSERT INTO `service_item` VALUES (16, 10, '少儿美术', '儿童美术培训，2小时/课', 180.00, 1, '2025-09-21 14:27:50', '2025-09-21 14:27:50', 0);
INSERT INTO `service_item` VALUES (17, 11, '商务英语口语', '商务英语口语培训，1.5小时/课', 280.00, 1, '2025-09-21 14:27:50', '2025-09-21 14:27:50', 0);
INSERT INTO `service_item` VALUES (18, 12, '感统训练', '婴幼儿感统训练课程，1小时/课', 260.00, 1, '2025-09-21 14:27:50', '2025-09-21 14:27:50', 0);
INSERT INTO `service_item` VALUES (19, 15, '营养方案定制', '个性化营养方案定制服务', 500.00, 1, '2025-09-21 14:27:50', '2025-09-21 14:27:50', 0);
INSERT INTO `service_item` VALUES (20, 16, '运动康复', '专业运动康复理疗，1.5小时/次', 400.00, 1, '2025-09-21 14:27:50', '2025-09-21 14:27:50', 0);
INSERT INTO `service_item` VALUES (21, 17, '中医推拿', '中医推拿保健服务，1小时/次', 280.00, 1, '2025-09-21 14:27:50', '2025-09-21 14:27:50', 0);
INSERT INTO `service_item` VALUES (22, 18, '年度健康管理', '私人年度健康管理套餐', 12800.00, 1, '2025-09-21 14:27:50', '2025-09-21 14:27:50', 0);
INSERT INTO `service_item` VALUES (23, 19, '私教课程', '私人健身教练课程，1小时/节', 350.00, 1, '2025-09-21 14:27:50', '2025-09-21 14:27:50', 0);
INSERT INTO `service_item` VALUES (24, 21, '室内设计方案', '室内装修设计方案制作', 3800.00, 1, '2025-09-21 14:27:50', '2025-09-21 14:27:50', 0);
INSERT INTO `service_item` VALUES (25, 22, '水电改造', '室内水电改造服务，按平米计价', 100.00, 1, '2025-09-21 14:27:50', '2025-09-21 14:27:50', 0);
INSERT INTO `service_item` VALUES (26, 23, '定制衣柜', '定制实木衣柜制作安装', 1200.00, 1, '2025-09-21 14:27:50', '2025-09-21 14:27:50', 0);
INSERT INTO `service_item` VALUES (27, 24, '墙面刷新', '室内墙面粉刷服务，按平米计价', 40.00, 1, '2025-09-21 14:27:50', '2025-09-21 14:27:50', 0);
INSERT INTO `service_item` VALUES (28, 25, '瓷砖铺贴', '地面墙面瓷砖铺贴，按平米计价', 80.00, 1, '2025-09-21 14:27:50', '2025-09-21 14:27:50', 0);
INSERT INTO `service_item` VALUES (29, 26, '家具组装', '各类家具组装服务，按件计价', 150.00, 1, '2025-09-21 14:27:50', '2025-09-21 14:27:50', 0);
INSERT INTO `service_item` VALUES (30, 2, '开荒保洁', '新房开荒保洁服务，全屋深度清洁', 600.00, 1, '2025-09-21 14:27:50', '2025-09-21 14:27:50', 0);
INSERT INTO `service_item` VALUES (31, 2, '空调清洗', '家用空调深度清洗消毒', 180.00, 1, '2025-09-21 14:27:50', '2025-09-21 14:27:50', 0);
INSERT INTO `service_item` VALUES (32, 3, '老人陪护', '老人日常陪护服务，8小时/天', 280.00, 1, '2025-09-21 14:27:50', '2025-09-21 14:27:50', 0);
INSERT INTO `service_item` VALUES (33, 4, '催乳师', '专业催乳按摩服务', 380.00, 1, '2025-09-21 14:27:50', '2025-09-21 14:27:50', 0);
INSERT INTO `service_item` VALUES (34, 4, '产后修复', '产后身体恢复护理服务', 450.00, 1, '2025-09-21 14:27:50', '2025-09-21 14:27:50', 0);
INSERT INTO `service_item` VALUES (35, 5, '做饭阿姨', '专业做饭服务，3小时/次', 120.00, 1, '2025-09-21 14:27:50', '2025-09-21 14:27:50', 0);
INSERT INTO `service_item` VALUES (36, 6, '管道疏通', '厨卫管道疏通服务', 150.00, 1, '2025-09-21 14:27:50', '2025-09-21 14:27:50', 0);
INSERT INTO `service_item` VALUES (37, 6, '开锁换锁', '专业开锁换锁服务', 120.00, 1, '2025-09-21 14:27:50', '2025-09-21 14:27:50', 0);
INSERT INTO `service_item` VALUES (38, 7, '公司搬迁', '企业办公室搬迁服务', 2000.00, 1, '2025-09-21 14:27:50', '2025-09-21 14:27:50', 0);
INSERT INTO `service_item` VALUES (39, 9, '高中物理辅导', '高中物理一对一辅导，2小时/课', 300.00, 1, '2025-09-21 14:27:50', '2025-09-21 14:27:50', 0);
INSERT INTO `service_item` VALUES (40, 10, '古筝课程', '专业古筝教学，1小时/课', 280.00, 1, '2025-09-21 14:27:50', '2025-09-21 14:27:50', 0);
INSERT INTO `service_item` VALUES (41, 11, '日语培训', '日语一对一培训，1.5小时/课', 260.00, 1, '2025-09-21 14:27:50', '2025-09-21 14:27:50', 0);
INSERT INTO `service_item` VALUES (42, 13, '书法培训', '少儿书法培训，1.5小时/课', 200.00, 1, '2025-09-21 14:27:50', '2025-09-21 14:27:50', 0);
INSERT INTO `service_item` VALUES (43, 15, '月子餐定制', '专业月子餐制作配送', 200.00, 1, '2025-09-21 14:27:50', '2025-09-21 14:27:50', 0);
INSERT INTO `service_item` VALUES (44, 17, '艾灸理疗', '中医艾灸养生理疗', 220.00, 1, '2025-09-21 14:27:50', '2025-09-21 14:27:50', 0);
INSERT INTO `service_item` VALUES (45, 19, '瑜伽私教', '专业瑜伽私教课程，1小时/节', 300.00, 1, '2025-09-21 14:27:50', '2025-09-21 14:27:50', 0);

-- ----------------------------
-- Table structure for service_order
-- ----------------------------
DROP TABLE IF EXISTS `service_order`;
CREATE TABLE `service_order`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `staff_id` bigint NOT NULL COMMENT '服务人员ID',
  `service_id` bigint NOT NULL COMMENT '服务项目ID',
  `order_status` tinyint NOT NULL DEFAULT 1 COMMENT '订单状态(1:待支付,2:待接单,3:已接单,4:服务中,5:已完成,6:已取消,7:已关闭)',
  `total_amount` decimal(10, 2) NOT NULL COMMENT '订单金额',
  `payment_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支付方式(WECHAT:微信,ALIPAY:支付宝,BALANCE:余额)',
  `payment_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `paid_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '实付金额',
  `refund_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '退款金额',
  `refund_status` tinyint NULL DEFAULT 0 COMMENT '退款状态(0:无退款,1:退款中,2:已退款,3:退款失败)',
  `cancel_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '取消原因',
  `cancel_time` datetime NULL DEFAULT NULL COMMENT '取消时间',
  `complete_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '是否删除(0:未删除,1:已删除)',
  `service_time` datetime NULL DEFAULT NULL COMMENT '服务开始时间',
  `duration` float NULL DEFAULT NULL COMMENT '服务时长(小时)',
  `old_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_staff_id`(`staff_id` ASC) USING BTREE,
  INDEX `idx_service_id`(`service_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 126 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '服务订单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of service_order
-- ----------------------------
INSERT INTO `service_order` VALUES ('035780e25d53475b92747afb28cc8229', 8, 2, 10, 5, 229.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-10 15:30:00', NULL, '2025-09-10 11:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 49);
INSERT INTO `service_order` VALUES ('04c066c6068c478b81c4adf54a2dde00', 9, 1, 8, 5, 199.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-14 14:30:00', NULL, '2025-09-14 10:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 40);
INSERT INTO `service_order` VALUES ('063e02f566a340f3b57acc6977ec7e39', 5, 2, 26, 5, 459.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-08-04 11:45:00', NULL, '2025-08-04 08:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 81);
INSERT INTO `service_order` VALUES ('0affd073eeeb4b0aa34478263124a534', 7, 3, 14, 5, 289.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-10-04 16:45:00', NULL, '2025-10-04 12:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 98);
INSERT INTO `service_order` VALUES ('0b150f0f0d0344909671da4983fae0c8', 8, 1, 7, 5, 179.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-22 11:45:00', NULL, '2025-09-22 08:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 24);
INSERT INTO `service_order` VALUES ('0b89407d222e44b1bbff86f90c34c1e3', 8, 1, 24, 5, 429.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-08-31 18:45:00', NULL, '2025-08-31 14:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 84);
INSERT INTO `service_order` VALUES ('0c80f823bdab4055b389277ec412c44c', 3, 7, 9, 5, 700.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-28 14:25:51', NULL, '2025-09-27 14:25:51', '2025-11-04 22:50:12', 0, NULL, NULL, 5);
INSERT INTO `service_order` VALUES ('0d0755a7822a49a792a1cf841e4f30de', 8, 4, 17, 5, 339.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-10-06 15:20:00', NULL, '2025-10-06 11:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 99);
INSERT INTO `service_order` VALUES ('0fab320c1ad9449ab126f4c74a7159a5', 6, 2, 23, 5, 419.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-10-03 14:30:00', NULL, '2025-10-03 10:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 97);
INSERT INTO `service_order` VALUES ('1137c5a371da4d0cad5d1a99741fe7bc', 7, 3, 22, 3, 399.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, NULL, NULL, '2025-09-07 11:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 18);
INSERT INTO `service_order` VALUES ('126451535b594caa90f3233dd39d1559', 9, 3, 22, 5, 399.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-08-26 17:30:00', NULL, '2025-08-26 13:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 110);
INSERT INTO `service_order` VALUES ('130f58ed86ab4863802047986b5cca1f', 8, 3, 20, 5, 379.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-25 18:45:00', NULL, '2025-09-25 14:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 94);
INSERT INTO `service_order` VALUES ('141161ee282849349cdc61d5e2b7ad9a', 5, 1, 1, 5, 400.00, 'BALANCE', '2025-10-12 13:11:33', 400.00, 0.00, 0, NULL, NULL, '2025-10-12 13:13:08', '{\"联系电话\":\"15252393509\",\"服务地址\":\"江苏省淮安市\",\"备注说明\":\"\"}', '2025-10-12 13:11:28', '2025-11-04 22:50:12', 0, '2025-10-28 00:00:00', 2, 123);
INSERT INTO `service_order` VALUES ('1451dbda51534b8394ad3715e35d5697', 7, 4, 19, 5, 369.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-08-13 16:30:00', NULL, '2025-08-13 12:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 103);
INSERT INTO `service_order` VALUES ('1980bba4326442f08414055b0955f316', 9, 4, 10, 5, 229.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-10-01 12:00:00', NULL, '2025-10-01 08:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 95);
INSERT INTO `service_order` VALUES ('19c91ab306c04750827d7bd280ce846f', 6, 2, 21, 5, 389.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-08-09 14:30:00', NULL, '2025-08-09 10:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 77);
INSERT INTO `service_order` VALUES ('1b80facfbc4049978a0c52d88ee43d43', 7, 2, 12, 5, 259.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-18 17:30:00', NULL, '2025-09-18 13:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 33);
INSERT INTO `service_order` VALUES ('1bb8cb3e5d65443f972c099ae1510f9c', 6, 3, 11, 5, 249.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-13 15:20:00', NULL, '2025-09-13 11:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 42);
INSERT INTO `service_order` VALUES ('1c250bc1e16a4686bda0ff2ad51bfae0', 5, 2, 1, 6, 400.00, NULL, NULL, NULL, 0.00, 0, '不需要了', '2025-09-10 17:36:41', NULL, '{\"联系电话\":\"15252393509\",\"服务地址\":\"江苏省淮安市\",\"备注说明\":\"OK\"}', '2025-09-10 17:36:08', '2025-11-04 22:50:12', 0, '2025-09-11 00:00:00', 2, 118);
INSERT INTO `service_order` VALUES ('241b5f1802444e929d049a2bb4156661', 9, 4, 26, 5, 459.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-17 14:15:00', NULL, '2025-09-17 10:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 35);
INSERT INTO `service_order` VALUES ('26a58a5ec9564b87855230fb30a889b7', 7, 4, 8, 5, 199.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-03 17:30:00', NULL, '2025-09-03 13:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 63);
INSERT INTO `service_order` VALUES ('29d929ec8a0d42eead004cf9580870d5', 5, 3, 13, 5, 279.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-11 16:30:00', NULL, '2025-09-11 12:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 46);
INSERT INTO `service_order` VALUES ('2a2861bf5da8446b8097e212384348bd', 6, 4, 22, 5, 399.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-08-27 14:30:00', NULL, '2025-08-27 10:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 87);
INSERT INTO `service_order` VALUES ('2f607dc769914d4fbfb4c9849dfaa046', 8, 1, 18, 5, 349.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-12 11:45:00', NULL, '2025-09-12 08:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 44);
INSERT INTO `service_order` VALUES ('2fbb953a4fc84a98be38bb9ae266ad49', 6, 2, 8, 5, 199.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-09 23:56:54', NULL, '2025-09-08 10:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 17);
INSERT INTO `service_order` VALUES ('304679959fe94f38afeb069c5a22352e', 5, 2, 1, 1, 400.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, NULL, '{\"服务时间\":\"2025-09-13 00:00\",\"服务时长\":2,\"联系电话\":\"15252393509\",\"服务地址\":\"江苏省淮安市\",\"备注说明\":\"\",\"订单金额\":\"400.00\"}', NULL, '2025-11-04 22:50:12', 0, NULL, NULL, 13);
INSERT INTO `service_order` VALUES ('322b495ec8934b0d8592b12ab3e55267', 5, 2, 1, 5, 200.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-10 17:50:24', '{\"联系电话\":\"15252393509\",\"服务地址\":\"江苏省淮安市\",\"备注说明\":\"OK\"}', '2025-09-10 17:49:11', '2025-11-04 22:50:12', 0, '2025-09-10 15:00:00', 1, 120);
INSERT INTO `service_order` VALUES ('33637dc6b0f348a9a6294509ee257a86', 9, 2, 11, 5, 249.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-02 14:15:00', NULL, '2025-09-02 10:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 65);
INSERT INTO `service_order` VALUES ('3c3438b20fe24902837c519276153a44', 5, 2, 24, 5, 429.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-04 16:45:00', NULL, '2025-09-04 12:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 61);
INSERT INTO `service_order` VALUES ('42814c7b1da445b1915275c09cb04ebc', 5, 1, 12, 5, 259.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-06 16:30:00', NULL, '2025-09-06 12:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 56);
INSERT INTO `service_order` VALUES ('444280896cd046df8710aeed20d3162c', 5, 4, 20, 5, 379.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-08-18 11:45:00', NULL, '2025-08-18 08:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 71);
INSERT INTO `service_order` VALUES ('46bc7971c04f40a4921608f35f9ffa0c', 2, 1, 4, 5, 300.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-22 00:13:02', NULL, '2025-09-21 14:25:51', '2025-11-04 22:50:13', 0, NULL, NULL, 8);
INSERT INTO `service_order` VALUES ('4b1ea39125e343829cdef3ef37a06b9b', 9, 3, 16, 5, 329.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-09 14:30:00', NULL, '2025-09-09 10:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 50);
INSERT INTO `service_order` VALUES ('4ba0d2e918194665a25e5d41c16de6c1', 5, 3, 10, 5, 229.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-21 16:30:00', NULL, '2025-09-21 12:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 26);
INSERT INTO `service_order` VALUES ('4d1bd4280f1440cf95e9ab39bbfd149d', 5, 2, 3, 1, 300.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, NULL, '{\"服务时间\":\"2025-04-30 00:00\",\"服务时长\":2,\"联系电话\":\"15252393509\",\"服务地址\":\"江苏省淮安市\",\"备注说明\":\"\",\"订单金额\":\"300.00\"}', NULL, '2025-11-04 22:50:12', 0, NULL, NULL, 113);
INSERT INTO `service_order` VALUES ('4de39f68870e4a41bb65ceeea085dc23', 5, 4, 25, 5, 449.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-28 11:45:00', NULL, '2025-09-28 08:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 91);
INSERT INTO `service_order` VALUES ('4fb4916c73374f259770b36a2e1fc60c', 7, 3, 28, 5, 479.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-08-08 16:45:00', NULL, '2025-08-08 12:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 78);
INSERT INTO `service_order` VALUES ('53598e4487994ce683e3ec7c75d5110b', 9, 1, 21, 5, 389.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-10-07 17:30:00', NULL, '2025-10-07 13:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 100);
INSERT INTO `service_order` VALUES ('5fad45a52d624ca4864b41486fb05e7d', 8, 2, 14, 5, 289.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-20 15:30:00', NULL, '2025-09-20 11:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 29);
INSERT INTO `service_order` VALUES ('613bf2d0045e43a49ba831aa4e545ad1', 9, 2, 25, 5, 449.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-12 14:15:00', NULL, '2025-09-12 10:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 45);
INSERT INTO `service_order` VALUES ('62298b2b11b34567856fc5f494497982', 2, 10, 2, 5, 400.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-10-03 14:25:51', NULL, '2025-10-02 14:25:51', '2025-11-04 22:50:12', 0, NULL, NULL, 2);
INSERT INTO `service_order` VALUES ('64f65bc663b941cfa4ea57d443205208', 8, 3, 21, 5, 389.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-07 11:45:00', NULL, '2025-09-07 08:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 54);
INSERT INTO `service_order` VALUES ('6751c3bb3f664bcfa4bce0ed382f3ab3', 8, 2, 8, 5, 199.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-08-28 15:20:00', NULL, '2025-08-28 11:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 109);
INSERT INTO `service_order` VALUES ('67b84fc42b524a9bbe9588f550ed2b7a', 5, 2, 1, 1, 400.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, NULL, '{\"服务时间\":\"2025-07-10 00:00\",\"服务时长\":2,\"联系电话\":\"15252393509\",\"服务地址\":\"江苏省淮安市\",\"备注说明\":\"\",\"订单金额\":\"400.00\"}', '2025-09-10 15:43:07', '2025-11-04 22:50:12', 0, NULL, NULL, 116);
INSERT INTO `service_order` VALUES ('686f77e587b14cc3b7769ed529646a93', 9, 3, 18, 5, 349.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-29 17:30:00', NULL, '2025-09-29 13:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 90);
INSERT INTO `service_order` VALUES ('6881056ca58c4f6e86018da3674fee35', 9, 2, 20, 5, 379.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-22 14:15:00', NULL, '2025-09-22 10:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 25);
INSERT INTO `service_order` VALUES ('692f089d12284507b58bb52fb90ac47f', 9, 1, 5, 5, 159.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-24 14:30:00', NULL, '2025-09-24 10:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 20);
INSERT INTO `service_order` VALUES ('782ca59146ad456d8d070fcc7d03c41d', 5, 5, 6, 6, 600.00, NULL, NULL, NULL, 0.00, 0, '1', '2025-11-04 22:24:13', NULL, '{\"联系电话\":\"15252393509\",\"服务地址\":\"江苏省淮安市\",\"备注说明\":\"\"}', '2025-10-12 13:16:01', '2025-11-04 22:50:12', 0, '2025-10-29 00:00:00', 2, 124);
INSERT INTO `service_order` VALUES ('78e647bda5fb455e86fb235fa27a3f2e', 1, 7, 10, 5, 180.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-24 14:25:51', NULL, '2025-09-23 14:25:51', '2025-11-04 22:50:13', 0, NULL, NULL, 7);
INSERT INTO `service_order` VALUES ('7953bb75b2434eb1afacb09b392c3f6e', 9, 1, 19, 5, 369.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-08-05 17:30:00', NULL, '2025-08-05 13:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 80);
INSERT INTO `service_order` VALUES ('80c1c14328194887b5789e701ef14477', 5, 3, 5, 6, 1000.00, 'WECHAT', '2025-10-12 11:23:24', 1000.00, 1000.00, 2, '退款成功', '2025-10-12 13:12:29', NULL, '{\"联系电话\":\"15252393509\",\"服务地址\":\"江苏省淮安市\",\"备注说明\":\"\"}', '2025-10-12 11:23:18', '2025-11-04 22:50:12', 0, '2025-10-22 00:00:00', 2, 122);
INSERT INTO `service_order` VALUES ('81dec35d19c04a4a9cca8fb99a2cc760', 2, 1, 1, 6, 200.00, NULL, NULL, NULL, 0.00, 0, '与用户协商一致', '2025-09-30 21:35:40', NULL, NULL, '2025-09-30 14:25:51', '2025-11-04 22:50:12', 0, NULL, NULL, 4);
INSERT INTO `service_order` VALUES ('8258995b17ff46beac82c25475f2c7bd', 9, 4, 28, 5, 479.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-07 14:15:00', NULL, '2025-09-07 10:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 55);
INSERT INTO `service_order` VALUES ('8342d9431f7945d1ac5511c4412c28de', 8, 3, 19, 5, 369.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-17 11:45:00', NULL, '2025-09-17 08:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 34);
INSERT INTO `service_order` VALUES ('856e42abb07c4e39ad08fd0e963ecf70', 6, 3, 25, 5, 449.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-23 15:20:00', NULL, '2025-09-23 11:00:00', '2025-11-04 22:50:12', 1, NULL, NULL, 22);
INSERT INTO `service_order` VALUES ('86874f3e591548edb772c32966df45c6', 7, 3, 24, 5, 429.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-15 12:00:00', NULL, '2025-09-15 08:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 38);
INSERT INTO `service_order` VALUES ('868f5e6e760e52f2c5a36543688cfc5e', 5, 3, 5, 2, 1000.00, 'ALIPAY', '2025-11-04 22:51:35', 1000.00, 0.00, 0, NULL, NULL, NULL, '{\"联系电话\":\"13123456789\",\"服务地址\":\"中国\",\"备注说明\":\"\"}', '2025-11-04 22:51:05', '2025-11-04 22:51:35', 0, '2025-11-11 00:00:00', 2, NULL);
INSERT INTO `service_order` VALUES ('869e65f2d1e6449bb1ee986d6df62f44', 5, 3, 5, 5, 159.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-01 16:30:00', NULL, '2025-09-01 12:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 66);
INSERT INTO `service_order` VALUES ('8979f248a8df471b998a95a437442c6c', 5, 2, 1, 6, 400.00, NULL, NULL, NULL, 0.00, 0, '不需要了', '2025-09-20 00:03:39', NULL, '服务时间：2025-09-25 00:00，服务时长：2小时\n联系电话：15252393509\n服务地址：江苏省淮安市\n备注：无', '2025-09-19 11:41:09', '2025-11-04 22:50:13', 0, NULL, NULL, 9);
INSERT INTO `service_order` VALUES ('8bb4b7f0725441c9bb778ff5348c605b', 5, 1, 17, 5, 339.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-08-11 15:30:00', NULL, '2025-08-11 11:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 76);
INSERT INTO `service_order` VALUES ('8c35f2a648954959a0c2a1606255ce1b', 7, 1, 25, 5, 449.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-08-22 16:45:00', NULL, '2025-08-22 12:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 68);
INSERT INTO `service_order` VALUES ('8ccc41a3fcdc41a38d04a7f85f77c5de', 5, 2, 22, 5, 399.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-14 16:45:00', NULL, '2025-09-14 12:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 41);
INSERT INTO `service_order` VALUES ('8f23206f40834c45a2f5d24bc928439e', 6, 4, 16, 5, 329.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-21 18:45:00', NULL, '2025-09-21 14:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 27);
INSERT INTO `service_order` VALUES ('93d936c9fa0b4342a7a44a5ac840a4bc', 6, 4, 24, 5, 429.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-08-02 14:30:00', NULL, '2025-08-02 10:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 107);
INSERT INTO `service_order` VALUES ('95f6ab4553dc4cb5b8fe3ac635270770', 6, 3, 9, 5, 219.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-08-03 14:15:00', NULL, '2025-08-03 10:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 82);
INSERT INTO `service_order` VALUES ('97a2c807b20544eb9307c79f68d8b1cb', 7, 2, 7, 5, 179.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-26 16:30:00', NULL, '2025-09-26 12:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 93);
INSERT INTO `service_order` VALUES ('97ae3893e4484ed4ab4d36b965df785a', 5, 1, 15, 2, 299.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, NULL, NULL, '2025-09-10 09:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 16);
INSERT INTO `service_order` VALUES ('993f0c77ff934f759907058693447f49', 8, 4, 15, 5, 299.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-15 15:30:00', NULL, '2025-09-15 11:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 39);
INSERT INTO `service_order` VALUES ('9add3a06a4ac4b2c8ca15c3a3bb64214', 6, 1, 28, 5, 479.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-18 15:20:00', NULL, '2025-09-18 11:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 32);
INSERT INTO `service_order` VALUES ('a0881947c4374a1c84308d48543640bb', 6, 1, 13, 5, 279.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-27 14:15:00', NULL, '2025-09-27 10:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 92);
INSERT INTO `service_order` VALUES ('a08b2693a89d4ba8abe960926a0e5c1f', 8, 4, 11, 4, 249.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, NULL, NULL, '2025-09-06 12:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 19);
INSERT INTO `service_order` VALUES ('a4e9ad0f65d24fdeb1c8388609c32d07', 9, 2, 15, 5, 299.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-08-30 12:00:00', NULL, '2025-08-30 08:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 85);
INSERT INTO `service_order` VALUES ('a8e25d2aa5164e7693d5c6c4e9182c8d', 9, 3, 17, 5, 339.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-19 14:30:00', NULL, '2025-09-19 10:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 30);
INSERT INTO `service_order` VALUES ('aa7f9f660f394cbe91df68a2c18d6078', 7, 1, 15, 5, 299.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-08-31 16:45:00', NULL, '2025-08-31 12:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 108);
INSERT INTO `service_order` VALUES ('abfcd1cb80484e268863c8e1aa0c1c53', 9, 2, 9, 5, 219.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-08-07 12:00:00', NULL, '2025-08-07 08:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 105);
INSERT INTO `service_order` VALUES ('ac5f9a0c3d67412faa58654e6ee30473', 5, 3, 17, 5, 339.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-08-05 15:30:00', NULL, '2025-08-05 11:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 106);
INSERT INTO `service_order` VALUES ('acb61ccb20a343fb83a097f5c03e8c4a', 9, 1, 17, 5, 339.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-04 14:30:00', NULL, '2025-09-04 10:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 60);
INSERT INTO `service_order` VALUES ('afb40dabc47f4f30a3bfed20f7203b8d', 1, 1, 1, 5, 200.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-10-05 14:25:51', NULL, '2025-10-04 14:25:51', '2025-11-04 22:50:12', 0, NULL, NULL, 1);
INSERT INTO `service_order` VALUES ('afd0436e284b4c0194798689fa6dcfaa', 5, 2, 28, 5, 479.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-08-20 11:45:00', NULL, '2025-08-20 08:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 101);
INSERT INTO `service_order` VALUES ('b09789c7a077491ebd93f82e6bb205dc', 6, 1, 10, 5, 229.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-08-17 14:15:00', NULL, '2025-08-17 10:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 72);
INSERT INTO `service_order` VALUES ('b6a60222dc8c41e8835b061dd40cf0d7', 4, 8, 3, 5, 150.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-26 14:25:51', NULL, '2025-09-25 14:25:51', '2025-11-04 22:50:13', 0, NULL, NULL, 6);
INSERT INTO `service_order` VALUES ('b6db50f9a7674728a1ec0c2ed3f7825a', 5, 1, 9, 5, 219.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-16 16:30:00', NULL, '2025-09-16 12:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 36);
INSERT INTO `service_order` VALUES ('b79833e7cc70426a876a86fef6d03728', 6, 4, 7, 5, 179.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-11 18:45:00', NULL, '2025-09-11 14:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 47);
INSERT INTO `service_order` VALUES ('b8b3428a0758440f9254ecdf2eb962fc', 5, 2, 1, 1, 400.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, NULL, '{\"联系电话\":\"15252393509\",\"服务地址\":\"江苏省淮安市\",\"备注说明\":\"\"}', '2025-09-10 17:35:38', '2025-11-04 22:50:12', 0, '2025-09-10 16:00:00', 2, 117);
INSERT INTO `service_order` VALUES ('b9013e6df4f64c74a35900509ce6bc53', 7, 4, 17, 5, 339.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-08-01 16:30:00', NULL, '2025-08-01 12:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 83);
INSERT INTO `service_order` VALUES ('baa0f11594fd4f848acd13e1f910fc97', 5, 2, 5, 6, 100.00, NULL, NULL, NULL, 0.00, 0, '不想要了', '2025-09-16 11:54:44', NULL, '{\"服务时间\":\"2025-09-20 00:00\",\"服务时长\":2,\"联系电话\":\"15252393509\",\"服务地址\":\"江苏省淮安市\",\"备注说明\":\"\",\"订单金额\":\"1000.00\"}', '2025-09-15 11:41:16', '2025-11-04 22:50:12', 0, NULL, NULL, 11);
INSERT INTO `service_order` VALUES ('bd4509df42f54703b1ad2d1a8e684fb8', 5, 2, 5, 1, 1000.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, NULL, '{\"服务时间\":\"2025-05-21 00:00\",\"服务时长\":2,\"联系电话\":\"15252393509\",\"服务地址\":\"江苏省淮安市\",\"备注说明\":\"\",\"订单金额\":\"1000.00\"}', '2025-05-09 23:38:57', '2025-11-04 22:50:12', 0, NULL, NULL, 114);
INSERT INTO `service_order` VALUES ('bf7da306d2694757b32c28825d1c5364', 9, 3, 7, 5, 179.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-08-19 17:30:00', NULL, '2025-08-19 13:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 70);
INSERT INTO `service_order` VALUES ('c0047716f85747a1ac5a8f1125a74aa2', 6, 1, 5, 5, 159.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-08-24 14:15:00', NULL, '2025-08-24 10:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 112);
INSERT INTO `service_order` VALUES ('c03d238878784ccc979146f37ca64ea7', 7, 4, 13, 5, 279.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-23 17:30:00', NULL, '2025-09-23 13:00:00', '2025-11-04 22:50:12', 1, NULL, NULL, 23);
INSERT INTO `service_order` VALUES ('c0fca2f8bee14fe89df74f3246f01f9a', 8, 3, 23, 5, 419.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-08-14 18:45:00', NULL, '2025-08-14 14:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 74);
INSERT INTO `service_order` VALUES ('c20f0cc121ac4173b6760e28984b294c', 6, 2, 19, 5, 369.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-06 18:45:00', NULL, '2025-09-06 14:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 57);
INSERT INTO `service_order` VALUES ('c813b3adea9047f581695f401a19fcd1', 5, 4, 11, 5, 249.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-08-25 11:45:00', NULL, '2025-08-25 08:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 111);
INSERT INTO `service_order` VALUES ('c8d6e45066864997b208401d42e26ba4', 6, 2, 17, 5, 339.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-16 18:45:00', NULL, '2025-09-16 14:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 37);
INSERT INTO `service_order` VALUES ('c92ef047df4745129a627d7f3f25e51f', 8, 2, 5, 5, 159.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-30 15:20:00', NULL, '2025-09-30 11:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 89);
INSERT INTO `service_order` VALUES ('cd109af3a19f4e519355e1a9bafe1752', 6, 4, 18, 5, 349.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-08-23 14:30:00', NULL, '2025-08-23 10:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 67);
INSERT INTO `service_order` VALUES ('ce2a3fe6f44b4cd48ea3ed4c1d31fd81', 5, 2, 18, 5, 349.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-24 16:45:00', NULL, '2025-09-24 12:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 21);
INSERT INTO `service_order` VALUES ('cf671b2ada1840b08470fec4316696bd', 7, 1, 23, 5, 419.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-20 12:00:00', NULL, '2025-09-20 08:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 28);
INSERT INTO `service_order` VALUES ('d3295b1d9fe9411ba6215a57983b61de', 5, 2, 1, 2, 400.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, NULL, '{\"服务时间\":\"2025-09-14 00:00\",\"服务时长\":2,\"联系电话\":\"15252393509\",\"服务地址\":\"江苏省淮安市\",\"备注说明\":\"\",\"订单金额\":\"400.00\"}', NULL, '2025-11-04 22:50:12', 0, NULL, NULL, 12);
INSERT INTO `service_order` VALUES ('d3cc9b048f454611a61fd0a7d2b8a9ae', 8, 1, 22, 5, 399.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-02 11:45:00', NULL, '2025-09-02 08:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 64);
INSERT INTO `service_order` VALUES ('d448a6ccd0404980abcb0a32759be3d6', 5, 1, 16, 5, 329.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-10-02 15:30:00', NULL, '2025-10-02 11:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 96);
INSERT INTO `service_order` VALUES ('d8bf242d74824ebeb012974092e455c7', 7, 3, 26, 5, 459.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-05 12:00:00', NULL, '2025-09-05 08:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 58);
INSERT INTO `service_order` VALUES ('d97182856df344fe9654e98a6faac1a1', 6, 3, 12, 5, 259.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-08-16 14:15:00', NULL, '2025-08-16 10:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 102);
INSERT INTO `service_order` VALUES ('da077610571149e5aea83335ba46ee23', 5, 3, 8, 5, 199.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-08-29 15:30:00', NULL, '2025-08-29 11:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 86);
INSERT INTO `service_order` VALUES ('df12c7ca7e1e47b59d7c09a1aa3dba81', 7, 1, 11, 5, 249.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-08-26 16:45:00', NULL, '2025-08-26 12:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 88);
INSERT INTO `service_order` VALUES ('dfc1ab0a60324987819cd942b2345535', 7, 2, 16, 5, 329.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-08-15 16:30:00', NULL, '2025-08-15 12:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 73);
INSERT INTO `service_order` VALUES ('e009641558dd4d18a3f2d330de2451a7', 5, 4, 21, 5, 389.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-19 16:45:00', NULL, '2025-09-19 12:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 31);
INSERT INTO `service_order` VALUES ('e0314515b0504a42b870675ec6f7b058', 5, 3, 5, 2, 1000.00, 'ALIPAY', '2025-11-04 22:32:16', 1000.00, 0.00, 0, NULL, NULL, NULL, '{\"联系电话\":\"13123456789\",\"服务地址\":\"中国\",\"备注说明\":\"\"}', '2025-11-04 22:24:24', '2025-11-04 22:50:12', 0, '2025-11-05 00:00:00', 2, 215681561);
INSERT INTO `service_order` VALUES ('e093078cb31c4bfbab60d3d9d7a3f453', 5, 4, 23, 5, 419.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-09 16:45:00', NULL, '2025-09-09 12:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 51);
INSERT INTO `service_order` VALUES ('e492e84b8de646fe9af4f04641dbfebd', 9, 4, 14, 5, 289.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-08-12 12:00:00', NULL, '2025-08-12 08:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 75);
INSERT INTO `service_order` VALUES ('e4affa0c4af84ee8a0c3607fcfe042c7', 8, 2, 13, 5, 279.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-08-20 15:20:00', NULL, '2025-08-20 11:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 69);
INSERT INTO `service_order` VALUES ('e72136ede0034359a3e6bff6c2e1b9bd', 1, 6, 7, 5, 800.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-10-01 18:26:16', NULL, '2025-10-01 14:25:51', '2025-11-04 22:50:12', 0, NULL, NULL, 3);
INSERT INTO `service_order` VALUES ('e755082b5859431795a389fa879ae16f', 7, 1, 20, 5, 379.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-10 12:00:00', NULL, '2025-09-10 08:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 48);
INSERT INTO `service_order` VALUES ('eaa4890549484b42aeae300817a486bb', 8, 4, 9, 5, 219.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-05 15:30:00', NULL, '2025-09-05 11:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 59);
INSERT INTO `service_order` VALUES ('eae11f447d784c0aade6a5c1c62481b7', 5, 2, 9, 2, 14000.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, NULL, '{\"服务时间\":\"2025-05-21 00:00\",\"服务时长\":2,\"联系电话\":\"15252393509\",\"服务地址\":\"江苏省淮安市\",\"备注说明\":\"\",\"订单金额\":\"14000.00\"}', '2025-05-09 23:39:56', '2025-11-04 22:50:12', 0, NULL, NULL, 115);
INSERT INTO `service_order` VALUES ('ec6f7e8748a84d5fb95e7a5fb872d918', 7, 4, 5, 5, 159.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-13 17:30:00', NULL, '2025-09-13 13:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 43);
INSERT INTO `service_order` VALUES ('ee1abc29f838445391499b2925d1dff7', 5, 2, 1, 1, 400.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, NULL, '{\"服务时间\":\"2025-09-12 00:00\",\"服务时长\":2,\"联系电话\":\"15252393509\",\"服务地址\":\"江苏省淮安市\",\"备注说明\":\"\",\"订单金额\":\"400.00\"}', NULL, '2025-11-04 22:50:12', 0, NULL, NULL, 14);
INSERT INTO `service_order` VALUES ('eecc45d2fe5442ab9e39edb10fa6c67c', 5, 1, 3, 5, 300.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-18 13:38:40', '{\"服务时间\":\"2025-09-25 00:00\",\"服务时长\":2,\"联系电话\":\"15252393509\",\"服务地址\":\"江苏省淮安市\",\"备注说明\":\"\",\"订单金额\":\"1000.00\"}', '2025-09-17 11:41:13', '2025-11-04 22:50:12', 0, NULL, NULL, 10);
INSERT INTO `service_order` VALUES ('f2acb65e372247e29ab9c2da2f0b7f24', 8, 4, 12, 5, 259.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-08-06 15:20:00', NULL, '2025-08-06 11:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 79);
INSERT INTO `service_order` VALUES ('f2c3089134b949a0849a964d35dd35e6', 6, 3, 15, 5, 299.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-03 15:20:00', NULL, '2025-09-03 11:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 62);
INSERT INTO `service_order` VALUES ('f563f5ca8c5747f0b3c50b7aae8a9b89', 6, 1, 14, 5, 289.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-08 15:20:00', NULL, '2025-09-08 11:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 52);
INSERT INTO `service_order` VALUES ('f6c3e89ed83e4834a878d573406f568a', 5, 4, 4, 2, 600.00, 'ALIPAY', '2025-10-12 11:19:13', 600.00, 0.00, 0, NULL, NULL, NULL, '{\"联系电话\":\"15252393509\",\"服务地址\":\"江苏省淮安市\",\"备注说明\":\"\"}', '2025-10-12 11:19:08', '2025-11-04 22:50:12', 0, '2025-10-21 00:00:00', 2, 121);
INSERT INTO `service_order` VALUES ('f81be77332ae444e8474b2ce7777785e', 5, 2, 1, 1, 400.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, NULL, '{\"联系电话\":\"15252393509\",\"服务地址\":\"江苏省淮安市\",\"备注说明\":\"\"}', '2025-09-10 17:48:35', '2025-11-04 22:50:12', 0, '2025-09-11 00:00:00', 2, 119);
INSERT INTO `service_order` VALUES ('fe581e3e362e4cea8b6673e917589648', 8, 1, 26, 5, 459.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-08-10 18:45:00', NULL, '2025-08-10 14:00:00', '2025-11-04 22:50:12', 0, NULL, NULL, 104);
INSERT INTO `service_order` VALUES ('ff33a97b07f44f82b0d79986eda2f1c0', 5, 2, 2, 1, 800.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, NULL, '{\"服务时间\":\"2025-09-11 00:00\",\"服务时长\":2,\"联系电话\":\"15252393509\",\"服务地址\":\"江苏省淮安市\",\"备注说明\":\"\",\"订单金额\":\"800.00\"}', NULL, '2025-11-04 22:50:12', 0, NULL, NULL, 15);
INSERT INTO `service_order` VALUES ('ffefda78db474086ae7d5f8439562e34', 7, 2, 17, 5, 339.00, NULL, NULL, NULL, 0.00, 0, NULL, NULL, '2025-09-08 17:30:00', NULL, '2025-09-08 13:00:00', '2025-11-04 22:50:13', 0, NULL, NULL, 53);

-- ----------------------------
-- Table structure for service_review
-- ----------------------------
DROP TABLE IF EXISTS `service_review`;
CREATE TABLE `service_review`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `order_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `staff_id` bigint NOT NULL COMMENT '服务人员ID',
  `skill_rating` int NOT NULL COMMENT '技能满意度评分(1-5)',
  `attitude_rating` int NOT NULL COMMENT '服务态度评分(1-5)',
  `experience_rating` int NOT NULL COMMENT '综合体验评分(1-5)',
  `overall_rating` decimal(2, 1) NOT NULL COMMENT '总体评分',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '评价内容',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_staff_id`(`staff_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '服务评价表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of service_review
-- ----------------------------
INSERT INTO `service_review` VALUES (1, 'afb40dabc47f4f30a3bfed20f7203b8d', 6, 2, 5, 5, 4, 4.7, '服务很专业，态度很好，下次还会预约', '2025-09-17 14:25:51', '2025-11-04 22:50:12');
INSERT INTO `service_review` VALUES (2, '62298b2b11b34567856fc5f494497982', 6, 2, 5, 4, 5, 4.7, '打扫得很干净，很满意', '2025-09-19 14:25:51', '2025-11-04 22:50:12');
INSERT INTO `service_review` VALUES (4, 'b6a60222dc8c41e8835b061dd40cf0d7', 8, 4, 5, 4, 4, 4.3, '玻璃擦得很干净，速度也快', '2025-09-14 14:25:51', '2025-11-04 22:50:13');
INSERT INTO `service_review` VALUES (5, '78e647bda5fb455e86fb235fa27a3f2e', 9, 2, 4, 5, 4, 4.3, '服务态度很好，准时准点', '2025-09-16 14:25:51', '2025-11-04 22:50:13');
INSERT INTO `service_review` VALUES (7, 'eecc45d2fe5442ab9e39edb10fa6c67c', 5, 1, 5, 5, 4, 4.7, '好！', NULL, '2025-11-04 22:50:12');

-- ----------------------------
-- Table structure for service_staff
-- ----------------------------
DROP TABLE IF EXISTS `service_staff`;
CREATE TABLE `service_staff`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '服务人员ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `service_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '服务类型(JSON格式)',
  `experience` int NULL DEFAULT 0 COMMENT '工作经验年限',
  `rating` decimal(2, 1) NULL DEFAULT 5.0 COMMENT '综合评分',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '个人描述',
  `certificates` json NULL COMMENT '证书信息(JSON格式)',
  `work_area` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务区域',
  `total_orders` int NULL DEFAULT 0 COMMENT '总订单数',
  `completion_rate` decimal(5, 2) NULL DEFAULT 100.00 COMMENT '完成率',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '是否删除(0:未删除,1:已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_rating`(`rating` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '服务人员表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of service_staff
-- ----------------------------
INSERT INTO `service_staff` VALUES (1, 10, '[\"保洁\",\"护工\"]', 5, 4.7, '专业保洁护工，五年经验，擅长日常保洁和老人护理', '{\"健康证\": \"2023001\", \"职业资格证\": \"2023002\"}', '朝阳区,海淀区', 28, 96.60, '2025-09-21 14:24:29', '2025-11-04 22:50:10', 0);
INSERT INTO `service_staff` VALUES (2, 11, '[\"保洁服务\",\"护理服务\",\"母婴服务\"]', 8, 4.4, '专业月嫂，八年经验，持有高级育婴师证书', '[{\"code\": \"YYS2023011\", \"name\": \"高级育婴师证\", \"issueDate\": \"2023-01-15\"}]', '西城区,东城区', 25, 71.40, '2025-09-21 14:24:29', '2025-11-04 22:50:10', 0);
INSERT INTO `service_staff` VALUES (3, 12, '[\"保姆\",\"护工\"]', 6, 4.8, '专业保姆，六年经验，擅长家务和老人照顾', '{\"健康证\": \"2023005\", \"育婴师证\": \"2023006\"}', '朝阳区,东城区', 22, 91.70, '2025-09-21 14:24:29', '2025-11-04 22:50:10', 0);
INSERT INTO `service_staff` VALUES (4, 13, '[\"保洁\",\"保姆\"]', 4, 4.6, '专业保洁保姆，四年经验，细心负责', '{\"健康证\": \"2023007\", \"职业资格证\": \"2023008\"}', '海淀区,丰台区', 22, 91.70, '2025-09-21 14:24:29', '2025-11-04 22:50:10', 0);
INSERT INTO `service_staff` VALUES (5, 15, '[\"保洁\",\"护理\"]', 3, 4.5, '年轻有活力，擅长保洁和基础护理工作', '{\"健康证\": \"2023009\"}', '朝阳区,东城区', 0, 100.00, '2025-09-21 14:24:29', '2025-11-04 22:50:10', 0);
INSERT INTO `service_staff` VALUES (6, 16, '[\"教育培训\",\"课业辅导\"]', 5, 4.9, '重点中学在职教师，擅长数学和物理辅导', '{\"教师资格证\": \"2020001\"}', '海淀区,西城区', 1, 100.00, '2025-09-21 14:24:29', '2025-11-04 22:50:10', 0);

-- ----------------------------
-- Table structure for staff_service_item
-- ----------------------------
DROP TABLE IF EXISTS `staff_service_item`;
CREATE TABLE `staff_service_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `staff_id` bigint NOT NULL COMMENT '服务人员ID',
  `service_id` bigint NOT NULL COMMENT '服务项目ID',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态(0:禁用,1:正常)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_staff_service`(`staff_id` ASC, `service_id` ASC) USING BTREE,
  INDEX `idx_staff_id`(`staff_id` ASC) USING BTREE,
  INDEX `idx_service_id`(`service_id` ASC) USING BTREE,
  CONSTRAINT `fk_ssi_service_id` FOREIGN KEY (`service_id`) REFERENCES `service_item` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_ssi_staff_id` FOREIGN KEY (`staff_id`) REFERENCES `service_staff` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 29 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '员工服务项目关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of staff_service_item
-- ----------------------------
INSERT INTO `staff_service_item` VALUES (1, 1, 1, 1, '2025-09-21 14:30:00', '2025-09-21 14:30:00');
INSERT INTO `staff_service_item` VALUES (2, 1, 2, 1, '2025-09-21 14:30:00', '2025-09-21 14:30:00');
INSERT INTO `staff_service_item` VALUES (3, 1, 3, 1, '2025-09-21 14:30:00', '2025-09-21 14:30:00');
INSERT INTO `staff_service_item` VALUES (4, 1, 30, 1, '2025-09-21 14:30:00', '2025-09-21 14:30:00');
INSERT INTO `staff_service_item` VALUES (5, 1, 31, 1, '2025-09-21 14:30:00', '2025-09-21 14:30:00');
INSERT INTO `staff_service_item` VALUES (6, 2, 7, 1, '2025-09-21 14:30:00', '2025-09-21 14:30:00');
INSERT INTO `staff_service_item` VALUES (7, 2, 8, 1, '2025-09-21 14:30:00', '2025-09-21 14:30:00');
INSERT INTO `staff_service_item` VALUES (8, 2, 33, 1, '2025-09-21 14:30:00', '2025-09-21 14:30:00');
INSERT INTO `staff_service_item` VALUES (9, 2, 34, 1, '2025-09-21 14:30:00', '2025-09-21 14:30:00');
INSERT INTO `staff_service_item` VALUES (10, 2, 43, 1, '2025-09-21 14:30:00', '2025-09-21 14:30:00');
INSERT INTO `staff_service_item` VALUES (11, 3, 9, 1, '2025-09-21 14:30:00', '2025-09-21 14:30:00');
INSERT INTO `staff_service_item` VALUES (12, 3, 10, 1, '2025-09-21 14:30:00', '2025-09-21 14:30:00');
INSERT INTO `staff_service_item` VALUES (13, 3, 35, 1, '2025-09-21 14:30:00', '2025-09-21 14:30:00');
INSERT INTO `staff_service_item` VALUES (14, 3, 5, 1, '2025-09-21 14:30:00', '2025-09-21 14:30:00');
INSERT INTO `staff_service_item` VALUES (15, 3, 32, 1, '2025-09-21 14:30:00', '2025-09-21 14:30:00');
INSERT INTO `staff_service_item` VALUES (16, 4, 1, 1, '2025-09-21 14:30:00', '2025-09-21 14:30:00');
INSERT INTO `staff_service_item` VALUES (17, 4, 2, 1, '2025-09-21 14:30:00', '2025-09-21 14:30:00');
INSERT INTO `staff_service_item` VALUES (18, 4, 4, 1, '2025-09-21 14:30:00', '2025-09-21 14:30:00');
INSERT INTO `staff_service_item` VALUES (19, 4, 10, 1, '2025-09-21 14:30:00', '2025-09-21 14:30:00');
INSERT INTO `staff_service_item` VALUES (20, 4, 35, 1, '2025-09-21 14:30:00', '2025-09-21 14:30:00');
INSERT INTO `staff_service_item` VALUES (21, 5, 1, 1, '2025-09-21 14:30:00', '2025-09-21 14:30:00');
INSERT INTO `staff_service_item` VALUES (22, 5, 3, 1, '2025-09-21 14:30:00', '2025-09-21 14:30:00');
INSERT INTO `staff_service_item` VALUES (23, 5, 31, 1, '2025-09-21 14:30:00', '2025-09-21 14:30:00');
INSERT INTO `staff_service_item` VALUES (24, 5, 6, 1, '2025-09-21 14:30:00', '2025-09-21 14:30:00');
INSERT INTO `staff_service_item` VALUES (25, 5, 32, 1, '2025-09-21 14:30:00', '2025-09-21 14:30:00');
INSERT INTO `staff_service_item` VALUES (26, 6, 13, 1, '2025-09-21 14:30:00', '2025-09-21 14:30:00');
INSERT INTO `staff_service_item` VALUES (27, 6, 14, 1, '2025-09-21 14:30:00', '2025-09-21 14:30:00');
INSERT INTO `staff_service_item` VALUES (28, 6, 39, 1, '2025-09-21 14:30:00', '2025-09-21 14:30:00');

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜单名称',
  `path` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '菜单路径',
  `component` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '组件路径',
  `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '菜单图标',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',
  `pid` int NULL DEFAULT NULL COMMENT '父菜单ID',
  `sort_num` int NULL DEFAULT 1 COMMENT '排序号',
  `hidden` tinyint NULL DEFAULT 0 COMMENT '是否隐藏',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_pid`(`pid` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统菜单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, '工作台', '/dashboard', 'Dashboard', 'House', '后台管理首页', NULL, 1, 0, '2025-09-21 09:59:35', '2025-09-21 10:03:44');
INSERT INTO `sys_menu` VALUES (2, '人员管理', '/user', '', 'User', '用户相关管理', NULL, 2, 0, '2025-09-21 09:59:35', '2025-09-21 09:59:35');
INSERT INTO `sys_menu` VALUES (3, '服务中心', '/service', '', 'Service', '服务相关管理', NULL, 3, 0, '2025-09-21 09:59:35', '2025-09-21 09:59:35');
INSERT INTO `sys_menu` VALUES (4, '订单中心', '/order', '', 'Tickets', '订单相关管理', NULL, 4, 0, '2025-09-21 09:59:35', '2025-09-21 09:59:35');
INSERT INTO `sys_menu` VALUES (5, '系统配置', '/system', '', 'Setting', '系统设置管理', NULL, 5, 0, '2025-09-21 09:59:35', '2025-09-21 09:59:35');
INSERT INTO `sys_menu` VALUES (6, '客户列表', '/user/list', 'user/List', 'Avatar', '用户信息管理', 2, 1, 0, '2025-09-21 09:59:35', '2025-09-21 09:59:35');
INSERT INTO `sys_menu` VALUES (7, '家政人员', '/user/staff', 'user/Staff', 'UserFilled', '服务人员管理', 2, 2, 0, '2025-09-21 09:59:35', '2025-09-21 09:59:35');
INSERT INTO `sys_menu` VALUES (8, '服务管理', '/service/list', 'service/List', 'Grid', '服务项目列表', 3, 1, 0, '2025-09-21 09:59:35', '2025-09-21 09:59:35');
INSERT INTO `sys_menu` VALUES (9, '分类管理', '/service/category', 'service/Category', 'Files', '服务分类管理', 3, 2, 0, '2025-09-21 09:59:35', '2025-09-21 09:59:35');
INSERT INTO `sys_menu` VALUES (10, '收藏管理', '/service/favorite', 'service/FavoriteList', 'Collection', '服务类型管理', 3, 3, 0, '2025-09-21 09:59:35', '2025-09-21 18:57:24');
INSERT INTO `sys_menu` VALUES (11, '订单列表', '/order/list', 'order/List', 'Document', '订单列表管理', 4, 1, 0, '2025-09-21 09:59:35', '2025-09-21 09:59:35');
INSERT INTO `sys_menu` VALUES (12, '服务评价', '/order/reviews', 'order/Reviews', 'ChatLineRound', '订单评价列表', 4, 2, 0, '2025-09-21 09:59:35', '2025-09-21 09:59:35');
INSERT INTO `sys_menu` VALUES (13, '角色权限', '/system/role', 'system/RoleList', 'UserFilled', '角色权限管理', 5, 1, 0, '2025-09-21 09:59:35', '2025-09-21 11:03:39');
INSERT INTO `sys_menu` VALUES (14, '菜单配置', '/system/menu', 'system/MenuList', 'Menu', '系统菜单配置', 5, 2, 0, '2025-09-21 09:59:35', '2025-09-21 11:03:34');
INSERT INTO `sys_menu` VALUES (15, '个人信息', '/user/person', '/user/PersonInfo', 'Avatar', '', 2, 1, 0, '2025-09-21 19:51:33', '2025-09-21 19:51:56');
INSERT INTO `sys_menu` VALUES (16, '轮播图管理', '/system/banner', 'system/BannerList', 'Camera', '', 5, 1, 0, '2025-09-21 21:44:31', '2025-09-21 21:44:57');
INSERT INTO `sys_menu` VALUES (18, '退款管理', '/refund', 'order/RefundList', 'Money', '退款申请审核管理', 4, 3, 0, '2025-10-12 11:11:56', '2025-10-12 11:11:56');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色编码',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '是否删除(0:未删除,1:已删除)',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, 'ADMIN', '系统管理员', '超级管理员角色', 0, '2025-09-21 10:03:11', '2025-03-20 23:22:11');
INSERT INTO `sys_role` VALUES (2, 'USER', '普通用户', '普通注册用户角色', 0, '2025-09-21 10:03:15', '2025-09-21 10:03:18');
INSERT INTO `sys_role` VALUES (3, 'STAFF', '家政人员', '', 0, '2025-09-21 15:03:33', '2025-09-21 20:34:22');

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `role_id` int NOT NULL,
  `menu_id` int NOT NULL,
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`role_id`, `menu_id`) USING BTREE,
  INDEX `idx_menu_id`(`menu_id` ASC) USING BTREE,
  CONSTRAINT `fk_rm_menu_id` FOREIGN KEY (`menu_id`) REFERENCES `sys_menu` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_rm_role_id` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色菜单关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (1, 1, '2025-03-20 23:22:11');
INSERT INTO `sys_role_menu` VALUES (1, 2, '2025-03-20 23:22:11');
INSERT INTO `sys_role_menu` VALUES (1, 3, '2025-03-20 23:22:11');
INSERT INTO `sys_role_menu` VALUES (1, 4, '2025-03-20 23:22:11');
INSERT INTO `sys_role_menu` VALUES (1, 5, '2025-03-20 23:22:11');
INSERT INTO `sys_role_menu` VALUES (1, 6, '2025-03-20 23:22:11');
INSERT INTO `sys_role_menu` VALUES (1, 7, '2025-03-20 23:22:11');
INSERT INTO `sys_role_menu` VALUES (1, 8, '2025-03-20 23:22:11');
INSERT INTO `sys_role_menu` VALUES (1, 9, '2025-03-20 23:22:11');
INSERT INTO `sys_role_menu` VALUES (1, 10, '2025-03-20 23:22:11');
INSERT INTO `sys_role_menu` VALUES (1, 11, '2025-03-20 23:22:11');
INSERT INTO `sys_role_menu` VALUES (1, 12, '2025-03-20 23:22:11');
INSERT INTO `sys_role_menu` VALUES (1, 13, '2025-03-20 23:22:11');
INSERT INTO `sys_role_menu` VALUES (1, 14, '2025-03-20 23:22:11');
INSERT INTO `sys_role_menu` VALUES (1, 15, '2025-03-20 23:22:11');
INSERT INTO `sys_role_menu` VALUES (1, 16, '2025-03-20 23:22:11');
INSERT INTO `sys_role_menu` VALUES (1, 18, '2025-10-12 11:11:56');
INSERT INTO `sys_role_menu` VALUES (3, 1, '2025-09-21 20:34:22');
INSERT INTO `sys_role_menu` VALUES (3, 2, '2025-09-21 20:34:22');
INSERT INTO `sys_role_menu` VALUES (3, 4, '2025-09-21 20:34:22');
INSERT INTO `sys_role_menu` VALUES (3, 11, '2025-09-21 20:34:22');
INSERT INTO `sys_role_menu` VALUES (3, 15, '2025-09-21 20:34:22');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码(加密存储)',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色code(USER/STAFF/ADMIN)',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '姓名',
  `id_card` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证号',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地址',
  `avatar` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `gender` tinyint NULL DEFAULT NULL COMMENT '性别(0:女,1:男)',
  `age` int NULL DEFAULT NULL COMMENT '年龄',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态(0:禁用,1:正常)',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  INDEX `idx_role_code`(`role_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', '$2a$10$xzzdI9wl.Pk9T96tnX.x1Od6.hPFJsATbajlsjZHki3QdYEB00dmy', '1796145608@qq.com', '15252393509', 'ADMIN', '小张', '320821200104093915', '无', '/img/1740118118080.jpeg', 1, 22, 1, '2025-09-21 10:05:44', '2025-09-21 18:59:19');
INSERT INTO `user` VALUES (5, 'test', '$2a$10$xzzdI9wl.Pk9T96tnX.x1Od6.hPFJsATbajlsjZHki3QdYEB00dmy', '13123455@qq.com', '13123456789', 'USER', 'xiaoming', '320821200104093915', '中国', '/img/1740197274138.jpeg', 1, 25, 1, NULL, '2025-09-21 22:44:01');
INSERT INTO `user` VALUES (6, 'user001', 'e10adc3949ba59abbe56e057f20f883e', 'zhang@example.com', '13800138001', 'USER', '张三', '110101199001011234', '北京市朝阳区建国路1号', '', 0, 32, 1, '2025-09-21 14:24:29', '2025-09-21 19:07:14');
INSERT INTO `user` VALUES (7, 'user002', 'e10adc3949ba59abbe56e057f20f883e', 'li@example.com', '13800138002', 'USER', '李四', '110101199002022345', '北京市海淀区中关村5号', '', 1, 28, 1, '2025-09-21 14:24:29', '2025-09-21 19:07:18');
INSERT INTO `user` VALUES (8, 'user003', 'e10adc3949ba59abbe56e057f20f883e', 'chen@example.com', '13800138006', 'USER', '陈七', '110101199103033456', '北京市朝阳区三里屯1号', '', 1, 25, 1, '2025-09-21 14:24:29', '2025-09-21 19:07:22');
INSERT INTO `user` VALUES (9, 'user004', 'e10adc3949ba59abbe56e057f20f883e', 'liu@example.com', '13800138007', 'USER', '刘八', '110101199204044567', '北京市海淀区五道口2号', NULL, 1, 30, 1, '2025-09-21 14:24:29', '2025-09-21 19:07:25');
INSERT INTO `user` VALUES (10, 'staff', '$2a$10$xzzdI9wl.Pk9T96tnX.x1Od6.hPFJsATbajlsjZHki3QdYEB00dmy', 'wang@example.com', '13800138003', 'STAFF', '王五', '110101198503033456', '北京市西城区西直门2号', '/img/1740145121304.png', 0, 38, 1, '2025-09-21 14:24:29', '2025-09-21 20:00:08');
INSERT INTO `user` VALUES (11, 'staff002', '$2a$10$xzzdI9wl.Pk9T96tnX.x1Od6.hPFJsATbajlsjZHki3QdYEB00dmy', 'zhao@example.com', '13800138004', 'STAFF', '赵六', '110101198704044567', '北京市东城区东直门3号', '', 0, 35, 1, '2025-09-21 14:24:29', '2025-03-18 19:13:46');
INSERT INTO `user` VALUES (12, 'staff003', '$2a$10$xzzdI9wl.Pk9T96tnX.x1Od6.hPFJsATbajlsjZHki3QdYEB00dmy', 'sun@example.com', '13800138008', 'STAFF', '孙九', '110101198805055678', '北京市朝阳区望京1号', '', 0, 35, 1, '2025-09-21 14:24:29', '2025-05-28 23:50:05');
INSERT INTO `user` VALUES (13, 'staff004', '$2a$10$xzzdI9wl.Pk9T96tnX.x1Od6.hPFJsATbajlsjZHki3QdYEB00dmy', 'zhou@example.com', '13800138009', 'STAFF', '周十', '110101198906066789', '北京市海淀区上地3号', '', 1, 34, 1, '2025-09-21 14:24:29', '2025-05-28 16:36:29');
INSERT INTO `user` VALUES (14, 'admin001', 'e10adc3949ba59abbe56e057f20f883e', 'admin@example.com', '13800138005', 'ADMIN', '管理员', '110101199505055678', '北京市丰台区丰台路1号', NULL, 1, 28, 1, '2025-09-21 14:24:29', '2025-09-21 19:07:31');
INSERT INTO `user` VALUES (15, 'staff005', '$2a$10$xzzdI9wl.Pk9T96tnX.x1Od6.hPFJsATbajlsjZHki3QdYEB00dmy', 'wu@example.com', '13800138010', 'STAFF', '吴小美', '110101199007077890', '北京市朝阳区国贸2号', '/img/avatar5.jpg', 0, 33, 1, '2025-09-21 14:24:29', '2025-10-12 10:20:00');
INSERT INTO `user` VALUES (16, 'staff006', '$2a$10$xzzdI9wl.Pk9T96tnX.x1Od6.hPFJsATbajlsjZHki3QdYEB00dmy', 'zheng@example.com', '13800138011', 'STAFF', '郑小丽', '110101199108088901', '北京市海淀区中关村8号', '/img/avatar6.jpg', 0, 32, 1, '2025-09-21 14:24:29', '2025-10-12 10:20:00');
INSERT INTO `user` VALUES (17, 'user005', '$2a$10$xzzdI9wl.Pk9T96tnX.x1Od6.hPFJsATbajlsjZHki3QdYEB00dmy', 'qian@example.com', '13800138012', 'USER', '钱小明', '110101199209099012', '北京市东城区王府井1号', '', 1, 31, 1, '2025-09-21 14:24:29', '2025-10-12 10:20:00');
INSERT INTO `user` VALUES (18, 'user006', '$2a$10$xzzdI9wl.Pk9T96tnX.x1Od6.hPFJsATbajlsjZHki3QdYEB00dmy', 'xu@example.com', '13800138013', 'USER', '徐小红', '110101199310100123', '北京市西城区金融街5号', '', 0, 30, 1, '2025-09-21 14:24:29', '2025-10-12 10:20:00');

-- ----------------------------
-- Table structure for user_favorite
-- ----------------------------
DROP TABLE IF EXISTS `user_favorite`;
CREATE TABLE `user_favorite`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `service_id` bigint NOT NULL COMMENT '服务ID',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_service`(`user_id` ASC, `service_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户收藏表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_favorite
-- ----------------------------
INSERT INTO `user_favorite` VALUES (2, 6, 8, '2025-08-23 14:20:00');
INSERT INTO `user_favorite` VALUES (3, 6, 15, '2025-08-22 09:45:00');
INSERT INTO `user_favorite` VALUES (4, 7, 2, '2025-08-21 16:30:00');
INSERT INTO `user_favorite` VALUES (5, 7, 11, '2025-08-20 11:25:00');
INSERT INTO `user_favorite` VALUES (6, 7, 21, '2025-08-19 13:40:00');
INSERT INTO `user_favorite` VALUES (7, 7, 28, '2025-08-18 15:15:00');
INSERT INTO `user_favorite` VALUES (8, 8, 5, '2025-08-17 09:00:00');
INSERT INTO `user_favorite` VALUES (9, 8, 13, '2025-08-16 10:30:00');
INSERT INTO `user_favorite` VALUES (10, 8, 19, '2025-08-15 14:20:00');
INSERT INTO `user_favorite` VALUES (11, 8, 25, '2025-08-14 16:45:00');
INSERT INTO `user_favorite` VALUES (12, 9, 1, '2025-08-13 11:30:00');
INSERT INTO `user_favorite` VALUES (13, 9, 7, '2025-08-12 13:15:00');
INSERT INTO `user_favorite` VALUES (14, 9, 16, '2025-08-11 15:40:00');
INSERT INTO `user_favorite` VALUES (15, 9, 23, '2025-08-10 17:20:00');
INSERT INTO `user_favorite` VALUES (26, 5, 13, '2025-09-22 15:55:24');
INSERT INTO `user_favorite` VALUES (30, 5, 4, '2025-11-03 16:08:08');
INSERT INTO `user_favorite` VALUES (31, 5, 6, '2025-11-03 16:08:09');

SET FOREIGN_KEY_CHECKS = 1;
