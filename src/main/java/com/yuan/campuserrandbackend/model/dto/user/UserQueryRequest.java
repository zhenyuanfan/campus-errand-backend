package com.yuan.campuserrandbackend.model.dto.user;

import com.yuan.campuserrandbackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户查询请求（管理员用）
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {

    /**
     * 用户昵称（模糊搜索）
     */
    private String userName;

    /**
     * 用户账号（模糊搜索）
     */
    private String userAccount;

    /**
     * 用户角色筛选
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}
