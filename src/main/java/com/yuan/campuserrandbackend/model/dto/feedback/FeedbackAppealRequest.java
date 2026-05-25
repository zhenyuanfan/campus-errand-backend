package com.yuan.campuserrandbackend.model.dto.feedback;

import lombok.Data;

import java.io.Serializable;

/**
 * 接单员申诉请求
 */
@Data
public class FeedbackAppealRequest implements Serializable {

    /**
     * 反馈id
     */
    private Long feedbackId;

    /**
     * 申诉内容
     */
    private String runnerAppeal;
}
