package com.yuan.campuserrandbackend.controller;

import com.yuan.campuserrandbackend.common.BaseResponse;
import com.yuan.campuserrandbackend.common.ResultUtils;
import com.yuan.campuserrandbackend.exception.BusinessException;
import com.yuan.campuserrandbackend.exception.ErrorCode;
import com.yuan.campuserrandbackend.model.entity.User;
import com.yuan.campuserrandbackend.model.vo.OverviewStatsVO;
import com.yuan.campuserrandbackend.model.vo.RunnerAnalysisVO;
import com.yuan.campuserrandbackend.model.vo.TaskAnalysisVO;
import com.yuan.campuserrandbackend.model.vo.UserAnalysisVO;
import com.yuan.campuserrandbackend.service.AnalysisService;
import com.yuan.campuserrandbackend.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 数据分析接口（仅管理员可访问）
 */
@RestController
@RequestMapping("/analysis")
public class AnalysisController {

    @Resource
    private AnalysisService analysisService;

    @Resource
    private UserService userService;

    /**
     * 平台概览统计
     */
    @GetMapping("/overview")
    public BaseResponse<OverviewStatsVO> getOverviewStats(HttpServletRequest request) {
        checkAdmin(request);
        return ResultUtils.success(analysisService.getOverviewStats());
    }

    /**
     * 任务分析数据
     */
    @GetMapping("/task")
    public BaseResponse<TaskAnalysisVO> getTaskAnalysis(HttpServletRequest request) {
        checkAdmin(request);
        return ResultUtils.success(analysisService.getTaskAnalysis());
    }

    /**
     * 用户活跃度分析
     */
    @GetMapping("/user")
    public BaseResponse<UserAnalysisVO> getUserAnalysis(HttpServletRequest request) {
        checkAdmin(request);
        return ResultUtils.success(analysisService.getUserAnalysis());
    }

    /**
     * 跑腿人员分析
     */
    @GetMapping("/runner")
    public BaseResponse<RunnerAnalysisVO> getRunnerAnalysis(HttpServletRequest request) {
        checkAdmin(request);
        return ResultUtils.success(analysisService.getRunnerAnalysis());
    }

    /**
     * 校验管理员权限
     */
    private void checkAdmin(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (!"admin".equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "仅管理员可访问数据分析");
        }
    }
}
