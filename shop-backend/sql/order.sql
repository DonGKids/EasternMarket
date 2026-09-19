-- ======================================================================
-- shop-order 微服务建表脚本（订单主表 + 订单明细）
-- 数据库：eastern_market
-- 执行方式：mysql -uroot -p125044 < order.sql
-- ======================================================================

CREATE DATABASE IF NOT EXISTS `eastern_market` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `eastern_market`;

-- 订单主表
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

-- 订单明细表
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
