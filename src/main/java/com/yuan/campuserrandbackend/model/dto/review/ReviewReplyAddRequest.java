package com.yuan.campuserrandbackend.model.dto.review;

import lombok.Data;

import java.io.Serializable;

/**
 * 评价回复请求
 */
@Data
public class ReviewReplyAddRequest implements Serializable {

    /**
     * 评价id
     */
    private Long reviewId;

    /**
     * 回复内容
     */
    private String content;
}
