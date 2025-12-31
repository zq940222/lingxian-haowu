# 铃鲜好物 - 部署指南

## 环境要求

- Docker 20.10+
- Docker Compose 2.0+
- 服务器配置: 2核4G以上

## 开发环境部署

开发环境只启动基础设施服务（数据库、缓存、存储），后端服务在本地运行。

### 1. 启动基础设施

```bash
# 启动所有开发环境服务
docker-compose -f docker-compose.dev.yml up -d

# 查看服务状态
docker-compose -f docker-compose.dev.yml ps
```

### 2. 服务访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| PostgreSQL | localhost:5432 | 数据库 |
| Redis | localhost:6379 | 缓存 |
| MinIO API | localhost:9000 | 对象存储API |
| MinIO Console | localhost:9001 | MinIO管理界面 |
| Adminer | localhost:8081 | 数据库管理 |
| Redis Commander | localhost:8082 | Redis管理 |

### 3. 默认账号密码

| 服务 | 用户名 | 密码 |
|------|--------|------|
| PostgreSQL | postgres | postgres123 |
| MinIO | minioadmin | minioadmin123 |

### 4. 本地运行后端

```bash
cd lingxian-server
mvn spring-boot:run
```

### 5. 停止服务

```bash
docker-compose -f docker-compose.dev.yml down

# 停止并删除数据卷（清空数据）
docker-compose -f docker-compose.dev.yml down -v
```

---

## 生产环境部署

### 1. 配置环境变量

```bash
# 复制环境变量示例文件
cp .env.example .env

# 编辑环境变量
vim .env
```

**必须配置的环境变量：**

```bash
# 数据库密码（必填）
DB_PASSWORD=your_strong_password

# MinIO密码（必填）
MINIO_SECRET_KEY=your_minio_password

# JWT密钥（必填，至少32位）
JWT_SECRET=your_jwt_secret_key_at_least_32_characters

# 微信小程序配置
WX_USER_APPID=your_appid
WX_USER_SECRET=your_secret
WX_MERCHANT_APPID=your_merchant_appid
WX_MERCHANT_SECRET=your_merchant_secret
```

### 2. 构建前端

```bash
# 构建后台管理系统
cd lingxian-admin
npm install
npm run build
```

### 3. 启动所有服务

```bash
# 构建并启动
docker-compose up -d --build

# 查看日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f lingxian-server
```

### 4. 服务访问

| 服务 | 地址 |
|------|------|
| 后台管理系统 | http://your-domain/ |
| API接口 | http://your-domain/api/ |
| API文档 | http://your-domain/doc.html |

### 5. 常用运维命令

```bash
# 重启特定服务
docker-compose restart lingxian-server

# 查看服务状态
docker-compose ps

# 进入容器
docker exec -it lingxian-server sh

# 查看资源使用
docker stats

# 停止所有服务
docker-compose down
```

---

## HTTPS配置

### 1. 获取SSL证书

推荐使用 Let's Encrypt 免费证书：

```bash
# 安装certbot
apt install certbot

# 获取证书
certbot certonly --standalone -d your-domain.com
```

### 2. 配置证书

```bash
# 复制证书到部署目录
cp /etc/letsencrypt/live/your-domain.com/fullchain.pem deploy/ssl/
cp /etc/letsencrypt/live/your-domain.com/privkey.pem deploy/ssl/
```

### 3. 启用HTTPS

编辑 `deploy/nginx/conf.d/default.conf`，取消HTTPS server块的注释。

---

## 数据备份

### PostgreSQL备份

```bash
# 备份数据库
docker exec lingxian-postgres pg_dump -U postgres lingxian_db > backup_$(date +%Y%m%d).sql

# 恢复数据库
docker exec -i lingxian-postgres psql -U postgres lingxian_db < backup_20240101.sql
```

### Redis备份

```bash
# 触发RDB快照
docker exec lingxian-redis redis-cli -a your_password BGSAVE

# 复制RDB文件
docker cp lingxian-redis:/data/dump.rdb ./backup/
```

### MinIO备份

```bash
# 使用mc客户端备份
mc alias set myminio http://localhost:9000 minioadmin your_password
mc mirror myminio/lingxian ./backup/minio/
```

---

## 监控建议

### 健康检查端点

- 后端API: `GET /api/actuator/health`
- Nginx: `GET /health`

### 日志位置

| 服务 | 容器内路径 |
|------|-----------|
| Nginx | /var/log/nginx/ |
| 后端服务 | /app/logs/ |

### 推荐监控工具

- **Prometheus + Grafana**: 指标监控
- **ELK Stack**: 日志收集分析
- **Uptime Kuma**: 可用性监控

---

## 常见问题

### Q: 后端服务启动失败？

检查数据库连接：
```bash
docker logs lingxian-server
docker exec -it lingxian-postgres psql -U postgres -c "\l"
```

### Q: 上传文件失败？

检查MinIO服务：
```bash
docker logs lingxian-minio
# 访问 http://localhost:9001 检查bucket是否创建
```

### Q: 微信支付回调失败？

确保：
1. 服务器有公网IP
2. 域名已备案
3. 回调URL配置正确
4. 防火墙已开放443端口

---

## 架构图

```
                    ┌─────────────┐
                    │   用户/商户  │
                    │   小程序    │
                    └──────┬──────┘
                           │
                    ┌──────▼──────┐
                    │    Nginx    │ :80/:443
                    │  (反向代理)  │
                    └──────┬──────┘
           ┌───────────────┼───────────────┐
           │               │               │
    ┌──────▼──────┐ ┌──────▼──────┐ ┌──────▼──────┐
    │  后台管理    │ │   API服务   │ │  静态文件   │
    │  (静态文件)  │ │(Spring Boot)│ │  (MinIO)   │
    └─────────────┘ └──────┬──────┘ └─────────────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
       ┌──────▼──────┐ ┌───▼───┐ ┌──────▼──────┐
       │ PostgreSQL  │ │ Redis │ │    MinIO    │
       │   (数据库)   │ │(缓存) │ │  (对象存储) │
       └─────────────┘ └───────┘ └─────────────┘
```
