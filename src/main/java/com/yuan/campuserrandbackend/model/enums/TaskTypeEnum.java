package com.yuan.campuserrandbackend.model.enums;

import lombok.Getter;

/**
 * 任务类型枚举
 */
@Getter
public enum TaskTypeEnum {

    FILE_DELIVERY("file_delivery", "文件传递"),
    GOODS_PURCHASE("goods_purchase", "物品采购"),
    FOOD_DELIVERY("food_delivery", "餐饮配送"),
    OTHER("other", "其他");

    private final String value;
    private final String text;

    TaskTypeEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }

    /**
     * 根据 value 获取枚举
     */
    public static TaskTypeEnum getEnumByValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        for (TaskTypeEnum taskTypeEnum : TaskTypeEnum.values()) {
            if (taskTypeEnum.value.equals(value)) {
                return taskTypeEnum;
            }
        }
        return null;
    }
}
