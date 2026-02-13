package com.yuan.campuserrandbackend.model.dto.review;

import com.yuan.campuserrandbackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 评价高级查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ReviewQueryRequest extends PageRequest implements Serializable {

    /**
     * 关键字搜索（模糊匹配评价内容）
     */
    private String keyword;

    /**
     * 最低评分
     */
    private Integer minScore;

    /**
     * 最高评分
     */
    private Integer maxScore;

    /**
     * 创建时间范围 - 开始
     */
    private Date startTime;

    /**
     * 创建时间范围 - 结束
     */
    private Date endTime;
}
