-- ======================================================================
-- shop-user 微服务建表脚本（用户表）
-- 数据库：eastern_market
-- 执行方式：mysql -uroot -p125044 < user.sql
-- ======================================================================

CREATE DATABASE IF NOT EXISTS `eastern_market` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `eastern_market`;

-- 用户表
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
