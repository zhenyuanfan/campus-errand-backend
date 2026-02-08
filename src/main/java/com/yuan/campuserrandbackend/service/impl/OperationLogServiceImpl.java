package com.yuan.campuserrandbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuan.campuserrandbackend.mapper.OperationLogMapper;
import com.yuan.campuserrandbackend.model.entity.OperationLog;
import com.yuan.campuserrandbackend.service.OperationLogService;
import org.springframework.stereotype.Service;

@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    @Override
    public void addLog(Long userId, String action, String detail) {
        OperationLog operationLog = new OperationLog();
        operationLog.setUserId(userId);
        operationLog.setAction(action);
        operationLog.setDetail(detail);
        this.save(operationLog);
    }
}
