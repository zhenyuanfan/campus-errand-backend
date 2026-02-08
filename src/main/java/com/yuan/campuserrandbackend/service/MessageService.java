package com.yuan.campuserrandbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuan.campuserrandbackend.model.entity.Message;

public interface MessageService extends IService<Message> {

    void sendToUser(Long userId, String title, String content, String type);
}
