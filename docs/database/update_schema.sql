-- ====================================
-- 铃鲜好物 - 数据库增量更新脚本
-- 用于更新已有数据库表结构和添加测试数据
-- ====================================

-- 修改 t_product 表结构（如果表已存在）
-- 注意: PostgreSQL 中需要使用 ALTER TABLE 来修改表结构

-- 如果需要完全重建，先删除旧表
DROP TABLE IF EXISTS t_banner CASCADE;
DROP TABLE IF EXISTS t_category CASCADE;
DROP TABLE IF EXISTS t_product CASCADE;
DROP TABLE IF EXISTS t_group_activity CASCADE;

-- 重新创建轮播图表
CREATE TABLE t_banner (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(64),
    image_url VARCHAR(512) NOT NULL,
    link_type SMALLINT,
    link_id BIGINT,
    link_url VARCHAR(512),
    position SMALLINT DEFAULT 1,
    sort INTEGER DEFAULT 0,
    status SMALLINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);
CREATE INDEX idx_banner_position ON t_banner(position);

-- 重新创建分类表
CREATE TABLE t_category (
    id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT DEFAULT 0,
    name VARCHAR(32) NOT NULL,
    icon VARCHAR(512),
    image VARCHAR(512),
    level SMALLINT DEFAULT 1,
    sort INTEGER DEFAULT 0,
    status SMALLINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);
CREATE INDEX idx_category_parent ON t_category(parent_id);

-- 重新创建商品表
CREATE TABLE t_product (
    id BIGSERIAL PRIMARY KEY,
    merchant_id BIGINT NOT NULL,
    category_id BIGINT,
    name VARCHAR(128) NOT NULL,
    image VARCHAR(512),
    images TEXT,
    video VARCHAR(512),
    price DECIMAL(10,2) NOT NULL,
    original_price DECIMAL(10,2),
    cost_price DECIMAL(10,2),
    stock INTEGER DEFAULT 0,
    sales_count INTEGER DEFAULT 0,
    unit VARCHAR(16) DEFAULT '份',
    weight INTEGER,
    description VARCHAR(512),
    detail TEXT,
    status SMALLINT DEFAULT 1,
    is_recommend SMALLINT DEFAULT 0,
    is_new SMALLINT DEFAULT 0,
    is_hot SMALLINT DEFAULT 0,
    sort INTEGER DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);
CREATE INDEX idx_product_merchant ON t_product(merchant_id);
CREATE INDEX idx_product_category ON t_product(category_id);
CREATE INDEX idx_product_status ON t_product(status);

-- 重新创建拼团活动表
CREATE TABLE t_group_activity (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    product_id BIGINT NOT NULL,
    merchant_id BIGINT NOT NULL,
    original_price DECIMAL(10,2),
    group_price DECIMAL(10,2) NOT NULL,
    group_size INTEGER DEFAULT 2,
    limit_per_user INTEGER DEFAULT 0,
    stock INTEGER DEFAULT 0,
    sold_count INTEGER DEFAULT 0,
    group_count INTEGER DEFAULT 0,
    expire_hours INTEGER DEFAULT 24,
    auto_cancel SMALLINT DEFAULT 1,
    status SMALLINT DEFAULT 0,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    description VARCHAR(512),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);
CREATE INDEX idx_group_activity_merchant ON t_group_activity(merchant_id);
CREATE INDEX idx_group_activity_product ON t_group_activity(product_id);
CREATE INDEX idx_group_activity_status ON t_group_activity(status);

-- ====================================
-- 插入测试数据
-- ====================================

-- 轮播图测试数据
INSERT INTO t_banner (title, image_url, link_type, link_id, position, sort, status) VALUES
('新鲜蔬菜上市', 'https://picsum.photos/750/300?random=1', 1, 1, 1, 1, 1),
('水果特惠专区', 'https://picsum.photos/750/300?random=2', 1, 2, 1, 2, 1),
('限时拼团活动', 'https://picsum.photos/750/300?random=3', 3, 1, 1, 3, 1);

-- 商品分类测试数据
INSERT INTO t_category (parent_id, name, icon, level, sort, status) VALUES
(0, '蔬菜', 'https://picsum.photos/80/80?random=10', 1, 1, 1),
(0, '水果', 'https://picsum.photos/80/80?random=11', 1, 2, 1),
(0, '肉禽蛋', 'https://picsum.photos/80/80?random=12', 1, 3, 1),
(0, '海鲜水产', 'https://picsum.photos/80/80?random=13', 1, 4, 1),
(0, '乳品烘焙', 'https://picsum.photos/80/80?random=14', 1, 5, 1),
(0, '粮油调味', 'https://picsum.photos/80/80?random=15', 1, 6, 1),
(0, '休闲零食', 'https://picsum.photos/80/80?random=16', 1, 7, 1),
(0, '酒水饮料', 'https://picsum.photos/80/80?random=17', 1, 8, 1);

-- 确保商户表存在且有数据
INSERT INTO t_merchant (phone, password, shop_name, shop_logo, contact_name, contact_phone, province, city, district, address, verify_status, status) VALUES
('13800138001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '新鲜果蔬店', 'https://picsum.photos/200/200?random=20', '张三', '13800138001', '浙江省', '杭州市', '西湖区', '文三路100号', 1, 1)
ON CONFLICT DO NOTHING;

-- 商品测试数据
INSERT INTO t_product (merchant_id, category_id, name, image, images, price, original_price, stock, sales_count, unit, description, detail, status, is_recommend, sort) VALUES
(1, 1, '新鲜有机西红柿', 'https://picsum.photos/400/400?random=101', '["https://picsum.photos/400/400?random=101","https://picsum.photos/400/400?random=102"]', 5.90, 8.90, 100, 520, '500g', '有机种植，新鲜采摘', '<p>优质有机西红柿，无农药残留</p>', 1, 1, 1),
(1, 1, '云南甜脆黄瓜', 'https://picsum.photos/400/400?random=103', '["https://picsum.photos/400/400?random=103"]', 4.50, 6.00, 200, 380, '500g', '清脆爽口，绿色无公害', '<p>云南高原种植，口感清脆</p>', 1, 1, 2),
(1, 2, '红富士苹果', 'https://picsum.photos/400/400?random=104', '["https://picsum.photos/400/400?random=104"]', 12.90, 18.90, 150, 890, '500g', '甜脆多汁，营养丰富', '<p>山东烟台红富士，脆甜可口</p>', 1, 1, 3),
(1, 2, '泰国金枕榴莲', 'https://picsum.photos/400/400?random=105', '["https://picsum.photos/400/400?random=105"]', 89.00, 128.00, 50, 230, '个', '肉厚核小，香甜软糯', '<p>泰国进口，果肉饱满</p>', 1, 1, 4),
(1, 3, '土鸡蛋', 'https://picsum.photos/400/400?random=106', '["https://picsum.photos/400/400?random=106"]', 28.00, 35.00, 300, 1200, '30枚', '散养土鸡蛋，营养健康', '<p>农家散养，蛋黄金黄</p>', 1, 1, 5),
(1, 4, '活冻大虾', 'https://picsum.photos/400/400?random=107', '["https://picsum.photos/400/400?random=107"]', 68.00, 88.00, 80, 450, '500g', '深海捕捞，鲜甜Q弹', '<p>南美白对虾，肉质紧实</p>', 1, 1, 6),
(1, 5, '蒙牛纯牛奶', 'https://picsum.photos/400/400?random=108', '["https://picsum.photos/400/400?random=108"]', 59.90, 69.90, 200, 680, '箱', '纯正奶源，营养美味', '<p>新鲜牧场奶源</p>', 1, 0, 7),
(1, 6, '金龙鱼调和油', 'https://picsum.photos/400/400?random=109', '["https://picsum.photos/400/400?random=109"]', 45.90, 55.90, 100, 320, '桶', '健康食用油，品质保证', '<p>1:1:1健康配比</p>', 1, 0, 8);

-- 拼团活动测试数据
INSERT INTO t_group_activity (name, product_id, merchant_id, original_price, group_price, group_size, stock, status, start_time, end_time, description) VALUES
('红富士苹果拼团', 3, 1, 18.90, 9.90, 3, 100, 1, NOW() - INTERVAL '1 day', NOW() + INTERVAL '7 days', '限时拼团，超低价格'),
('泰国榴莲拼团', 4, 1, 128.00, 69.00, 5, 30, 1, NOW() - INTERVAL '1 day', NOW() + INTERVAL '3 days', '新鲜到货，拼团更优惠'),
('土鸡蛋拼团', 5, 1, 35.00, 19.90, 2, 200, 1, NOW() - INTERVAL '1 day', NOW() + INTERVAL '5 days', '农家散养，限时特价');

-- 完成
SELECT '数据库更新完成！' AS message;
