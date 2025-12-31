package com.lingxian.common.constant;

/**
 * Redis Key 常量
 */
public interface RedisConstant {

    /**
     * 用户Token前缀
     */
    String USER_TOKEN_PREFIX = "user:token:";

    /**
     * 商户Token前缀
     */
    String MERCHANT_TOKEN_PREFIX = "merchant:token:";

    /**
     * 管理员Token前缀
     */
    String ADMIN_TOKEN_PREFIX = "admin:token:";

    /**
     * 验证码前缀
     */
    String SMS_CODE_PREFIX = "sms:code:";

    /**
     * 用户信息缓存前缀
     */
    String USER_INFO_PREFIX = "user:info:";

    /**
     * 商户信息缓存前缀
     */
    String MERCHANT_INFO_PREFIX = "merchant:info:";

    /**
     * 商品信息缓存前缀
     */
    String PRODUCT_INFO_PREFIX = "product:info:";

    /**
     * 商品库存前缀
     */
    String PRODUCT_STOCK_PREFIX = "product:stock:";

    /**
     * 购物车前缀
     */
    String CART_PREFIX = "cart:";

    /**
     * 拼团信息前缀
     */
    String GROUP_INFO_PREFIX = "group:info:";

    /**
     * 拼团参与者前缀
     */
    String GROUP_PARTICIPANTS_PREFIX = "group:participants:";

    /**
     * 订单超时队列
     */
    String ORDER_TIMEOUT_QUEUE = "order:timeout:queue";

    /**
     * 分布式锁前缀
     */
    String LOCK_PREFIX = "lock:";

    /**
     * 库存锁前缀
     */
    String STOCK_LOCK_PREFIX = "lock:stock:";

    /**
     * 用户签到记录前缀
     */
    String USER_SIGN_PREFIX = "user:sign:";

    /**
     * 热门商品排行
     */
    String HOT_PRODUCT_RANK = "rank:product:hot";

    /**
     * 商户销量排行
     */
    String MERCHANT_SALES_RANK = "rank:merchant:sales";

    /**
     * Token过期时间(秒) - 7天
     */
    long TOKEN_EXPIRE = 7 * 24 * 60 * 60;

    /**
     * 验证码过期时间(秒) - 5分钟
     */
    long SMS_CODE_EXPIRE = 5 * 60;

    /**
     * 用户信息缓存过期时间(秒) - 1小时
     */
    long USER_INFO_EXPIRE = 60 * 60;

    /**
     * 商品信息缓存过期时间(秒) - 30分钟
     */
    long PRODUCT_INFO_EXPIRE = 30 * 60;

    /**
     * 订单超时时间(秒) - 30分钟
     */
    long ORDER_TIMEOUT = 30 * 60;
}
