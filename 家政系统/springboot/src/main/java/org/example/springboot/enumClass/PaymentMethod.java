package org.example.springboot.enumClass;

import lombok.Getter;

/**
 * 支付方式枚举
 */
@Getter
public enum PaymentMethod {
    WECHAT("WECHAT", "微信支付"),
    ALIPAY("ALIPAY", "支付宝"),
    BALANCE("BALANCE", "余额支付");

    private final String code;
    private final String desc;

    PaymentMethod(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PaymentMethod getByCode(String code) {
        for (PaymentMethod method : PaymentMethod.values()) {
            if (method.getCode().equals(code)) {
                return method;
            }
        }
        return null;
    }
}
