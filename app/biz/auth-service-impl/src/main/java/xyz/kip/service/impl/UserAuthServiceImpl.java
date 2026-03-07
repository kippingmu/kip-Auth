package xyz.kip.service.impl;

import jakarta.annotation.Resource;
import xyz.kip.open.common.base.Result;
import org.springframework.stereotype.Service;

import xyz.kip.service.UserAuthService;

import xyz.kip.service.model.LoginRequestModel;
import xyz.kip.service.model.LoginResponseModel;
import xyz.kip.service.model.RegisterRequestModel;
import xyz.kip.service.model.UserAuthModel;
import xyz.kip.service.security.JwtUtil;
import xyz.kip.service.security.PasswordEncoder;
import xyz.kip.auth.infrastructure.util.SnowFlakeUtil;
import xyz.kip.service.repository.InMemoryUserStore;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户认证业务服务实现
 * @author xiaoshichuan
 * @version 2026-02-28
 */
@Service
public class UserAuthServiceImpl implements UserAuthService {

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private InMemoryUserStore userStore;

    // TODO: 注入用户数据访问层 (Repository)
    // @Resource
    // private UserAuthRepository userAuthRepository;

    /**
     * 用户登录
     */
    @Override
    public Result<LoginResponseModel> login(LoginRequestModel loginRequest) {
        // 输入验证
        if (loginRequest == null || loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
            return Result.failure("用户名和密码不能为空");
        }

        // 查询用户
        Result<UserAuthModel> queryResult = queryByUsername(loginRequest.getUsername());
        if (!queryResult.isSuccess() || queryResult.getResult() == null) {
            return Result.failure("用户名或密码错误");
        }

        UserAuthModel user = queryResult.getResult();

        // 验证账号状态
        if (user.getStatus() == 0) {
            return Result.failure("账号已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matchPassword(loginRequest.getPassword(), user.getPassword())) {
            return Result.failure("用户名或密码错误");
        }

        // 生成JWT token
        Map<String, Object> claims = new HashMap<>();
        claims.put("tenantId", user.getTenantId());
        String token = jwtUtil.generateToken(user.getUserId(), user.getUsername(), claims);

        // 构建响应
        LoginResponseModel response = new LoginResponseModel();
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setNickname(user.getNickname());
        response.setToken(token);
        response.setTokenType("Bearer");
        response.setExpiresIn(jwtUtil.getExpiresIn());

        return Result.success(response);
    }

    /**
     * 用户注册
     */
    @Override
    public Result<UserAuthModel> register(RegisterRequestModel registerRequest) {
        // 输入验证
        if (registerRequest == null || registerRequest.getUsername() == null || registerRequest.getPassword() == null) {
            return Result.failure("用户名和密码不能为空");
        }

        // 验证密码一致性
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            return Result.failure("两次输入的密码不一致");
        }

        // 验证用户是否已存在
        Result<UserAuthModel> queryResult = queryByUsername(registerRequest.getUsername());
        if (queryResult.isSuccess() && queryResult.getResult() != null) {
            return Result.failure("用户已存在");
        }

        // 创建新用户
        UserAuthModel newUser = new UserAuthModel();
        newUser.setUserId(SnowFlakeUtil.nextId() + "");
        newUser.setUsername(registerRequest.getUsername());
        newUser.setPassword(passwordEncoder.encodePassword(registerRequest.getPassword()));
        newUser.setEmail(registerRequest.getEmail());
        newUser.setPhone(registerRequest.getPhone());
        newUser.setNickname(registerRequest.getNickname());
        newUser.setStatus(1); // 1-启用
        newUser.setTenantId(registerRequest.getTenantId() != null ? registerRequest.getTenantId() : "default");

        userStore.saveUser(newUser);

        UserAuthModel result = new UserAuthModel();
        result.setUserId(newUser.getUserId());
        result.setUsername(newUser.getUsername());
        result.setEmail(newUser.getEmail());
        result.setPhone(newUser.getPhone());
        result.setNickname(newUser.getNickname());
        result.setStatus(newUser.getStatus());
        result.setTenantId(newUser.getTenantId());

        return Result.success(result);
    }

    /**
     * 通过用户名查询用户
     */
    @Override
    public Result<UserAuthModel> queryByUsername(String username) {
        UserAuthModel user = userStore.getUserByUsername(username);
        if (user != null) {
            return Result.success(user);
        }
        return Result.failure("用户不存在");
    }

    /**
     * 通过用户ID查询用户
     */
    @Override
    public Result<UserAuthModel> queryByUserId(String userId) {
        UserAuthModel user = userStore.getUserById(userId);
        if (user != null) {
            return Result.success(user);
        }
        return Result.failure("用户不存在");
    }

    /**
     * 修改密码
     */
    @Override
    public Result<Boolean> changePassword(String userId, String oldPassword, String newPassword) {
        // 验证参数
        if (userId == null || oldPassword == null || newPassword == null) {
            return Result.failure("参数不能为空");
        }

        // 查询用户
        Result<UserAuthModel> queryResult = queryByUserId(userId);
        if (!queryResult.isSuccess() || queryResult.getResult() == null) {
            return Result.failure("用户不存在");
        }

        UserAuthModel user = queryResult.getResult();

        // 验证旧密码
        if (!passwordEncoder.matchPassword(oldPassword, user.getPassword())) {
            return Result.failure("原密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encodePassword(newPassword));
        userStore.updateUser(user);

        return Result.success(true);
    }

    /**
     * 重置密码
     */
    @Override
    public Result<Boolean> resetPassword(String userId, String newPassword) {
        // 验证参数
        if (userId == null || newPassword == null) {
            return Result.failure("参数不能为空");
        }

        // 查询用户
        Result<UserAuthModel> queryResult = queryByUserId(userId);
        if (!queryResult.isSuccess() || queryResult.getResult() == null) {
            return Result.failure("用户不存在");
        }

        // 重置密码
        UserAuthModel user = queryResult.getResult();
        user.setPassword(passwordEncoder.encodePassword(newPassword));
        userStore.updateUser(user);

        return Result.success(true);
    }

    /**
     * 启用用户
     */
    @Override
    public Result<Boolean> enableUser(String userId) {
        Result<UserAuthModel> queryResult = queryByUserId(userId);
        if (!queryResult.isSuccess() || queryResult.getResult() == null) {
            return Result.failure("用户不存在");
        }

        UserAuthModel user = queryResult.getResult();
        user.setStatus(1);
        userStore.updateUser(user);

        return Result.success(true);
    }

    /**
     * 禁用用户
     */
    @Override
    public Result<Boolean> disableUser(String userId) {
        Result<UserAuthModel> queryResult = queryByUserId(userId);
        if (!queryResult.isSuccess() || queryResult.getResult() == null) {
            return Result.failure("用户不存在");
        }

        UserAuthModel user = queryResult.getResult();
        user.setStatus(0);
        userStore.updateUser(user);

        return Result.success(true);
    }

    /**
     * 删除用户
     */
    @Override
    public Result<Boolean> deleteUser(String userId) {
        Result<UserAuthModel> queryResult = queryByUserId(userId);
        if (!queryResult.isSuccess() || queryResult.getResult() == null) {
            return Result.failure("用户不存在");
        }

        userStore.deleteUser(userId);
        return Result.success(true);
    }

    /**
     * 更新用户信息
     */
    @Override
    public Result<Boolean> updateUser(UserAuthModel userAuth) {
        if (userAuth == null || userAuth.getUserId() == null) {
            return Result.failure("参数不能为空");
        }

        Result<UserAuthModel> queryResult = queryByUserId(userAuth.getUserId());
        if (!queryResult.isSuccess() || queryResult.getResult() == null) {
            return Result.failure("用户不存在");
        }

        userStore.updateUser(userAuth);

        return Result.success(true);
    }

    /**
     * 验证token
     */
    @Override
    public Result<UserAuthModel> validateToken(String token) {
        if (token == null || !jwtUtil.validateToken(token)) {
            return Result.failure("无效的token");
        }

        String userId = jwtUtil.getUserIdFromToken(token);
        return queryByUserId(userId);
    }
}
