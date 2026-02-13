package com.yuan.campuserrandbackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 评价回复视图对象
 */
@Data
public class ReviewReplyVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 评价id
     */
    private Long reviewId;

    /**
     * 回复者id
     */
    private Long replierId;

    /**
     * 回复者昵称
     */
    private String replierName;

    /**
     * 回复者头像
     */
    private String replierAvatar;

    /**
     * 回复内容
     */
    private String content;

    /**
     * 创建时间
     */
    private Date createTime;
}
