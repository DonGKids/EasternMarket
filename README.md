# Eastern Market 电商微服务系统

![Version](https://img.shields.io/badge/version-v1.0.0-blue)
![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.14-green)
![Vue](https://img.shields.io/badge/Vue-3.4-brightgreen)
![License](https://img.shields.io/badge/License-Study-lightgrey)

基于 **Spring Cloud Alibaba + Vue 3** 的全栈电商示例项目,采用 Docker Compose 一键编排,涵盖用户、商品、优惠券、购物车、订单与支付、网关六大微服务,前端独立 SPA,适合作为 Spring Cloud 2025 学习脚手架。

> **当前版本**:v1.0.0(首发版本,部署于 GitHub)。已具备完整的电商业务链路与 Docker 一键启动能力,部分功能仍在持续迭代中,详见文末「项目待完善的部分」。

## ✨ 功能概览

| 模块 | 主要功能 |
|---|---|
| **shop-user** (7071) | 注册 / 登录、头像上传、收货地址管理、邮箱验证码 |
| **shop-coupon** (7072) | 优惠券模板、用户领券、活动券发放 |
| **shop-product** (7073) | 商品列表、分类管理、商品图片维护 |
| **shop-order** (7074) | 下单、订单查询、**支付宝沙箱支付**、支付回调 |
| **shop-cart** (7075) | 购物车增删改查 |
| **shop-gateway** (7070) | Spring Cloud Gateway 统一路由、StripPrefix 转发 |
| **shop-frontend** (1160) | Vue 3 SPA,路由守卫鉴权,七牛云图片上传 |

## 🧱 技术栈

### 后端
- **Spring Boot 3.5.14** + **Spring Cloud 2025.0.0** + **Spring Cloud Alibaba 2025.0.0.0**
- Java 17、Maven 多模块
- **Nacos 2.4.2** —— 服务注册发现 + 配置中心
- **Spring Cloud Gateway** —— 反应式网关(WebFlux)
- **OpenFeign + LoadBalancer** —— 服务间声明式调用
- **MyBatis-Plus 3.5.10** + **MySQL 8.0**
- **Redis 7** —— 缓存 / 验证码
- **Hutool / Lombok / Spring Mail**
- **Alipay SDK(沙箱模式)**

### 前端
- **Vue 3.4** + **Vue Router 4**
- **Vite 5** 构建,dev server 端口 `1160`,内置 `/shop/*` 反向代理到各后端服务
- 七牛云 JS SDK(图片上传)

## 🏗️ 系统架构

```
                        ┌─────────────────────┐
                        │  shop-frontend:1160 │  (Vue 3 SPA)
                        └──────────┬──────────┘
                                   │  /shop/*
                            ┌──────▼──────┐
                            │   Gateway   │  7070  Spring Cloud Gateway
                            └──────┬──────┘
                  ┌─────────┬──────┴──────┬─────────┬──────────┐
                  ▼         ▼             ▼         ▼          ▼
            user 7071  coupon 7072   product 7073  order 7074  cart 7075
                  │         │             │         │          │
                  └─────────┴─────┬───────┴─────────┴──────────┘
                                  │
                       ┌──────────┴──────────┐
                       │  Nacos 8848 (注册+配置) │
                       │  MySQL 3306 / Redis 6379 │
                       └─────────────────────┘
                  ┌──────────────────────────┐
                  │   OpenFeign 服务间调用        │
                  │  user ↔ product ↔ coupon     │
                  │  order → product / coupon    │
                  └──────────────────────────┘
```

## 📂 项目结构

```
EasternMarket/
├── docker-compose.yml          # 全栈编排(MySQL/Redis/Nacos + 6 个微服务)
├── Dockerfile                  # 多阶段构建(Maven 编译 → JRE 运行)
├── .dockerignore
├── pom.xml                     # 顶层聚合
├── shop-backend/
│   ├── pom.xml                 # 后端聚合,声明 Spring Boot/Cloud 版本
│   ├── shop-common/            # 公共工具
│   ├── shop-entity/            # 实体 / DTO
│   ├── shop-parent/            # 业务服务聚合
│   │   ├── shop-gateway/       # 网关 7070
│   │   ├── shop-user/          # 用户 7071
│   │   ├── shop-coupon/        # 优惠券 7072
│   │   ├── shop-product/       # 商品 7073
│   │   ├── shop-order/         # 订单 + 支付 7074
│   │   └── shop-cart/          # 购物车 7075
│   └── sql/                     # 建表与种子数据
│       ├── all.sql             # 全量建表 + 初始化
│       ├── user.sql / product.sql / coupon.sql / order.sql / cart.sql
│       ├── seed-product.sql    # 商品种子
│       ├── seed-activity.sql   # 活动种子
│       └── update-product-images.sql
└── shop-frontend/
    ├── package.json
    ├── vite.config.js          # dev 端口 1160 + /shop/* 代理
    └── src/
        ├── router/index.js     # 路由守卫(token 校验)
        └── views/              # Home/Market/Activity/Login/Register/Profile/...
```

## 🚀 快速开始

### 方式一:Docker Compose 一键启动(推荐)

> 需要:Docker Desktop 或 Docker Engine + `docker compose` v2。

```bash
# 1. 启动全部(首次会拉镜像并编译,约 5~10 分钟)
docker compose up -d --build

# 2. 仅启动中间件(自己跑后端 / 前端时使用)
docker compose up -d mysql redis nacos

# 3. 常用命令
docker compose ps            # 查看状态
docker compose logs -f shop-order   # 跟踪某服务日志
docker compose down          # 停止全部
docker compose down -v       # 停止并清空数据卷(重置数据库)
```

启动完成后访问:
- **前端**:http://localhost:1160
- **网关**:http://localhost:7070
- **Nacos 控制台**:http://localhost:8848/nacos(账号 / 密码:`nacos` / `nacos`)
- **MySQL**:`localhost:3306`,初始 root 密码见 `docker-compose.yml` 中 `MYSQL_ROOT_PASSWORD`

### 方式二:本地手动启动(开发联调)

```bash
# 1. 起中间件
docker compose up -d mysql redis nacos

# 2. 后端:在每个服务目录执行,或用 IDE 启动主类
mvn -f shop-backend/pom.xml clean package -DskipTests
java -jar shop-backend/shop-parent/shop-gateway/target/shop-gateway-*.jar
# 同理依次启动 shop-user / shop-coupon / shop-product / shop-order / shop-cart

# 3. 前端
cd shop-frontend
npm install
npm run dev
```

## 🔌 端口与路由

| 服务 | 端口 | 路由前缀(经网关) |
|---|---|---|
| shop-gateway | 7070 | (自身) |
| shop-user | 7071 | `/shop/user/**` |
| shop-coupon | 7072 | `/shop/coupon/**` |
| shop-product | 7073 | `/shop/product/**` |
| shop-order | 7074 | `/shop/order/**` |
| shop-cart | 7075 | `/shop/cart/**` |
| shop-frontend | 1160 | (自身,内部代理 `/shop/*`) |
| MySQL | 3306 | — |
| Redis | 6379 | — |
| Nacos | 8848 / 9848 / 9849 | HTTP + gRPC |

> 网关对所有业务路由使用 `StripPrefix=1`,例如 `GET /shop/user/info` 实际转发到 shop-user 的 `/user/info`。

## 🗄️ 数据库表

`eastern_market` 单库共 9 张表(由 `shop-backend/sql/all.sql` 自动初始化):

| 表名 | 说明 |
|---|---|
| `user` | 用户 |
| `category` | 商品分类 |
| `product` | 商品 |
| `template_coupon` | 优惠券模板 |
| `user_coupon` | 用户已领券 |
| `activity` | 活动 |
| `orders` | 订单主表 |
| `order_item` | 订单明细 |
| `cart_item` | 购物车项 |

种子数据:`seed-product.sql`(商品)、`seed-activity.sql`(活动)、`update-product-images.sql`(图片)会在容器**首次启动**时按文件名前缀顺序执行。

## 💳 支付宝沙箱支付说明

`shop-order` 已集成支付宝沙箱支付,配置位于 [shop-backend/shop-parent/shop-order/src/main/resources/application.yml](shop-backend/shop-parent/shop-order/src/main/resources/application.yml#L29-L41) 的 `alipay.*` 节点,所有密钥通过 `${ALIPAY_*}` 环境变量注入(见「关键环境变量」):

- `app-id` / `private-key` / `public-key` —— 沙箱应用密钥,从 `.env` 读取
- `gateway` —— 沙箱网关 `https://openapi.alipaydev.com/gateway.do`
- `notify-url` —— 异步回调地址,**需公网可访问**(本地开发用内网穿透工具如 cpolar / ngrok / natapp 暴露 7074 端口)
- `return-url` —— 同步跳转,默认回到前端 `http://localhost:1160/pay-result`

> ⚠️ 沙箱密钥请勿直接提交到公开仓库,1.0 版本已全部迁移到 `.env`(见下文「关键环境变量」)。

## ⚙️ 关键环境变量

敏感信息(MySQL 密码、支付宝密钥、QQ 邮箱授权码、七牛云 AK/SK)统一放在项目根目录的 `.env` 文件中(已被 `.gitignore` 忽略,不会提交)。复制 `.env.example` 为 `.env` 并填入真实值即可:

```bash
cp .env.example .env        # Linux/Mac
copy .env.example .env       # Windows
```

容器内服务通过 `docker-compose.yml` 中 `x-backend-env` 锚点统一注入通用变量,各服务再按需追加:

| 变量 | 默认值 | 注入到 | 说明 |
|---|---|---|---|
| `MYSQL_ROOT_PASSWORD` | (无,必填) | mysql 容器 + 全部后端 | MySQL root 密码,后端服务以 `MYSQL_PASSWORD` 形式读取 |
| `MYSQL_HOST` | `mysql` | 全部后端 | MySQL 主机(容器内服务名) |
| `REDIS_HOST` | `redis` | 全部后端 | Redis 主机 |
| `NACOS_ADDR` | `nacos:8848` | 全部后端 | Nacos 地址 |
| `TZ` | `Asia/Shanghai` | 全部后端 | 时区 |
| `JAVA_OPTS` | `-Xms256m -Xmx512m` | 全部后端 | JVM 参数 |
| `AVATAR_DIR` | `/app/avatars` | shop-user | 用户头像存储目录(挂载到卷 `avatars_data`) |
| `ALIPAY_APP_ID` / `ALIPAY_PRIVATE_KEY` / `ALIPAY_PUBLIC_KEY` | (空) | shop-order | 支付宝沙箱密钥 |
| `ALIPAY_NOTIFY_URL` / `ALIPAY_RETURN_URL` | (空) / `http://localhost:1160/pay-result` | shop-order | 支付宝回调地址 |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | (空) | shop-user | QQ 邮箱 + 授权码(发验证码) |
| `QINIU_AK` / `QINIU_SK` / `QINIU_BUCKET` / `QINIU_DOMAIN` | (空) / `bukashop11` / 七牛域名 | shop-user / shop-product | 七牛云对象存储 |

## 🔮 项目待完善的部分

1.0 版本作为可部署到 GitHub 的初始版本,以下功能已具备但仍有改进空间,后续版本会逐步完善:

- [x] **敏感信息外置** ✅(1.0 已完成)—— MySQL 密码、支付宝密钥、QQ 邮箱授权码、七牛云 AK/SK 全部迁移到 `.env`,源码中再无硬编码
- [ ] **前端容器化**:在 `docker-compose.yml` 新增 `shop-frontend` 服务,用 nginx 承载 `dist/` 并反代 `/shop/*` 到网关 7070
- [ ] **网关鉴权**:在 shop-gateway 添加全局 JWT 过滤器,统一校验 `Authorization` 头,目前鉴权放在前端路由守卫
- [ ] **配置中心**:已引入 Nacos 但配置仍以 `application.yml` 为主,后续把环境差异配置迁入 Nacos 配置中心
- [ ] **单元测试**:基本无测试覆盖,优先为 shop-order 支付回调和 shop-cart 核心逻辑补 JUnit 测试
- [ ] **CI/CD**:加 GitHub Actions 做 `mvn package` + `npm run build` 构建校验,支持自动构建 Docker 镜像
- [ ] **API 文档**:接入 springdoc-openapi 自动生成 Swagger,方便前端联调
- [ ] **微服务边界**:当前 `eastern_market` 单库多服务共表,微服务边界不清晰,后续按业务拆分独立 schema
- [ ] **支付生产化**:支付宝 `notify-url` 目前依赖 natapp 免费内网穿透,会过期;上线需固定域名或切换生产配置
- [ ] **日志与监控**:补全 Logback 配置、接入 SkyWalking / Prometheus 监控告警

## 📄 License

仅作学习交流用途。

---

**版本**:v1.0.0(首发版本,部署于 GitHub)
