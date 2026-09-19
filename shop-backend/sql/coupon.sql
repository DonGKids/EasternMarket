-- ======================================================================
-- shop-coupon 微服务建表脚本（优惠券模板 / 用户券 / 活动中心）
-- 数据库：buka_shop
-- 执行方式：mysql -uroot -p125044 < coupon.sql
-- ======================================================================

CREATE DATABASE IF NOT EXISTS `buka_shop` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `buka_shop`;

-- --------------------------------------------------------------------
-- 优惠券模板表（规则定义，1 个模板可被多用户领取）
-- --------------------------------------------------------------------
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

-- --------------------------------------------------------------------
-- 用户优惠券表（领取后生成的实例，跟踪每张券的状态与核销记录）
-- --------------------------------------------------------------------
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

-- --------------------------------------------------------------------
-- 活动表（活动中心展示，一个活动可关联多个优惠券模板）
-- --------------------------------------------------------------------
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

-- --------------------------------------------------------------------
-- 初始化数据：新人券模板（ID 固定为 1，不可删除/修改）
-- 对应 shop-user 模块 application.yml 中 coupon.new-user-template-id: 1
-- --------------------------------------------------------------------
INSERT INTO `template_coupon` (`id`, `name`, `type`, `face_value`, `threshold`, `total_count`, `received_count`, `per_limit`, `get_way`, `scope_type`, `valid_days`, `status`, `create_time`, `update_time`)
SELECT 1, '新人立减券', 3, 10.00, 0.00, 0, 0, 1, 1, 1, 30, 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `template_coupon` WHERE `id` = 1);
