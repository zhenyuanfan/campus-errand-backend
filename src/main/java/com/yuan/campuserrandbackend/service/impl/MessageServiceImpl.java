package com.yuan.campuserrandbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuan.campuserrandbackend.mapper.MessageMapper;
import com.yuan.campuserrandbackend.model.entity.Message;
import com.yuan.campuserrandbackend.service.MessageService;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Override
    public void sendToUser(Long userId, String title, String content, String type) {
        Message message = new Message();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setType(type);
        message.setIsRead(0);
        this.save(message);
    }
}
