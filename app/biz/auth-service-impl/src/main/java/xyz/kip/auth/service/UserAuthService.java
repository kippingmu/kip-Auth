package xyz.kip.auth.service;

import xyz.kip.open.common.base.Result;
import xyz.kip.auth.service.model.LoginRequestModel;
import xyz.kip.auth.service.model.LoginResponseModel;
import xyz.kip.auth.service.model.RegisterRequestModel;
import xyz.kip.auth.service.model.UserAuthModel;

/**
 * 用户认证业务服务接口
 * @author xiaoshichuan
 * @version 2026-02-28
 */
public interface UserAuthService {

    /**
     * 用户登录
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    Result<LoginResponseModel> login(LoginRequestModel loginRequest);

    /**
     * 用户注册
     * @param registerRequest 注册请求
     * @return 用户信息
     */
    Result<UserAuthModel> register(RegisterRequestModel registerRequest);

    /**
     * 发送注册验证码
     * @param registerRequest 注册验证码请求
     * @return 6位验证码
     */
    Result<String> sendRegisterVerifyCode(RegisterRequestModel registerRequest);

    /**
     * 通过用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    Result<UserAuthModel> queryByUsername(String username);

    /**
     * 通过用户ID查询用户
     * @param userId 用户ID
     * @return 用户信息
     */
    Result<UserAuthModel> queryByUserId(String userId);

    /**
     * 修改密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    Result<Boolean> changePassword(String userId, String oldPassword, String newPassword);

    /**
     * 重置密码
     * @param userId 用户ID
     * @param newPassword 新密码
     * @return 重置结果
     */
    Result<Boolean> resetPassword(String userId, String newPassword);

    /**
     * 启用用户
     * @param userId 用户ID
     * @return 启用结果
     */
    Result<Boolean> enableUser(String userId);

    /**
     * 禁用用户
     * @param userId 用户ID
     * @return 禁用结果
     */
    Result<Boolean> disableUser(String userId);

    /**
     * 删除用户
     * @param userId 用户ID
     * @return 删除结果
     */
    Result<Boolean> deleteUser(String userId);

    /**
     * 更新用户信息
     * @param userAuth 用户信息
     * @return 更新结果
     */
    Result<Boolean> updateUser(UserAuthModel userAuth);

    /**
     * 验证token
     * @param token JWT token
     * @return 验证结果
     */
    Result<UserAuthModel> validateToken(String token);
}
