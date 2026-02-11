package com.yuan.campuserrandbackend.model.enums;

import lombok.Getter;

/**
 * 配送状态枚举
 */
@Getter
public enum DeliveryStatusEnum {

    PICKED_UP("picked_up", "已取件"),
    DELIVERING("delivering", "配送中"),
    ARRIVED("arrived", "已到达"),
    COMPLETED("completed", "已完成");

    private final String value;
    private final String text;

    DeliveryStatusEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }

    /**
     * 根据 value 获取枚举
     */
    public static DeliveryStatusEnum getEnumByValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        for (DeliveryStatusEnum deliveryStatusEnum : DeliveryStatusEnum.values()) {
            if (deliveryStatusEnum.value.equals(value)) {
                return deliveryStatusEnum;
            }
        }
        return null;
    }
}
