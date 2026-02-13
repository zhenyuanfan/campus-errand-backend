package com.yuan.campuserrandbackend.model.dto.feedback;

import lombok.Data;

import java.io.Serializable;

/**
 * 提交反馈请求
 */
@Data
public class FeedbackAddRequest implements Serializable {

    /**
     * 反馈类型：suggestion-建议/complaint-投诉/bug-Bug反馈/other-其他
     */
    private String type;

    /**
     * 反馈标题
     */
    private String title;

    /**
     * 反馈内容
     */
    private String content;

    /**
     * 联系方式（可选）
     */
    private String contactInfo;

    /**
     * 关联任务id（可选）
     */
    private Long taskId;
}
