-- ======================================================================
-- shop-cart：购物车持久化表
-- 按 user_id + product_id 唯一约束，避免同一商品重复入库
-- ======================================================================
USE `eastern_market`;

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
