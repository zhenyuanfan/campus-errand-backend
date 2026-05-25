package com.yuan.campuserrandbackend.model.enums;

import lombok.Getter;

/**
 * 支付状态枚举
 */
@Getter
public enum PaymentStatusEnum {

    UNPAID("unpaid", "未支付"),
    PAID("paid", "已支付(冻结)"),
    RELEASED("released", "已放款"),
    REFUNDED("refunded", "已退款");

    private final String value;
    private final String text;

    PaymentStatusEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }

    /**
     * 根据 value 获取枚举
     */
    public static PaymentStatusEnum getEnumByValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        for (PaymentStatusEnum paymentStatusEnum : PaymentStatusEnum.values()) {
            if (paymentStatusEnum.value.equals(value)) {
                return paymentStatusEnum;
            }
        }
        return null;
    }
}
