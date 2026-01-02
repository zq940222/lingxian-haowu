-- ====================================
-- 铃鲜好物 - 小区配送功能数据库表
-- 执行此脚本创建小区相关表
-- ====================================

-- ====================================
-- 小区表
-- ====================================
DROP TABLE IF EXISTS t_merchant_community CASCADE;
DROP TABLE IF EXISTS t_community CASCADE;

CREATE TABLE t_community (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    province VARCHAR(32),
    city VARCHAR(32),
    district VARCHAR(32),
    address VARCHAR(256),
    longitude DECIMAL(10,6),
    latitude DECIMAL(10,6),
    status SMALLINT DEFAULT 1,
    sort INTEGER DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

COMMENT ON TABLE t_community IS '小区表';
COMMENT ON COLUMN t_community.name IS '小区名称';
COMMENT ON COLUMN t_community.province IS '省份';
COMMENT ON COLUMN t_community.city IS '城市';
COMMENT ON COLUMN t_community.district IS '区县';
COMMENT ON COLUMN t_community.address IS '详细地址';
COMMENT ON COLUMN t_community.longitude IS '经度';
COMMENT ON COLUMN t_community.latitude IS '纬度';
COMMENT ON COLUMN t_community.status IS '状态 0-禁用 1-启用';
COMMENT ON COLUMN t_community.sort IS '排序';

CREATE INDEX idx_community_status ON t_community(status);
CREATE INDEX idx_community_city ON t_community(city);

-- ====================================
-- 商户配送小区关联表
-- ====================================
CREATE TABLE t_merchant_community (
    id BIGSERIAL PRIMARY KEY,
    merchant_id BIGINT NOT NULL,
    community_id BIGINT NOT NULL,
    enabled SMALLINT DEFAULT 1,
    sort INTEGER DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

COMMENT ON TABLE t_merchant_community IS '商户配送小区关联表';
COMMENT ON COLUMN t_merchant_community.merchant_id IS '商户ID';
COMMENT ON COLUMN t_merchant_community.community_id IS '小区ID';
COMMENT ON COLUMN t_merchant_community.enabled IS '是否开放配送 0-关闭 1-开放';
COMMENT ON COLUMN t_merchant_community.sort IS '排序';

CREATE INDEX idx_merchant_community_merchant ON t_merchant_community(merchant_id);
CREATE INDEX idx_merchant_community_community ON t_merchant_community(community_id);
CREATE INDEX idx_merchant_community_enabled ON t_merchant_community(enabled);
CREATE UNIQUE INDEX idx_merchant_community_unique ON t_merchant_community(merchant_id, community_id) WHERE deleted = 0;

-- ====================================
-- 插入测试小区数据
-- ====================================
INSERT INTO t_community (name, province, city, district, address, status, sort) VALUES
('阳光花园', '浙江省', '杭州市', '西湖区', '文三路100号', 1, 1),
('翠苑小区', '浙江省', '杭州市', '西湖区', '翠苑街道', 1, 2),
('城西银泰城', '浙江省', '杭州市', '西湖区', '丰潭路380号', 1, 3),
('西溪花园', '浙江省', '杭州市', '西湖区', '西溪路500号', 1, 4),
('嘉绿苑', '浙江省', '杭州市', '西湖区', '嘉绿北路', 1, 5);

-- ====================================
-- 插入测试商户配送小区数据（假设商户ID=1存在）
-- ====================================
INSERT INTO t_merchant_community (merchant_id, community_id, enabled, sort) VALUES
(1, 1, 1, 1),
(1, 2, 1, 2),
(1, 3, 0, 3),
(1, 4, 1, 4),
(1, 5, 0, 5);

-- 完成
SELECT '小区相关表创建完成！' AS message;
