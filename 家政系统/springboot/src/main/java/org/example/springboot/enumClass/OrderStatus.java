package org.example.springboot.enumClass;

import lombok.Getter;

@Getter
public enum OrderStatus {
    WAITING_PAY(1, "待支付"),
    WAITING_ACCEPT(2, "待接单"),
    ACCEPTED(3, "已接单"),
    IN_SERVICE(4, "服务中"),
    COMPLETED(5, "已完成"),
    CANCELLED(6, "已取消"),
    CLOSED(7, "已关闭");

    private final Integer value;
    private final String desc;

    OrderStatus(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static OrderStatus getByValue(Integer value) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
} 