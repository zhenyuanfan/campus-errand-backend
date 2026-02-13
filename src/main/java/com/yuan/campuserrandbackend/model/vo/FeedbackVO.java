package com.yuan.campuserrandbackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 反馈视图对象
 */
@Data
public class FeedbackVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 提交用户id
     */
    private Long userId;

    /**
     * 提交用户昵称
     */
    private String userName;

    /**
     * 提交用户头像
     */
    private String userAvatar;

    /**
     * 反馈类型
     */
    private String type;

    /**
     * 反馈类型文本
     */
    private String typeText;

    /**
     * 反馈标题
     */
    private String title;

    /**
     * 反馈内容
     */
    private String content;

    /**
     * 联系方式
     */
    private String contactInfo;

    /**
     * 关联任务id
     */
    private Long taskId;

    /**
     * 关联任务标题
     */
    private String taskTitle;

    /**
     * 处理状态
     */
    private String status;

    /**
     * 处理状态文本
     */
    private String statusText;

    /**
     * 处理人id（管理员）
     */
    private Long adminId;

    /**
     * 处理人昵称
     */
    private String adminName;

    /**
     * 管理员回复内容
     */
    private String adminReply;

    /**
     * 回复时间
     */
    private Date replyTime;

    /**
     * 创建时间
     */
    private Date createTime;
}
