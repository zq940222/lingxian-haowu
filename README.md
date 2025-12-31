# 铃鲜好物

多商户社区买菜小程序平台 - 为周边社区提供网上买菜和预约送货上门服务

## 项目简介

铃鲜好物是一个面向社区的生鲜电商平台，包含三端应用：

- **用户端微信小程序** - 用户浏览商品、下单购买、参与拼团
- **商户端微信小程序** - 商户管理商品、处理订单、配送管理
- **后台管理系统** - 平台运营管理、数据统计

## 技术栈

| 端 | 技术 |
|---|---|
| 后端 | Spring Boot 3.2 + MyBatis Plus + PostgreSQL + Redis |
| 用户小程序 | 微信原生开发 |
| 商户小程序 | 微信原生开发 |
| 后台管理 | Vue3 + Vite + Element Plus |

## 项目结构

```
lingxian-haowu/
├── lingxian-server/          # 后端服务 (Spring Boot)
├── lingxian-user-miniapp/    # 用户端微信小程序
├── lingxian-merchant-miniapp/# 商户端微信小程序
├── lingxian-admin/           # 后台管理系统 (Vue3)
└── docs/                     # 项目文档
```

## 核心功能

### 用户端
- 商品浏览与搜索
- 购物车管理
- 在线下单支付
- 拼团优惠
- 积分签到
- 订单追踪

### 商户端
- 店铺管理
- 商品上下架
- 订单处理
- 配送管理
- 销售统计

### 管理端
- 用户管理
- 商户审核
- 商品管理
- 订单管理
- 营销配置
- 数据统计

## 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- PostgreSQL 15+
- Redis 7+

### 后端启动
```bash
cd lingxian-server
mvn spring-boot:run
```

### 后台管理系统启动
```bash
cd lingxian-admin
npm install
npm run dev
```

### 小程序开发
使用微信开发者工具打开 `lingxian-user-miniapp` 或 `lingxian-merchant-miniapp` 目录

## 文档

- [架构文档](./docs/architecture/README.md)
- [数据库设计](./docs/database/schema.sql)
- [API文档](./docs/api/)

## License

MIT
