package com.yuan.campuserrandbackend.model.dto.order;

import com.yuan.campuserrandbackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单高级查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OrderQueryRequest extends PageRequest implements Serializable {

    /**
     * 关键字搜索（同时模糊匹配标题和描述）
     */
    private String keyword;

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * 任务状态
     */
    private String status;

    /**
     * 创建时间范围 - 开始
     */
    private Date startTime;

    /**
     * 创建时间范围 - 结束
     */
    private Date endTime;

    /**
     * 最低报酬
     */
    private BigDecimal minReward;

    /**
     * 最高报酬
     */
    private BigDecimal maxReward;
}
