package com.sakanal.common.exception;

public enum BizCodeEnum {
    UNKNOWN_EXCEPTION(10000,"系统未知异常"),
    VALID_EXCEPTION(10001,"参数格式校验失败"),
    SMS_CODE_EXCEPTION(10002,"验证码获取频率过高，请稍后再试"),
    PRODUCT_UP_EXCEPTION(11000, "商品上架异常"),
    TO_MANY_REQUEST(10003,"请求流量过大，请稍后再试"),
    USER_EXISTS_EXCEPTION(15001,"用户名已存在"),
    PHONE_EXISTS_EXCEPTION(15002,"手机号已存在"),
    NO_STOCK_EXCEPTION(21000,"商品库存不足"),
    LOGIN_ACCOUNT_PASSWORD_INVALID(15003,"用户名或密码错误");

    private final int code;
    private final String msg;
    BizCodeEnum(int code,String msg){

        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}

