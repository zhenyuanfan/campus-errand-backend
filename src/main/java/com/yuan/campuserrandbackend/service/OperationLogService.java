package com.yuan.campuserrandbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuan.campuserrandbackend.model.entity.OperationLog;

public interface OperationLogService extends IService<OperationLog> {

    void addLog(Long userId, String action, String detail);
}
