package com.yuan.campuserrandbackend.model.dto.task;

import lombok.Data;

import java.io.Serializable;

/**
 * 接单请求
 */
@Data
public class TaskAcceptRequest implements Serializable {

    /**
     * 任务id
     */
    private Long taskId;
}
