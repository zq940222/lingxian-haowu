-- ====================================
-- 铃鲜好物 - 数据库表结构设计
-- 数据库: PostgreSQL
-- 创建时间: 2024
-- ====================================

-- 创建数据库
-- CREATE DATABASE lingxian_db WITH ENCODING 'UTF8';

-- ====================================
-- 1. 用户相关表
-- ====================================

-- 用户表
CREATE TABLE t_user (
    id BIGSERIAL PRIMARY KEY,
    openid VARCHAR(64) NOT NULL UNIQUE COMMENT '微信openid',
    unionid VARCHAR(64) COMMENT '微信unionid',
    phone VARCHAR(20) COMMENT '手机号',
    nickname VARCHAR(64) COMMENT '昵称',
    avatar VARCHAR(512) COMMENT '头像URL',
    gender SMALLINT DEFAULT 0 COMMENT '性别 0-未知 1-男 2-女',
    points INTEGER DEFAULT 0 COMMENT '积分',
    balance DECIMAL(10,2) DEFAULT 0.00 COMMENT '余额',
    invite_code VARCHAR(16) UNIQUE COMMENT '邀请码',
    inviter_id BIGINT COMMENT '邀请人ID',
    status SMALLINT DEFAULT 1 COMMENT '状态 0-禁用 1-正常',
    last_login_time TIMESTAMP COMMENT '最后登录时间',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

COMMENT ON TABLE t_user IS '用户表';
CREATE INDEX idx_user_phone ON t_user(phone);
CREATE INDEX idx_user_inviter ON t_user(inviter_id);

-- 用户收货地址表
CREATE TABLE t_user_address (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    name VARCHAR(32) NOT NULL COMMENT '收货人姓名',
    phone VARCHAR(20) NOT NULL COMMENT '收货人电话',
    province VARCHAR(32) COMMENT '省份',
    city VARCHAR(32) COMMENT '城市',
    district VARCHAR(32) COMMENT '区县',
    address VARCHAR(256) NOT NULL COMMENT '详细地址',
    longitude DECIMAL(10,6) COMMENT '经度',
    latitude DECIMAL(10,6) COMMENT '纬度',
    is_default SMALLINT DEFAULT 0 COMMENT '是否默认 0-否 1-是',
    tag VARCHAR(16) COMMENT '标签',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

COMMENT ON TABLE t_user_address IS '用户收货地址表';
CREATE INDEX idx_address_user ON t_user_address(user_id);

-- ====================================
-- 2. 商户相关表
-- ====================================

-- 商户表
CREATE TABLE t_merchant (
    id BIGSERIAL PRIMARY KEY,
    openid VARCHAR(64) COMMENT '微信openid',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    password VARCHAR(128) NOT NULL COMMENT '密码',
    shop_name VARCHAR(64) NOT NULL COMMENT '店铺名称',
    shop_logo VARCHAR(512) COMMENT '店铺logo',
    shop_desc VARCHAR(512) COMMENT '店铺描述',
    contact_name VARCHAR(32) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    license_no VARCHAR(64) COMMENT '营业执照号',
    license_image VARCHAR(512) COMMENT '营业执照图片',
    province VARCHAR(32) COMMENT '省份',
    city VARCHAR(32) COMMENT '城市',
    district VARCHAR(32) COMMENT '区县',
    address VARCHAR(256) COMMENT '详细地址',
    longitude DECIMAL(10,6) COMMENT '经度',
    latitude DECIMAL(10,6) COMMENT '纬度',
    delivery_range INTEGER DEFAULT 5 COMMENT '配送范围(公里)',
    delivery_fee DECIMAL(10,2) DEFAULT 3.00 COMMENT '配送费',
    free_delivery_amount DECIMAL(10,2) DEFAULT 30.00 COMMENT '免配送费金额',
    open_time VARCHAR(10) DEFAULT '08:00' COMMENT '营业开始时间',
    close_time VARCHAR(10) DEFAULT '22:00' COMMENT '营业结束时间',
    shop_status SMALLINT DEFAULT 1 COMMENT '店铺状态 0-休息 1-营业',
    verify_status SMALLINT DEFAULT 0 COMMENT '审核状态 0-待审核 1-已通过 2-已拒绝',
    reject_reason VARCHAR(256) COMMENT '审核拒绝原因',
    status SMALLINT DEFAULT 1 COMMENT '账户状态 0-禁用 1-正常',
    rating DECIMAL(2,1) DEFAULT 5.0 COMMENT '评分',
    monthly_sales INTEGER DEFAULT 0 COMMENT '月销量',
    balance DECIMAL(12,2) DEFAULT 0.00 COMMENT '账户余额',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

COMMENT ON TABLE t_merchant IS '商户表';
CREATE INDEX idx_merchant_phone ON t_merchant(phone);
CREATE INDEX idx_merchant_location ON t_merchant(longitude, latitude);

-- ====================================
-- 3. 商品相关表
-- ====================================

-- 商品分类表
CREATE TABLE t_category (
    id BIGSERIAL PRIMARY KEY,
    merchant_id BIGINT COMMENT '商户ID(null为平台分类)',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    name VARCHAR(32) NOT NULL COMMENT '分类名称',
    icon VARCHAR(512) COMMENT '分类图标',
    sort INTEGER DEFAULT 0 COMMENT '排序',
    level SMALLINT DEFAULT 1 COMMENT '层级',
    status SMALLINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

COMMENT ON TABLE t_category IS '商品分类表';
CREATE INDEX idx_category_merchant ON t_category(merchant_id);
CREATE INDEX idx_category_parent ON t_category(parent_id);

-- 商品表
CREATE TABLE t_product (
    id BIGSERIAL PRIMARY KEY,
    merchant_id BIGINT NOT NULL COMMENT '商户ID',
    category_id BIGINT COMMENT '分类ID',
    name VARCHAR(128) NOT NULL COMMENT '商品名称',
    subtitle VARCHAR(256) COMMENT '商品副标题',
    main_image VARCHAR(512) COMMENT '商品主图',
    images TEXT COMMENT '商品图片(JSON数组)',
    description TEXT COMMENT '商品描述',
    original_price DECIMAL(10,2) COMMENT '原价',
    price DECIMAL(10,2) NOT NULL COMMENT '现价',
    group_price DECIMAL(10,2) COMMENT '拼团价',
    cost_price DECIMAL(10,2) COMMENT '成本价',
    stock INTEGER DEFAULT 0 COMMENT '库存',
    sales INTEGER DEFAULT 0 COMMENT '已售数量',
    unit VARCHAR(16) DEFAULT '份' COMMENT '单位',
    spec VARCHAR(64) COMMENT '规格',
    weight INTEGER COMMENT '重量(克)',
    limit_count INTEGER DEFAULT 0 COMMENT '限购数量(0不限购)',
    sort INTEGER DEFAULT 0 COMMENT '排序',
    group_enabled SMALLINT DEFAULT 0 COMMENT '是否支持拼团 0-否 1-是',
    group_count INTEGER DEFAULT 2 COMMENT '拼团人数',
    status SMALLINT DEFAULT 1 COMMENT '状态 0-下架 1-上架',
    recommended SMALLINT DEFAULT 0 COMMENT '是否推荐 0-否 1-是',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

COMMENT ON TABLE t_product IS '商品表';
CREATE INDEX idx_product_merchant ON t_product(merchant_id);
CREATE INDEX idx_product_category ON t_product(category_id);
CREATE INDEX idx_product_status ON t_product(status);

-- ====================================
-- 4. 订单相关表
-- ====================================

-- 订单表
CREATE TABLE t_order (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    merchant_id BIGINT NOT NULL COMMENT '商户ID',
    group_activity_id BIGINT COMMENT '拼团活动ID',
    group_record_id BIGINT COMMENT '拼团记录ID',
    order_type SMALLINT DEFAULT 1 COMMENT '订单类型 1-普通 2-拼团',
    status SMALLINT DEFAULT 0 COMMENT '状态 0-待支付 1-已支付 2-配送中 3-已完成 4-已取消 5-已退款',
    product_amount DECIMAL(10,2) NOT NULL COMMENT '商品总金额',
    delivery_fee DECIMAL(10,2) DEFAULT 0.00 COMMENT '配送费',
    discount_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '优惠金额',
    points_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '积分抵扣金额',
    used_points INTEGER DEFAULT 0 COMMENT '使用的积分',
    pay_amount DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    pay_type SMALLINT COMMENT '支付方式 1-微信 2-余额',
    pay_time TIMESTAMP COMMENT '支付时间',
    transaction_id VARCHAR(64) COMMENT '微信支付单号',
    receiver_name VARCHAR(32) NOT NULL COMMENT '收货人姓名',
    receiver_phone VARCHAR(20) NOT NULL COMMENT '收货人电话',
    receiver_address VARCHAR(256) NOT NULL COMMENT '收货地址',
    longitude DECIMAL(10,6) COMMENT '经度',
    latitude DECIMAL(10,6) COMMENT '纬度',
    delivery_time TIMESTAMP COMMENT '预约配送时间',
    delivery_user_id BIGINT COMMENT '配送员ID',
    actual_delivery_time TIMESTAMP COMMENT '实际配送时间',
    finish_time TIMESTAMP COMMENT '完成时间',
    cancel_time TIMESTAMP COMMENT '取消时间',
    cancel_reason VARCHAR(256) COMMENT '取消原因',
    remark VARCHAR(256) COMMENT '订单备注',
    earned_points INTEGER DEFAULT 0 COMMENT '获得积分',
    commented SMALLINT DEFAULT 0 COMMENT '是否已评价 0-否 1-是',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

COMMENT ON TABLE t_order IS '订单表';
CREATE INDEX idx_order_user ON t_order(user_id);
CREATE INDEX idx_order_merchant ON t_order(merchant_id);
CREATE INDEX idx_order_status ON t_order(status);
CREATE INDEX idx_order_create_time ON t_order(create_time);

-- 订单商品项表
CREATE TABLE t_order_item (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(128) NOT NULL COMMENT '商品名称',
    product_image VARCHAR(512) COMMENT '商品图片',
    product_spec VARCHAR(64) COMMENT '商品规格',
    price DECIMAL(10,2) NOT NULL COMMENT '商品单价',
    quantity INTEGER NOT NULL COMMENT '购买数量',
    subtotal DECIMAL(10,2) NOT NULL COMMENT '小计金额',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

COMMENT ON TABLE t_order_item IS '订单商品项表';
CREATE INDEX idx_order_item_order ON t_order_item(order_id);

-- ====================================
-- 5. 拼团相关表
-- ====================================

-- 拼团活动表
CREATE TABLE t_group_activity (
    id BIGSERIAL PRIMARY KEY,
    merchant_id BIGINT NOT NULL COMMENT '商户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    name VARCHAR(128) NOT NULL COMMENT '活动名称',
    group_price DECIMAL(10,2) NOT NULL COMMENT '拼团价格',
    group_count INTEGER DEFAULT 2 COMMENT '成团人数',
    limit_count INTEGER DEFAULT 0 COMMENT '限购数量',
    start_time TIMESTAMP NOT NULL COMMENT '开始时间',
    end_time TIMESTAMP NOT NULL COMMENT '结束时间',
    expire_hours INTEGER DEFAULT 24 COMMENT '拼团有效时间(小时)',
    status SMALLINT DEFAULT 0 COMMENT '状态 0-未开始 1-进行中 2-已结束',
    success_count INTEGER DEFAULT 0 COMMENT '已成团数',
    participant_count INTEGER DEFAULT 0 COMMENT '参与人数',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

COMMENT ON TABLE t_group_activity IS '拼团活动表';
CREATE INDEX idx_group_activity_merchant ON t_group_activity(merchant_id);
CREATE INDEX idx_group_activity_product ON t_group_activity(product_id);
CREATE INDEX idx_group_activity_status ON t_group_activity(status);

-- 拼团记录表
CREATE TABLE t_group_record (
    id BIGSERIAL PRIMARY KEY,
    activity_id BIGINT NOT NULL COMMENT '拼团活动ID',
    leader_user_id BIGINT NOT NULL COMMENT '团长用户ID',
    group_count INTEGER NOT NULL COMMENT '成团人数',
    current_count INTEGER DEFAULT 1 COMMENT '当前参团人数',
    status SMALLINT DEFAULT 0 COMMENT '状态 0-拼团中 1-成功 2-失败',
    expire_time TIMESTAMP NOT NULL COMMENT '过期时间',
    success_time TIMESTAMP COMMENT '成团时间',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

COMMENT ON TABLE t_group_record IS '拼团记录表';
CREATE INDEX idx_group_record_activity ON t_group_record(activity_id);
CREATE INDEX idx_group_record_leader ON t_group_record(leader_user_id);
CREATE INDEX idx_group_record_status ON t_group_record(status);

-- 拼团参与者表
CREATE TABLE t_group_participant (
    id BIGSERIAL PRIMARY KEY,
    record_id BIGINT NOT NULL COMMENT '拼团记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    order_id BIGINT COMMENT '订单ID',
    is_leader SMALLINT DEFAULT 0 COMMENT '是否团长 0-否 1-是',
    status SMALLINT DEFAULT 0 COMMENT '状态 0-待支付 1-已参团 2-已退出',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

COMMENT ON TABLE t_group_participant IS '拼团参与者表';
CREATE INDEX idx_group_participant_record ON t_group_participant(record_id);
CREATE INDEX idx_group_participant_user ON t_group_participant(user_id);

-- ====================================
-- 6. 积分相关表
-- ====================================

-- 积分记录表
CREATE TABLE t_points_record (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    type SMALLINT NOT NULL COMMENT '类型 1-签到 2-消费获取 3-消费抵扣 4-邀请奖励 5-退款返还 6-活动奖励 7-兑换商品',
    points INTEGER NOT NULL COMMENT '积分变化值',
    after_points INTEGER NOT NULL COMMENT '变化后积分',
    order_id BIGINT COMMENT '关联订单ID',
    description VARCHAR(256) COMMENT '描述',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

COMMENT ON TABLE t_points_record IS '积分记录表';
CREATE INDEX idx_points_record_user ON t_points_record(user_id);
CREATE INDEX idx_points_record_type ON t_points_record(type);

-- ====================================
-- 7. 购物车表
-- ====================================

CREATE TABLE t_cart (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    merchant_id BIGINT NOT NULL COMMENT '商户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INTEGER DEFAULT 1 COMMENT '数量',
    price DECIMAL(10,2) COMMENT '加入时价格',
    selected SMALLINT DEFAULT 1 COMMENT '是否选中 0-否 1-是',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

COMMENT ON TABLE t_cart IS '购物车表';
CREATE INDEX idx_cart_user ON t_cart(user_id);
CREATE UNIQUE INDEX idx_cart_user_product ON t_cart(user_id, product_id) WHERE deleted = 0;

-- ====================================
-- 8. 管理员相关表
-- ====================================

-- 管理员用户表
CREATE TABLE t_admin_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(32) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(128) NOT NULL COMMENT '密码',
    real_name VARCHAR(32) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(64) COMMENT '邮箱',
    avatar VARCHAR(512) COMMENT '头像',
    role_id BIGINT COMMENT '角色ID',
    status SMALLINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    last_login_time TIMESTAMP COMMENT '最后登录时间',
    last_login_ip VARCHAR(64) COMMENT '最后登录IP',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

COMMENT ON TABLE t_admin_user IS '管理员用户表';

-- 角色表
CREATE TABLE t_role (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(32) NOT NULL COMMENT '角色名称',
    code VARCHAR(32) NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(256) COMMENT '描述',
    permissions TEXT COMMENT '权限列表(JSON)',
    status SMALLINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

COMMENT ON TABLE t_role IS '角色表';

-- ====================================
-- 9. 营销相关表
-- ====================================

-- 轮播图表
CREATE TABLE t_banner (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(64) COMMENT '标题',
    image_url VARCHAR(512) NOT NULL COMMENT '图片URL',
    link_type SMALLINT COMMENT '跳转类型 1-商品 2-商户 3-活动 4-外链',
    link_id BIGINT COMMENT '跳转目标ID',
    link_url VARCHAR(512) COMMENT '跳转URL',
    position SMALLINT DEFAULT 1 COMMENT '位置 1-首页 2-分类页 3-个人中心',
    sort INTEGER DEFAULT 0 COMMENT '排序',
    status SMALLINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

COMMENT ON TABLE t_banner IS '轮播图表';
CREATE INDEX idx_banner_position ON t_banner(position);

-- ====================================
-- 10. 系统配置表
-- ====================================

CREATE TABLE t_system_config (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(64) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    description VARCHAR(256) COMMENT '描述',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_system_config IS '系统配置表';

-- 初始化配置数据
INSERT INTO t_system_config (config_key, config_value, description) VALUES
('points_sign_in_daily', '5', '每日签到积分'),
('points_order_rate', '1', '订单金额积分比例(1元=N积分)'),
('points_invite_reward', '100', '邀请新用户奖励积分'),
('points_exchange_rate', '100', '积分兑换比例(N积分=1元)'),
('delivery_default_fee', '3.00', '默认配送费'),
('delivery_free_amount', '30.00', '免配送费金额'),
('delivery_max_distance', '5', '最大配送距离(公里)'),
('group_expire_hours', '24', '拼团过期时间(小时)'),
('group_max_participants', '10', '最大参团人数');

-- 初始化管理员账号 (密码: admin123)
INSERT INTO t_admin_user (username, password, real_name, role_id, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '超级管理员', 1, 1);

-- 初始化角色
INSERT INTO t_role (name, code, description, permissions, status) VALUES
('超级管理员', 'SUPER_ADMIN', '拥有所有权限', '["*"]', 1),
('运营管理员', 'OPERATOR', '负责日常运营管理', '["user:view","merchant:view","order:view","product:view"]', 1);
