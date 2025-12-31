package com.lingxian.common.result;

import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
public enum ResultCode {

    // 成功
    SUCCESS(200, "操作成功"),

    // 客户端错误 4xx
    FAILED(400, "操作失败"),
    VALIDATE_FAILED(400, "参数校验失败"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有相关权限"),
    NOT_FOUND(404, "资源不存在"),

    // 服务端错误 5xx
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),

    // 业务错误 1xxx
    USER_NOT_EXIST(1001, "用户不存在"),
    USER_DISABLED(1002, "用户已被禁用"),
    PASSWORD_ERROR(1003, "密码错误"),
    PHONE_EXIST(1004, "手机号已注册"),
    CODE_ERROR(1005, "验证码错误"),
    CODE_EXPIRED(1006, "验证码已过期"),

    // 商户相关 2xxx
    MERCHANT_NOT_EXIST(2001, "商户不存在"),
    MERCHANT_DISABLED(2002, "商户已被禁用"),
    MERCHANT_NOT_VERIFIED(2003, "商户未认证"),
    SHOP_CLOSED(2004, "店铺已打烊"),

    // 商品相关 3xxx
    PRODUCT_NOT_EXIST(3001, "商品不存在"),
    PRODUCT_OFF_SHELF(3002, "商品已下架"),
    STOCK_NOT_ENOUGH(3003, "库存不足"),
    PRODUCT_LIMIT_EXCEED(3004, "超出限购数量"),

    // 订单相关 4xxx
    ORDER_NOT_EXIST(4001, "订单不存在"),
    ORDER_STATUS_ERROR(4002, "订单状态异常"),
    ORDER_CANCELED(4003, "订单已取消"),
    ORDER_PAID(4004, "订单已支付"),
    ORDER_EXPIRED(4005, "订单已过期"),

    // 拼团相关 5xxx
    GROUP_NOT_EXIST(5001, "拼团不存在"),
    GROUP_FULL(5002, "拼团人数已满"),
    GROUP_EXPIRED(5003, "拼团已过期"),
    GROUP_JOINED(5004, "已参与该拼团"),
    GROUP_NOT_START(5005, "拼团未开始"),
    GROUP_ENDED(5006, "拼团已结束"),

    // 支付相关 6xxx
    PAY_FAILED(6001, "支付失败"),
    PAY_AMOUNT_ERROR(6002, "支付金额异常"),
    REFUND_FAILED(6003, "退款失败"),

    // 积分相关 7xxx
    POINTS_NOT_ENOUGH(7001, "积分不足"),
    POINTS_EXCHANGE_FAILED(7002, "积分兑换失败"),

    // 地址相关 8xxx
    ADDRESS_NOT_EXIST(8001, "地址不存在"),
    ADDRESS_OUT_OF_RANGE(8002, "超出配送范围"),

    // 文件相关 9xxx
    FILE_UPLOAD_FAILED(9001, "文件上传失败"),
    FILE_TYPE_NOT_SUPPORT(9002, "不支持的文件类型"),
    FILE_SIZE_EXCEED(9003, "文件大小超出限制");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
