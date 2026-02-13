package com.yuan.campuserrandbackend.model.enums;

import lombok.Getter;

/**
 * 反馈类型枚举
 */
@Getter
public enum FeedbackTypeEnum {

    SUGGESTION("suggestion", "建议"),
    COMPLAINT("complaint", "投诉"),
    BUG("bug", "Bug反馈"),
    OTHER("other", "其他");

    private final String value;
    private final String text;

    FeedbackTypeEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }

    /**
     * 根据 value 获取枚举
     */
    public static FeedbackTypeEnum getEnumByValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        for (FeedbackTypeEnum feedbackTypeEnum : FeedbackTypeEnum.values()) {
            if (feedbackTypeEnum.value.equals(value)) {
                return feedbackTypeEnum;
            }
        }
        return null;
    }
}
