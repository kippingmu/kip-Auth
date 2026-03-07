package xyz.kip.auth.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.kip.auth.request.LoginRequestRequest;
import xyz.kip.auth.resonse.LoginResponse;
import xyz.kip.auth.resonse.RegisterRequest;
import xyz.kip.auth.request.UserAuthRequest;
import xyz.kip.auth.resonse.UserAuthResponse;
import xyz.kip.service.UserAuthService;
import xyz.kip.open.common.base.Result;
import xyz.kip.service.model.LoginRequestModel;
import xyz.kip.service.model.LoginResponseModel;
import xyz.kip.service.model.RegisterRequestModel;
import xyz.kip.service.model.UserAuthModel;

/**
 * 用户认证控制器
 *
 * @author xiaoshichuan
 * @version 2026-02-28
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserAuthController {

    @Resource
    private UserAuthService userAuthService;

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequestRequest loginRequest) {
        LoginRequestModel loginRequestModel = new LoginRequestModel();
        Result<LoginResponseModel> login = userAuthService.login(loginRequestModel);
        return Result.success(new LoginResponse());
    }

    /**
     * 用户注册
     *
     * @param registerRequest 注册请求
     * @return 用户信息
     */
    @PostMapping("/register")
    public Result<UserAuthRequest> register(@RequestBody RegisterRequest registerRequest) {
        RegisterRequestModel registerRequestModel = new RegisterRequestModel();

        userAuthService.register(registerRequestModel);
        return Result.success(new UserAuthRequest());
    }

    /**
     * 获取当前用户信息 (需要token认证)
     *
     * @param token Authorization header中的token
     * @return 用户信息
     */
    @GetMapping("/user/info")
    public Result<UserAuthModel> getUserInfo(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || token.isEmpty()) {
            return Result.failure("缺少authorization token");
        }

        // 移除 "Bearer " 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        return userAuthService.validateToken(token);
    }

    /**
     * 验证token
     *
     * @param token Authorization header中的token
     * @return 验证结果
     */
    @PostMapping("/token/validate")
    public Result<UserAuthModel> validateToken(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || token.isEmpty()) {
            return Result.failure("缺少authorization token");
        }

        // 移除 "Bearer " 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        return userAuthService.validateToken(token);
    }

    /**
     * 修改密码
     *
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    @PostMapping("/password/change")
    public Result<Boolean> changePassword(
            @RequestParam String userId,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        return userAuthService.changePassword(userId, oldPassword, newPassword);
    }

    /**
     * 重置密码
     *
     * @param userId      用户ID
     * @param newPassword 新密码
     * @return 重置结果
     */
    @PostMapping("/password/reset")
    public Result<Boolean> resetPassword(
            @RequestParam String userId,
            @RequestParam String newPassword) {
        return userAuthService.resetPassword(userId, newPassword);
    }

    /**
     * 启用用户
     *
     * @param userId 用户ID
     * @return 启用结果
     */
    @PostMapping("/user/enable")
    public Result<Boolean> enableUser(@RequestParam String userId) {
        return userAuthService.enableUser(userId);
    }

    /**
     * 禁用用户
     *
     * @param userId 用户ID
     * @return 禁用结果
     */
    @PostMapping("/user/disable")
    public Result<Boolean> disableUser(@RequestParam String userId) {
        return userAuthService.disableUser(userId);
    }

    /**
     * 删除用户
     *
     * @param userId 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/user/{userId}")
    public Result<Boolean> deleteUser(@PathVariable String userId) {
        return userAuthService.deleteUser(userId);
    }

    /**
     * 查询用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/user/{userId}")
    public Result<UserAuthResponse> getUser(@PathVariable String userId) {
        userAuthService.queryByUserId(userId);
        UserAuthResponse userAuthResponse = new UserAuthResponse();
        return Result.success(new UserAuthResponse());

    }

    /**
     * 通过用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    @GetMapping("/user/name/{username}")
    public Result<UserAuthResponse> getUserByName(@PathVariable String username) {
        userAuthService.queryByUsername(username);
        UserAuthResponse userAuthResponse = new UserAuthResponse();
        return Result.success(new UserAuthResponse());
    }

    /**
     * 更新用户信息
     *
     * @param userAuth 用户信息
     * @return 更新结果
     */
    @PutMapping("/user")
    public Result<Boolean> updateUser(@RequestBody UserAuthRequest userAuth) {
        UserAuthModel userAuthModel = new UserAuthModel();
        userAuthService.updateUser(new UserAuthModel());
        return Result.success(true);

    }

    /**
     * 健康检查
     *
     * @return ok
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("Auth service is running");
    }
}
