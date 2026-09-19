-- ======================================================================
-- eastern_market 数据库一键建表脚本（合并版）
-- 汇总自 4 个微服务的分模块 SQL：
--   user.sql / product.sql / coupon.sql / order.sql
-- 共 8 张表 + 1 条种子数据（新人券模板 id=1）
--
-- 执行方式：mysql -uroot -p125044 < all.sql
-- 重复执行安全（全部使用 IF NOT EXISTS / WHERE NOT EXISTS）
-- ======================================================================

CREATE DATABASE IF NOT EXISTS `eastern_market` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `eastern_market`;

-- ======================================================================
-- 1. shop-user：用户表
-- ======================================================================
CREATE TABLE IF NOT EXISTS `user` (
  `id`        INT          NOT NULL AUTO_INCREMENT,
  `mail`      VARCHAR(100) NOT NULL COMMENT '邮箱（登录账号）',
  `password`  VARCHAR(100) NOT NULL COMMENT '密码（演示用明文，生产请加密）',
  `nickname`  VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
  `age`       INT          DEFAULT NULL COMMENT '年龄',
  `gender`    TINYINT      DEFAULT NULL COMMENT '性别：0 女，1 男',
  `avatar`    VARCHAR(255) DEFAULT NULL COMMENT '头像访问URL',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mail` (`mail`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ======================================================================
-- 2. shop-product：商品分类表 + 商品表
-- ======================================================================
CREATE TABLE IF NOT EXISTS `category` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(50)  NOT NULL COMMENT '分类名称',
  `sort`        INT          DEFAULT 0 COMMENT '排序（越小越靠前）',
  `status`      TINYINT      DEFAULT 1 COMMENT '状态：0 禁用，1 启用',
  `create_time` DATETIME     DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME     DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

CREATE TABLE IF NOT EXISTS `product` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT,
  `name`          VARCHAR(100)  NOT NULL COMMENT '商品名称',
  `description`   VARCHAR(500)  DEFAULT NULL COMMENT '商品描述',
  `price`         DECIMAL(10,2) NOT NULL COMMENT '现价',
  `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
  `badge`         VARCHAR(20)   DEFAULT NULL COMMENT '角标文字（如热销、新品、8折）',
  `is_flash`      TINYINT       DEFAULT 0 COMMENT '是否限时商品：0 否，1 是',
  `image_url`     VARCHAR(255)  DEFAULT NULL COMMENT '商品图片路径',
  `category_id`   BIGINT        DEFAULT NULL COMMENT '分类ID',
  `stock`         INT           DEFAULT 0 COMMENT '库存数量',
  `sales`         INT           DEFAULT 0 COMMENT '销量',
  `status`        TINYINT       DEFAULT 1 COMMENT '状态：0 下架，1 上架',
  `create_time`   DATETIME      DEFAULT NULL COMMENT '创建时间',
  `update_time`   DATETIME      DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_flash` (`is_flash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- ======================================================================
-- 3. shop-coupon：优惠券模板 + 用户券 + 活动表
-- ======================================================================
CREATE TABLE IF NOT EXISTS `template_coupon` (
  `id`             BIGINT        NOT NULL AUTO_INCREMENT,
  `name`           VARCHAR(50)   NOT NULL COMMENT '券名称',
  `type`           TINYINT       NOT NULL COMMENT '类型：1 满减，2 折扣，3 立减',
  `face_value`     DECIMAL(10,2) DEFAULT NULL COMMENT '面值/立减金额（满减、立减时使用）',
  `discount`       DECIMAL(10,2) DEFAULT NULL COMMENT '折扣率（折扣券，如 0.85 表示 85 折）',
  `threshold`      DECIMAL(10,2) DEFAULT 0.00 COMMENT '使用门槛（满多少可用，0 表示无门槛）',
  `total_count`    INT           DEFAULT 0 COMMENT '发行总量，0 表示不限',
  `received_count` INT           DEFAULT 0 COMMENT '已领取数量',
  `per_limit`      INT           DEFAULT 1 COMMENT '每人限领数量',
  `get_way`        TINYINT       DEFAULT NULL COMMENT '获取方式：1 新人专享，2 限时抢券，3 邀请奖励，4 签到领取',
  `activity_id`    BIGINT        DEFAULT NULL COMMENT '关联活动 ID（activity.id），为空表示不属于任何活动',
  `scope_type`     TINYINT       DEFAULT 1 COMMENT '适用范围：1 全场，2 指定分类，3 指定商品',
  `scope_id`       BIGINT        DEFAULT NULL COMMENT '关联的分类/商品 ID（scope_type=1 时为空）',
  `valid_start`    DATETIME      DEFAULT NULL COMMENT '领取开始时间',
  `valid_end`      DATETIME      DEFAULT NULL COMMENT '领取结束时间',
  `valid_days`     INT           DEFAULT NULL COMMENT '领取后有效天数（领取时据此计算 expire_time）',
  `status`         TINYINT       DEFAULT 0 COMMENT '模板状态：0 未开始，1 进行中，2 已结束',
  `create_time`    DATETIME      DEFAULT NULL COMMENT '创建时间',
  `update_time`    DATETIME      DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_activity` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券模板表';

CREATE TABLE IF NOT EXISTS `user_coupon` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`      BIGINT       NOT NULL COMMENT '用户 ID',
  `template_id`  BIGINT       NOT NULL COMMENT '关联的优惠券模板 ID',
  `status`       TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0 未使用，1 已使用，2 已过期',
  `receive_time` DATETIME     DEFAULT NULL COMMENT '领取时间',
  `expire_time`  DATETIME     DEFAULT NULL COMMENT '过期时间（领取时根据模板 valid_days 计算落库）',
  `used_time`    DATETIME     DEFAULT NULL COMMENT '核销时间',
  `order_id`     VARCHAR(32)  DEFAULT NULL COMMENT '核销订单号',
  `create_time`  DATETIME     DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_template_id` (`template_id`),
  KEY `idx_status` (`status`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';

CREATE TABLE IF NOT EXISTS `activity` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT,
  `name`           VARCHAR(50)  NOT NULL COMMENT '活动名称',
  `category`       TINYINT      NOT NULL DEFAULT 1 COMMENT '分类：1 常驻，2 限时',
  `description`    VARCHAR(500) DEFAULT NULL COMMENT '活动描述',
  `countdown_text` VARCHAR(50)  DEFAULT NULL COMMENT '倒计时文案（如 "2天 05:30:12"），为空则不显示倒计时',
  `urgent`         TINYINT      DEFAULT 0 COMMENT '是否紧急（即将结束）：0 否，1 是',
  `rules`          TEXT         DEFAULT NULL COMMENT '活动规则（JSON 数组字符串，如 ["规则1","规则2"]）',
  `status`         TINYINT      DEFAULT 1 COMMENT '状态：0 未开始，1 进行中，2 已结束',
  `sort`           INT          DEFAULT 0 COMMENT '排序（越小越靠前）',
  `create_time`    DATETIME     DEFAULT NULL COMMENT '创建时间',
  `update_time`    DATETIME     DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动表';

-- 新人券模板（ID 固定为 1，不可删除/修改）
-- 对应 shop-user 模块 application.yml 中 coupon.new-user-template-id: 1
INSERT INTO `template_coupon` (`id`, `name`, `type`, `face_value`, `threshold`, `total_count`, `received_count`, `per_limit`, `get_way`, `scope_type`, `valid_days`, `status`, `create_time`, `update_time`)
SELECT 1, '新人立减券', 3, 10.00, 0.00, 0, 0, 1, 1, 1, 30, 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `template_coupon` WHERE `id` = 1);

-- ======================================================================
-- 4. shop-order：订单主表 + 订单明细表
-- ======================================================================
CREATE TABLE IF NOT EXISTS `orders` (
  `id`             BIGINT        NOT NULL AUTO_INCREMENT,
  `order_no`       VARCHAR(32)   NOT NULL COMMENT '订单号（业务唯一，如 ORD+时间戳）',
  `user_id`        BIGINT        NOT NULL COMMENT '用户ID',
  `total_amount`   DECIMAL(10,2) NOT NULL COMMENT '商品总金额',
  `discount_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '优惠金额',
  `pay_amount`     DECIMAL(10,2) NOT NULL COMMENT '实付金额 = total_amount - discount_amount',
  `user_coupon_id` BIGINT        DEFAULT NULL COMMENT '使用的用户券ID（user_coupon.id）',
  `status`         TINYINT       NOT NULL DEFAULT 0 COMMENT '状态：0 待付款，1 已付款，2 已发货，3 已完成，4 已取消',
  `consignee`      VARCHAR(50)   DEFAULT NULL COMMENT '收货人',
  `phone`          VARCHAR(20)   DEFAULT NULL COMMENT '联系电话',
  `address`        VARCHAR(255) DEFAULT NULL COMMENT '收货地址',
  `pay_time`       DATETIME      DEFAULT NULL COMMENT '支付时间',
  `ship_time`      DATETIME      DEFAULT NULL COMMENT '发货时间',
  `finish_time`    DATETIME      DEFAULT NULL COMMENT '完成时间',
  `cancel_time`    DATETIME      DEFAULT NULL COMMENT '取消时间',
  `create_time`    DATETIME      DEFAULT NULL COMMENT '创建时间',
  `update_time`    DATETIME      DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_user_coupon` (`user_coupon_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

CREATE TABLE IF NOT EXISTS `order_item` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT,
  `order_id`      BIGINT        NOT NULL COMMENT '关联订单ID（orders.id）',
  `product_id`    BIGINT        NOT NULL COMMENT '商品ID',
  `product_name`  VARCHAR(100)  NOT NULL COMMENT '商品名称（下单时快照）',
  `product_image` VARCHAR(255) DEFAULT NULL COMMENT '商品图片（下单时快照）',
  `price`         DECIMAL(10,2) NOT NULL COMMENT '单价（下单时快照）',
  `quantity`      INT           NOT NULL COMMENT '购买数量',
  `subtotal`      DECIMAL(10,2) NOT NULL COMMENT '小计 = price * quantity',
  `create_time`   DATETIME      DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- ======================================================================
-- 5. shop-cart：购物车持久化表
-- ======================================================================
CREATE TABLE IF NOT EXISTS `cart_item` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT       NOT NULL COMMENT '用户 ID',
  `product_id`  BIGINT       NOT NULL COMMENT '商品 ID',
  `quantity`    INT          NOT NULL DEFAULT 1 COMMENT '购买数量',
  `create_time` DATETIME     DEFAULT NULL COMMENT '加入时间',
  `update_time` DATETIME     DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车条目表';

-- ======================================================================
-- 完成：共 9 张表
-- user / category / product / template_coupon / user_coupon / activity
--   / orders / order_item / cart_item
-- ======================================================================
