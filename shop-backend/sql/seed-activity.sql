-- ======================================================================
-- 活动表 + 优惠券模板 测试数据
-- 数据库：eastern_market
-- 执行方式：mysql -uroot -p125044 < seed-activity.sql
-- 重复执行安全（全部使用 WHERE NOT EXISTS）
-- ======================================================================

USE `eastern_market`;

-- 先清理旧数据（可选，如需重置请取消注释）
-- DELETE FROM `template_coupon` WHERE `activity_id` IS NOT NULL;
-- DELETE FROM `activity`;

-- ======================================================================
-- 活动表：4 条（2 常驻 + 2 限时，均为进行中）
-- ======================================================================

INSERT INTO `activity` (`id`, `name`, `category`, `description`, `countdown_text`, `urgent`, `rules`, `status`, `sort`, `create_time`, `update_time`)
SELECT 1, '新人首单福利', 1, '新用户注册即可自动领取专属立减券，首单购物享受额外优惠，无门槛限制，超值体验等你来！', NULL, 0,
       '["仅限新注册用户领取","每账号限领 1 张","有效期 30 天，过期作废"]',
       1, 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `activity` WHERE `id` = 1);

INSERT INTO `activity` (`id`, `name`, `category`, `description`, `countdown_text`, `urgent`, `rules`, `status`, `sort`, `create_time`, `update_time`)
SELECT 2, '会员日特惠', 2, '每月 11 号会员日，全场限时抢券！满 100 减 20、85 折券等你来抢，手快有手慢无！', '0天 12:00:00', 1,
       '["活动时间：每月 11 号 00:00 - 23:59","券数量有限，抢完即止","每人每券限领 1 张","优惠券有效期 7 天"]',
       1, 2, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `activity` WHERE `id` = 2);

INSERT INTO `activity` (`id`, `name`, `category`, `description`, `countdown_text`, `urgent`, `rules`, `status`, `sort`, `create_time`, `update_time`)
SELECT 3, '816 乞巧节·甜蜜季', 2, '乞巧节浪漫献礼，满 50 减 10、9 折券甜蜜上线，为心爱的她/他选购一份惊喜吧！', '2天 05:30:12', 1,
       '["活动时间：8 月 16 日 - 8 月 22 日","全场商品均可使用","每人每券限领 1 张","优惠券有效期至活动结束后 7 天"]',
       1, 3, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `activity` WHERE `id` = 3);

INSERT INTO `activity` (`id`, `name`, `category`, `description`, `countdown_text`, `urgent`, `rules`, `status`, `sort`, `create_time`, `update_time`)
SELECT 4, '周年庆典·全场钜惠', 1, '平台周年庆，钜惠来袭！满 200 减 50、7.5 折券，邀请好友额外得券，更有签到领券福利！', NULL, 0,
       '["全场商品通用","可与邀请奖励叠加","签到领券每日限 1 张","优惠券有效期 14 天"]',
       1, 4, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `activity` WHERE `id` = 4);

-- ======================================================================
-- 优惠券模板：按活动关联，覆盖 4 种 get_way
--   get_way: 1=新人专享 2=限时抢券 3=邀请奖励 4=签到领取
--   type:    1=满减 2=折扣 3=立减
-- ======================================================================

-- ---------- 活动 1：新人首单福利（get_way=1 新人专享） ----------

INSERT INTO `template_coupon` (`id`, `name`, `type`, `face_value`, `threshold`, `total_count`, `received_count`, `per_limit`, `get_way`, `activity_id`, `scope_type`, `valid_days`, `status`, `create_time`, `update_time`)
SELECT 101, '新人立减 ¥10', 3, 10.00, 0.00, 0, 0, 1, 1, 1, 1, 30, 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `template_coupon` WHERE `id` = 101);

INSERT INTO `template_coupon` (`id`, `name`, `type`, `face_value`, `threshold`, `total_count`, `received_count`, `per_limit`, `get_way`, `activity_id`, `scope_type`, `valid_days`, `status`, `create_time`, `update_time`)
SELECT 102, '新人满减 ¥20', 1, 20.00, 99.00, 0, 0, 1, 1, 1, 1, 30, 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `template_coupon` WHERE `id` = 102);

-- ---------- 活动 2：会员日特惠（get_way=2 限时抢券） ----------

INSERT INTO `template_coupon` (`id`, `name`, `type`, `face_value`, `threshold`, `total_count`, `received_count`, `per_limit`, `get_way`, `activity_id`, `scope_type`, `valid_start`, `valid_end`, `valid_days`, `status`, `create_time`, `update_time`)
SELECT 201, '满 100 减 20', 1, 20.00, 100.00, 500, 0, 1, 2, 2, 1, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 7, 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `template_coupon` WHERE `id` = 201);

INSERT INTO `template_coupon` (`id`, `name`, `type`, `discount`, `threshold`, `total_count`, `received_count`, `per_limit`, `get_way`, `activity_id`, `scope_type`, `valid_start`, `valid_end`, `valid_days`, `status`, `create_time`, `update_time`)
SELECT 202, '8.5 折券', 2, 0.85, 0.00, 300, 0, 1, 2, 2, 1, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 7, 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `template_coupon` WHERE `id` = 202);

-- ---------- 活动 3：816 乞巧节（get_way=2 限时抢券） ----------

INSERT INTO `template_coupon` (`id`, `name`, `type`, `face_value`, `threshold`, `total_count`, `received_count`, `per_limit`, `get_way`, `activity_id`, `scope_type`, `valid_start`, `valid_end`, `valid_days`, `status`, `create_time`, `update_time`)
SELECT 301, '满 50 减 10', 1, 10.00, 50.00, 800, 0, 1, 2, 3, 1, NOW(), DATE_ADD(NOW(), INTERVAL 14 DAY), 7, 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `template_coupon` WHERE `id` = 301);

INSERT INTO `template_coupon` (`id`, `name`, `type`, `discount`, `threshold`, `total_count`, `received_count`, `per_limit`, `get_way`, `activity_id`, `scope_type`, `valid_start`, `valid_end`, `valid_days`, `status`, `create_time`, `update_time`)
SELECT 302, '9 折券', 2, 0.90, 0.00, 500, 0, 1, 2, 3, 1, NOW(), DATE_ADD(NOW(), INTERVAL 14 DAY), 7, 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `template_coupon` WHERE `id` = 302);

-- ---------- 活动 4：周年庆典（get_way=3 邀请奖励 + get_way=4 签到领取） ----------

INSERT INTO `template_coupon` (`id`, `name`, `type`, `face_value`, `threshold`, `total_count`, `received_count`, `per_limit`, `get_way`, `activity_id`, `scope_type`, `valid_days`, `status`, `create_time`, `update_time`)
SELECT 401, '满 200 减 50', 1, 50.00, 200.00, 200, 0, 3, 3, 4, 1, 14, 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `template_coupon` WHERE `id` = 401);

INSERT INTO `template_coupon` (`id`, `name`, `type`, `discount`, `threshold`, `total_count`, `received_count`, `per_limit`, `get_way`, `activity_id`, `scope_type`, `valid_days`, `status`, `create_time`, `update_time`)
SELECT 402, '7.5 折券', 2, 0.75, 0.00, 200, 0, 3, 3, 4, 1, 14, 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `template_coupon` WHERE `id` = 402);

INSERT INTO `template_coupon` (`id`, `name`, `type`, `face_value`, `threshold`, `total_count`, `received_count`, `per_limit`, `get_way`, `activity_id`, `scope_type`, `valid_days`, `status`, `create_time`, `update_time`)
SELECT 403, '签到立减 ¥5', 3, 5.00, 0.00, 1000, 0, 1, 4, 4, 1, 7, 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `template_coupon` WHERE `id` = 403);

-- ======================================================================
-- 完成！共 4 条活动 + 9 条优惠券模板
-- 活动 ID 对应关系：
--   1 新人首单福利    -> 券 101,102
--   2 会员日特惠      -> 券 201,202
--   3 816 乞巧节      -> 券 301,302
--   4 周年庆典        -> 券 401,402,403
-- ======================================================================
