package com.yuan.campuserrandbackend.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserAvatarUploadVO implements Serializable {

    private String url;

    private static final long serialVersionUID = 1L;
}
