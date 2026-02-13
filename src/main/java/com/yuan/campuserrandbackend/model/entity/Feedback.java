package com.yuan.campuserrandbackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户反馈
 *
 * @TableName feedback
 */
@TableName(value = "feedback")
@Data
public class Feedback implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 提交用户id
     */
    private Long userId;

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

    /**
     * 处理状态：pending-待处理/processing-处理中/resolved-已解决/rejected-已驳回
     */
    private String status;

    /**
     * 处理人id（管理员）
     */
    private Long adminId;

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

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除（逻辑删除）
     */
    @TableLogic
    private Integer isDelete;
}
