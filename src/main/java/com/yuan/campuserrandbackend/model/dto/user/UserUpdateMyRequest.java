package com.yuan.campuserrandbackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserUpdateMyRequest implements Serializable {

    private Long id;

    private String userName;

    private String userProfile;

    private String contactInfo;

    private static final long serialVersionUID = 1L;
}
