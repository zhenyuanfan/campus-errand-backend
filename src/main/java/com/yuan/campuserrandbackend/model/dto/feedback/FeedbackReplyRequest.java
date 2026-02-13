package com.yuan.campuserrandbackend.model.dto.feedback;

import lombok.Data;

import java.io.Serializable;

/**
 * 管理员回复反馈请求
 */
@Data
public class FeedbackReplyRequest implements Serializable {

    /**
     * 反馈id
     */
    private Long feedbackId;

    /**
     * 处理状态：processing-处理中/resolved-已解决/rejected-已驳回
     */
    private String status;

    /**
     * 回复内容
     */
    private String adminReply;
}
