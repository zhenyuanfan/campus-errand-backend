package com.yuan.campuserrandbackend.model.dto.feedback;

import com.yuan.campuserrandbackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 反馈查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FeedbackQueryRequest extends PageRequest implements Serializable {

    /**
     * 反馈类型
     */
    private String type;

    /**
     * 处理状态
     */
    private String status;

    /**
     * 关键字搜索（模糊匹配标题和内容）
     */
    private String keyword;
}
