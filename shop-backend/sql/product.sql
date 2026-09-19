-- ======================================================================
-- shop-product 微服务建表脚本（分类 + 商品）
-- 数据库：buka_shop
-- 执行方式：mysql -uroot -p125044 < product.sql
-- ======================================================================

CREATE DATABASE IF NOT EXISTS `buka_shop` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `buka_shop`;

-- 商品分类表
CREATE TABLE IF NOT EXISTS `category` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(50)  NOT NULL COMMENT '分类名称',
  `sort`        INT          DEFAULT 0 COMMENT '排序（越小越靠前）',
  `status`      TINYINT      DEFAULT 1 COMMENT '状态：0 禁用，1 启用',
  `create_time` DATETIME     DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME     DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 商品表
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
