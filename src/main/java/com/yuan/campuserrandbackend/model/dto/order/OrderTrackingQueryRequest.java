package com.yuan.campuserrandbackend.model.dto.order;

import com.yuan.campuserrandbackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户查询订单列表请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OrderTrackingQueryRequest extends PageRequest implements Serializable {

    /**
     * 任务状态筛选
     */
    private String status;
}
