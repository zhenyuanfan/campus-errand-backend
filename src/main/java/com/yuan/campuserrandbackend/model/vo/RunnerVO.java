package com.yuan.campuserrandbackend.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 跑腿人员视图对象
 */
@Data
public class RunnerVO implements Serializable {

    /**
     * 用户id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 联系方式
     */
    private String contactInfo;

    /**
     * 信誉评分
     */
    private Double creditScore;

    /**
     * 接单数量
     */
    private Integer orderCount;

    /**
     * 平均评分
     */
    private Double avgScore;

    /**
     * 评价数量
     */
    private Integer reviewCount;
}
