package com.yuan.campuserrandbackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户信息 VO（管理员视角）
 */
@Data
public class UserVO implements Serializable {

    private Long id;

    private String userAccount;

    private String userName;

    private String userAvatar;

    private String userProfile;

    private String userRole;

    private String contactInfo;

    private Double creditScore;

    private BigDecimal balance;

    private Integer orderCount;

    private Date createTime;

    private Date updateTime;

    private static final long serialVersionUID = 1L;
}
