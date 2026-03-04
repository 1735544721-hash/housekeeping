package org.example.springboot.enumClass;

import lombok.Getter;

/**
 * 退款状态枚举
 */
@Getter
public enum RefundStatus {
    PENDING_AUDIT(1, "待审核"),
    AUDIT_PASSED(2, "审核通过"),
    AUDIT_REJECTED(3, "审核拒绝"),
    REFUNDING(4, "退款中"),
    REFUNDED(5, "已退款"),
    REFUND_FAILED(6, "退款失败");

    private final Integer value;
    private final String desc;

    RefundStatus(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static RefundStatus getByValue(Integer value) {
        for (RefundStatus status : RefundStatus.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
