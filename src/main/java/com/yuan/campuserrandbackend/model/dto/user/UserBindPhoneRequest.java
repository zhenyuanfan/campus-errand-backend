package com.yuan.campuserrandbackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserBindPhoneRequest implements Serializable {

    private String phone;

    private String code;

    private static final long serialVersionUID = 1L;
}
