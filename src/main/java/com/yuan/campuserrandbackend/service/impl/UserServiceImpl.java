package com.yuan.campuserrandbackend.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teautil.models.RuntimeOptions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuan.campuserrandbackend.exception.BusinessException;
import com.yuan.campuserrandbackend.exception.ErrorCode;
import com.yuan.campuserrandbackend.mapper.UserMapper;
import com.yuan.campuserrandbackend.model.entity.User;
import com.yuan.campuserrandbackend.model.enums.UserRoleEnum;
import com.yuan.campuserrandbackend.manager.CosManager;
import com.yuan.campuserrandbackend.model.vo.LoginUserVO;
import com.yuan.campuserrandbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.UUID;

import static com.yuan.campuserrandbackend.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author 86182
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2025-11-27 19:33:46
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private CosManager cosManager;

    @Value("${aliyun.sms.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.sms.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.sms.sign-name}")
    private String signName;

    @Value("${aliyun.sms.template-code}")
    private String templateCode;

    /**
     * 短信验证码在 Session 中的前缀
     */
    private static final String SMS_CODE_KEY_PREFIX = "sms_code_";

    /**
     * 短信验证码有效期（毫秒）
     */
    private static final long SMS_CODE_EXPIRE_MILLIS = 5 * 60 * 1000L;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String userRole,
            String contactInfo) {
        // 1. 校验
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        // 校验联系方式（手机号格式）
        if (StrUtil.isNotBlank(contactInfo)) {
            if (!contactInfo.matches("^1[3-9]\\d{9}$")) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号格式不正确");
            }
        }

        // 校验用户角色
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(userRole);
        if (userRoleEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户角色不合法");
        }

        // 2. 检查是否重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 3. 加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 4. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("无名");
        user.setUserRole(userRole); // 使用传入的用户角色
        user.setContactInfo(contactInfo);
        // 设置默认值
        user.setCreditScore(100.0); // 默认信誉分100
        user.setOrderCount(0); // 初始接单数为0

        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }
        return user.getId();
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接返回上述结果）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public String getEncryptPassword(String userPassword) {
        // 盐值，混淆密码
        final String SALT = "yuan";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    @Override
    public boolean sendSmsCode(String phone, HttpServletRequest request) {
        if (StrUtil.isBlank(phone)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号不能为空");
        }
        // 简单校验手机号长度（真实项目可用正则校验）
        if (phone.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号格式错误");
        }
        // 生成 6 位数字验证码
        String code = RandomUtil.randomNumbers(6);
        long expireTime = System.currentTimeMillis() + SMS_CODE_EXPIRE_MILLIS;
        SmsCode smsCode = new SmsCode(code, expireTime);
        request.getSession().setAttribute(SMS_CODE_KEY_PREFIX + phone, smsCode);

        try {
            // 使用凭据初始化账号Client（按照官方示例代码）
            com.aliyun.credentials.Client credential = new com.aliyun.credentials.Client();

            com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                    .setCredential(credential)
                    .setAccessKeyId(accessKeyId)
                    .setAccessKeySecret(accessKeySecret);
            // 号码认证服务固定 endpoint
            config.endpoint = "dypnsapi.aliyuncs.com";
            Client client = new Client(config);

            // 组装请求参数：签名、模板、手机号、模板中的验证码变量
            // 模板100001需要两个参数：code（验证码）和min（有效期分钟数）
            int expireMinutes = (int) (SMS_CODE_EXPIRE_MILLIS / (60 * 1000)); // 转换为分钟
            String templateParam = "{\"code\":\"" + code + "\",\"min\":\"" + expireMinutes + "\"}";

            SendSmsVerifyCodeRequest sendSmsVerifyCodeRequest = new SendSmsVerifyCodeRequest()
                    .setPhoneNumber(phone)
                    .setSignName(signName)
                    .setTemplateCode(templateCode)
                    .setTemplateParam(templateParam);

            // 记录发送的参数（用于调试对比）
            log.info("SendSmsVerifyCode params: SignName={}, TemplateCode={}, PhoneNumber={}, TemplateParam={}",
                    signName, templateCode, phone, templateParam);

            // 使用sendSmsVerifyCodeWithOptions方法（按照官方示例）
            RuntimeOptions runtime = new RuntimeOptions();
            SendSmsVerifyCodeResponse sendSmsVerifyCodeResponse = client
                    .sendSmsVerifyCodeWithOptions(sendSmsVerifyCodeRequest, runtime);
            String respCode = sendSmsVerifyCodeResponse.getBody().getCode();
            String respMsg = sendSmsVerifyCodeResponse.getBody().getMessage();
            log.info("Aliyun SMS send result, phone = {}, code = {}, respCode = {}, respMsg = {}", phone, code,
                    respCode, respMsg);

            if (!"OK".equalsIgnoreCase(respCode)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "短信发送失败：" + respMsg);
            }
        } catch (TeaException e) {
            // 阿里云SDK抛出的异常，包含详细的错误信息
            log.error("Aliyun SMS TeaException, phone = {}, code = {}, message = {}, requestId = {}",
                    phone, e.getCode(), e.getMessage(), e.getData().get("RequestId"));
            String errorMsg = "短信发送失败：" + e.getMessage();
            if (e.getCode() != null && e.getCode().equals("403")) {
                errorMsg += "（权限不足，请检查AccessKey权限、签名名称或模板代码是否正确）";
            }
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMsg);
        } catch (Exception e) {
            log.error("sendSmsCode error, phone = {}", phone, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "短信发送异常：" + e.getMessage());
        }

        return true;
    }

    @Override
    public LoginUserVO smsLogin(String phone, String code, HttpServletRequest request) {
        if (StrUtil.hasBlank(phone, code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号或验证码不能为空");
        }
        Object obj = request.getSession().getAttribute(SMS_CODE_KEY_PREFIX + phone);
        if (!(obj instanceof SmsCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码不存在，请先获取验证码");
        }
        SmsCode smsCode = (SmsCode) obj;
        long now = System.currentTimeMillis();
        if (now > smsCode.getExpireTime()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码已过期，请重新获取");
        }
        if (!code.equals(smsCode.getCode())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }
        // 验证通过后，删除验证码
        request.getSession().removeAttribute(SMS_CODE_KEY_PREFIX + phone);

        // 根据手机号查找用户，这里采用 contactInfo 作为手机号存储字段
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("contactInfo", phone);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 如果用户不存在，则自动注册一个新用户（方便体验）
        if (user == null) {
            user = new User();
            user.setUserAccount(phone);
            user.setUserPassword(getEncryptPassword(RandomUtil.randomString(8)));
            user.setUserName("手机用户");
            user.setUserRole(UserRoleEnum.USER.getValue());
            user.setContactInfo(phone);
            user.setCreditScore(100.0);
            user.setOrderCount(0);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
        }
        // 记录登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    /**
     * 存储在 Session 中的短信验证码
     */
    @Override
    public String uploadAvatar(MultipartFile multipartFile, HttpServletRequest request) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }
        User loginUser = this.getLoginUser(request);

        String originalFilename = multipartFile.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String key = "avatar/" + loginUser.getId() + "/" + UUID.randomUUID() + ext;

        try {
            String url = cosManager.upload(multipartFile.getInputStream(), multipartFile.getSize(),
                    multipartFile.getContentType(), key);
            User update = new User();
            update.setId(loginUser.getId());
            update.setUserAvatar(url);
            boolean result = this.updateById(update);
            if (!result) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新头像失败");
            }
            return url;
        } catch (Exception e) {
            log.error("uploadAvatar error", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传头像失败：" + e.getMessage());
        }
    }

    @Override
    public boolean bindPhone(String phone, String code, HttpServletRequest request) {
        if (StrUtil.hasBlank(phone, code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号或验证码不能为空");
        }
        User loginUser = this.getLoginUser(request);
        Object obj = request.getSession().getAttribute(SMS_CODE_KEY_PREFIX + phone);
        if (!(obj instanceof SmsCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码不存在，请先获取验证码");
        }
        SmsCode smsCode = (SmsCode) obj;
        long now = System.currentTimeMillis();
        if (now > smsCode.getExpireTime()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码已过期，请重新获取");
        }
        if (!code.equals(smsCode.getCode())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }
        request.getSession().removeAttribute(SMS_CODE_KEY_PREFIX + phone);

        User update = new User();
        update.setId(loginUser.getId());
        update.setContactInfo(phone);
        return this.updateById(update);
    }

    /**
     * 存储在 Session 中的短信验证码
     */
    private static class SmsCode implements Serializable {
        private final String code;
        private final long expireTime;

        SmsCode(String code, long expireTime) {
            this.code = code;
            this.expireTime = expireTime;
        }

        public String getCode() {
            return code;
        }

        public long getExpireTime() {
            return expireTime;
        }
    }

}
