SET NAMES utf8mb4;
-- ======================================================================
-- 商品表测试数据
-- 数据库：eastern_market
-- 执行方式：mysql -uroot -p125044 < seed-product.sql
-- 重复执行安全（使用 WHERE NOT EXISTS）
-- ======================================================================

USE `eastern_market`;

-- 清空旧商品数据（如需重置请取消注释）
-- DELETE FROM `product`;

-- ======================================================================
-- 分类数据
-- ======================================================================

INSERT INTO `category` (`id`, `name`, `sort`, `status`, `create_time`, `update_time`)
SELECT 1, '七夕限时', 1, 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `category` WHERE `id` = 1);

INSERT INTO `category` (`id`, `name`, `sort`, `status`, `create_time`, `update_time`)
SELECT 2, '数码办公', 2, 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `category` WHERE `id` = 2);

INSERT INTO `category` (`id`, `name`, `sort`, `status`, `create_time`, `update_time`)
SELECT 3, '服饰配件', 3, 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `category` WHERE `id` = 3);

INSERT INTO `category` (`id`, `name`, `sort`, `status`, `create_time`, `update_time`)
SELECT 4, '美食礼品', 4, 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `category` WHERE `id` = 4);

INSERT INTO `category` (`id`, `name`, `sort`, `status`, `create_time`, `update_time`)
SELECT 5, '图书文具', 5, 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `category` WHERE `id` = 5);

-- ======================================================================
-- 商品数据（与前端原硬编码数据一致）
-- ======================================================================

-- 七夕限时商品
INSERT INTO `product` (`id`, `name`, `description`, `price`, `original_price`, `badge`, `is_flash`, `image_url`, `category_id`, `stock`, `sales`, `status`, `create_time`, `update_time`)
SELECT 1, '情侣对戒礼盒', '925银对戒，刻字定制，附赠首饰袋', 299.00, 499.00, '热销', 1, '/products/1.jpg', 1, 50, 0, 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `product` WHERE `id` = 1);

INSERT INTO `product` (`id`, `name`, `description`, `price`, `original_price`, `badge`, `is_flash`, `image_url`, `category_id`, `stock`, `sales`, `status`, `create_time`, `update_time`)
SELECT 2, '永生花玫瑰礼盒', '进口厄瓜多尔玫瑰，可保存3年', 199.00, 329.00, '新品', 1, '/products/2.jpg', 4, 80, 0, 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `product` WHERE `id` = 2);

INSERT INTO `product` (`id`, `name`, `description`, `price`, `original_price`, `badge`, `is_flash`, `image_url`, `category_id`, `stock`, `sales`, `status`, `create_time`, `update_time`)
SELECT 3, '情侣睡衣套装', '纯棉情侣款，丝绒刺绣，两色可选', 159.00, 259.00, NULL, 1, '/products/3.jpg', 3, 30, 0, 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `product` WHERE `id` = 3);

INSERT INTO `product` (`id`, `name`, `description`, `price`, `original_price`, `badge`, `is_flash`, `image_url`, `category_id`, `stock`, `sales`, `status`, `create_time`, `update_time`)
SELECT 4, '情侣手链对装', '钛钢防敏，磁吸相扣，含礼盒', 128.00, 199.00, '特价', 1, '/products/4.jpg', 3, 100, 0, 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `product` WHERE `id` = 4);

INSERT INTO `product` (`id`, `name`, `description`, `price`, `original_price`, `badge`, `is_flash`, `image_url`, `category_id`, `stock`, `sales`, `status`, `create_time`, `update_time`)
SELECT 5, '巧克力礼盒', '比利时进口，16颗手工松露', 139.00, 219.00, NULL, 1, '/products/5.jpg', 4, 120, 0, 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `product` WHERE `id` = 5);

INSERT INTO `product` (`id`, `name`, `description`, `price`, `original_price`, `badge`, `is_flash`, `image_url`, `category_id`, `stock`, `sales`, `status`, `create_time`, `update_time`)
SELECT 6, '情侣香氛蜡烛', '玫瑰+檀木对香，燃烧40小时', 99.00, 169.00, '新品', 1, '/products/6.jpg', 4, 60, 0, 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `product` WHERE `id` = 6);

-- 常规商品
INSERT INTO `product` (`id`, `name`, `description`, `price`, `original_price`, `badge`, `is_flash`, `image_url`, `category_id`, `stock`, `sales`, `status`, `create_time`, `update_time`)
SELECT 7, '三模87配列键盘', '蓝牙/2.4G/有线三模，青轴，RGB背光', 229.00, 359.00, '8折', 0, '/products/7.jpg', 2, 40, 0, 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `product` WHERE `id` = 7);

INSERT INTO `product` (`id`, `name`, `description`, `price`, `original_price`, `badge`, `is_flash`, `image_url`, `category_id`, `stock`, `sales`, `status`, `create_time`, `update_time`)
SELECT 8, '三模鼠标', '蓝牙/2.4G/有线，静音微动，16000DPI', 149.00, 219.00, '8折', 0, '/products/8.jpg', 2, 60, 0, 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `product` WHERE `id` = 8);

INSERT INTO `product` (`id`, `name`, `description`, `price`, `original_price`, `badge`, `is_flash`, `image_url`, `category_id`, `stock`, `sales`, `status`, `create_time`, `update_time`)
SELECT 9, '头戴式无线耳机', '主动降噪，40小时续航，蛋白皮耳罩', 329.00, 499.00, '8折', 0, '/products/9.jpg', 2, 35, 0, 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `product` WHERE `id` = 9);

INSERT INTO `product` (`id`, `name`, `description`, `price`, `original_price`, `badge`, `is_flash`, `image_url`, `category_id`, `stock`, `sales`, `status`, `create_time`, `update_time`)
SELECT 10, '双肩电脑包', '15.6英寸防泼水，减震隔层，大容量', 129.00, 199.00, '8折', 0, '/products/10.jpg', 3, 80, 0, 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `product` WHERE `id` = 10);

INSERT INTO `product` (`id`, `name`, `description`, `price`, `original_price`, `badge`, `is_flash`, `image_url`, `category_id`, `stock`, `sales`, `status`, `create_time`, `update_time`)
SELECT 11, '英语复习资料', '考研真题精解+核心词汇，2026版', 69.00, 98.00, '7折', 0, '/products/11.jpg', 5, 200, 0, 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `product` WHERE `id` = 11);

INSERT INTO `product` (`id`, `name`, `description`, `price`, `original_price`, `badge`, `is_flash`, `image_url`, `category_id`, `stock`, `sales`, `status`, `create_time`, `update_time`)
SELECT 12, '汉字字帖（随机古籍）', '随机发送《兰亭序》《多宝塔碑》等经典碑帖', 39.00, 59.00, '7折', 0, '/products/12.jpg', 5, 150, 0, 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `product` WHERE `id` = 12);

-- ======================================================================
-- 完成！共 5 条分类 + 12 条商品
-- ======================================================================
