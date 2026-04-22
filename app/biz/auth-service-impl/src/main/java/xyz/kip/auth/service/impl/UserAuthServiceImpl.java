package xyz.kip.auth.service.impl;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.util.StringUtils;
import xyz.kip.open.common.base.Result;
import org.springframework.stereotype.Service;

import xyz.kip.auth.service.UserAuthService;

import xyz.kip.auth.service.model.LoginRequestModel;
import xyz.kip.auth.service.model.LoginResponseModel;
import xyz.kip.auth.service.model.RegisterRequestModel;
import xyz.kip.auth.service.model.UserAuthModel;
import xyz.kip.auth.service.utils.JwtUtil;
import xyz.kip.auth.service.utils.PasswordEncoder;
import xyz.kip.auth.service.utils.SnowFlakeUtil;
import xyz.kip.auth.manager.UserManager;
import xyz.kip.auth.manager.domain.UserDomain;
import xyz.kip.auth.manager.cache.CacheManager;
import xyz.kip.auth.manager.util.RedisKeyUtil;

import java.util.HashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 用户认证业务服务实现
 * @author xiaoshichuan
 * @version 2026-02-28
 */
@RefreshScope
@Service
public class UserAuthServiceImpl implements UserAuthService {

    @Value("${auth.token.user-whitelist:}")
    private String tokenUserWhitelistCsv = "";

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private UserManager userManager;

    @Resource
    private CacheManager cacheManager;


    /**
     * 用户登录
     */
    @Override
    public Result<LoginResponseModel> login(LoginRequestModel loginRequest) {
        // 输入验证
        if (loginRequest == null || loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
            return Result.failure("用户名和密码不能为空");
        }

        // C 端用户按账号/邮箱直接查询，不再按租户隔离。
        Result<UserDomain> dbRes = userManager.findByUsername(loginRequest.getUsername());
        if (!dbRes.isSuccess() || dbRes.getResult() == null) {
            return Result.failure("用户名或密码错误");
        }
        UserAuthModel user = toModel(dbRes.getResult());

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
        claims.put("roleCodes", user.getRoleCodes());
        String token = jwtUtil.generateToken(user.getUserId(), user.getUsername(), claims);

        // 构建响应
        LoginResponseModel response = new LoginResponseModel();
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setNickname(user.getNickname());
        response.setRoleCodes(user.getRoleCodes());
        response.setToken(token);
        response.setTokenType("Bearer");
        response.setExpiresIn(jwtUtil.getExpiresIn());

        // 缓存用户token与必要信息供网关使用
        long ttl = jwtUtil.getExpiresIn() != null ? jwtUtil.getExpiresIn() : 0L;
        String tokenKey = RedisKeyUtil.userTokenKey(user.getUserId());
        String infoKey = RedisKeyUtil.userInfoKey(user.getUserId());
        cacheManager.set(tokenKey, token, ttl);

        UserAuthModel cacheUser = new UserAuthModel();
        cacheUser.setUserId(user.getUserId());
        cacheUser.setUsername(user.getUsername());
        cacheUser.setEmail(user.getEmail());
        cacheUser.setPhone(user.getPhone());
        cacheUser.setNickname(user.getNickname());
        cacheUser.setTenantId(user.getTenantId());
        cacheUser.setRoleCodes(user.getRoleCodes());
        cacheManager.set(infoKey, cacheUser, ttl);

        return Result.success(response);
    }

    /**
     * 用户注册
     */
    @Override
    public Result<UserAuthModel> register(RegisterRequestModel registerRequest) {
        if (registerRequest == null || registerRequest.getEmail() == null || registerRequest.getEmail().isBlank()) {
            return Result.failure("邮箱不能为空");
        }

        String account = registerRequest.getEmail().trim();
        Result<UserDomain> exists = userManager.findByUsername(account);
        if (exists.isSuccess() && exists.getResult() != null) {
            return Result.failure("用户已存在");
        }

        String userId = SnowFlakeUtil.nextSegmentId();
        String generatedPhone = buildRegistrationPhone(registerRequest.getPhone(), userId);
        String encoded = passwordEncoder.encodePassword("Pass@123456");
        String salt = java.util.UUID.randomUUID().toString().replace("-", "");
        UserDomain d = new UserDomain();
        d.setUserId(userId);
        d.setUsername(account);
        d.setPhone(generatedPhone);
        d.setEmail(account);
        d.setPassword(encoded);
        d.setSalt(salt);
        d.setStatus(1);
        d.setRoleCodes(List.of("USER"));
        d.setTenantId("default");
        Result<Boolean> created = userManager.createUser(d);
        if (!created.isSuccess() || !Boolean.TRUE.equals(created.getResult())) {
            return Result.failure(created.getMessage() != null ? created.getMessage() : "创建用户失败");
        }

        UserAuthModel result = new UserAuthModel();
        result.setUserId(userId);
        result.setUsername(account);
        result.setEmail(account);
        result.setPhone(generatedPhone);
        result.setNickname(registerRequest.getNickname());
        result.setStatus(1);
        result.setRoleCodes(List.of("USER"));
        return Result.success(result);
    }

    private static String buildRegistrationPhone(String requestPhone, String userId) {
        if (requestPhone == null || requestPhone.isBlank()) {
            String digits = userId == null ? "" : userId.chars()
                    .filter(Character::isDigit)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();
            if (digits.length() >= 11) {
                return digits.substring(digits.length() - 11);
            }
            long hashBase = userId == null ? 0L : Math.abs((long) userId.hashCode()) % 10_000_000_000L;
            return String.format("1%010d", hashBase);
        }
        return requestPhone.trim();
    }

    private static UserAuthModel toModel(UserDomain d) {
        UserAuthModel m = new UserAuthModel();
        m.setUserId(d.getUserId());
        m.setUsername(d.getUsername());
        m.setPhone(d.getPhone());
        m.setEmail(d.getEmail());
        m.setPassword(d.getPassword());
        m.setStatus(d.getStatus());
        m.setTenantId(d.getTenantId());
        m.setRoleCodes(d.getRoleCodes());
        return m;
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
        String username = jwtUtil.getUsernameFromToken(token);
        if (!StringUtils.hasText(userId)) {
            return Result.failure("无效的token");
        }
        if (!isTokenBypassUser(username)) {
            String cachedToken = normalizeCachedToken(cacheManager.get(RedisKeyUtil.userTokenKey(userId), String.class));
            if (!StringUtils.hasText(cachedToken) || !cachedToken.equals(token)) {
                return Result.failure("Token revoked or not latest");
            }
        }
        return queryByUserId(userId);
    }

    private boolean isTokenBypassUser(String username) {
        if (!StringUtils.hasText(username)) {
            return false;
        }
        List<String> whitelist = Arrays.stream(tokenUserWhitelistCsv.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
        return whitelist.contains(username);
    }

    private String normalizeCachedToken(String cachedToken) {
        if (!StringUtils.hasText(cachedToken)) {
            return cachedToken;
        }
        String normalized = cachedToken.trim();
        if (normalized.length() >= 2 && normalized.startsWith("\"") && normalized.endsWith("\"")) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        return normalized;
    }

    /**
     * 通过用户名查询用户（DB 优先，默认租户 default）
     */
    @Override
    public Result<UserAuthModel> queryByUsername(String username) {
        Result<UserDomain> res = userManager.findByUsername(username);
        if (res.isSuccess() && res.getResult() != null) {
            return Result.success(toModel(res.getResult()));
        }
        return Result.failure("用户不存在");
    }

    /**
     * 通过用户ID查询用户
     */
    @Override
    public Result<UserAuthModel> queryByUserId(String userId) {
        Result<UserDomain> res = userManager.findByUserId(userId);
        if (res.isSuccess() && res.getResult() != null) {
            return Result.success(toModel(res.getResult()));
        }
        return Result.failure("用户不存在");
    }

    /**
     * 修改密码
     */
    @Override
    public Result<Boolean> changePassword(String userId, String oldPassword, String newPassword) {
        if (userId == null || oldPassword == null || newPassword == null) {
            return Result.failure("参数不能为空");
        }
        Result<UserAuthModel> qr = queryByUserId(userId);
        if (!qr.isSuccess() || qr.getResult() == null) {
            return Result.failure("用户不存在");
        }
        UserAuthModel user = qr.getResult();
        if (!passwordEncoder.matchPassword(oldPassword, user.getPassword())) {
            return Result.failure("原密码错误");
        }
        String encoded = passwordEncoder.encodePassword(newPassword);
        String salt = java.util.UUID.randomUUID().toString().replace("-", "");
        Result<Boolean> updated = userManager.updatePassword(userId, encoded, salt, "system");
        if (!updated.isSuccess() || !Boolean.TRUE.equals(updated.getResult())) {
            return Result.failure(updated.getMessage());
        }
        return Result.success(true);
    }

    /**
     * 重置密码
     */
    @Override
    public Result<Boolean> resetPassword(String userId, String newPassword) {
        if (userId == null || newPassword == null) {
            return Result.failure("参数不能为空");
        }
        Result<UserAuthModel> qr = queryByUserId(userId);
        if (!qr.isSuccess() || qr.getResult() == null) {
            return Result.failure("用户不存在");
        }
        String encoded = passwordEncoder.encodePassword(newPassword);
        String salt = java.util.UUID.randomUUID().toString().replace("-", "");
        Result<Boolean> updated = userManager.updatePassword(userId, encoded, salt, "system");
        if (!updated.isSuccess() || !Boolean.TRUE.equals(updated.getResult())) {
            return Result.failure(updated.getMessage());
        }
        return Result.success(true);
    }

    /**
     * 启用用户
     */
    @Override
    public Result<Boolean> enableUser(String userId) {
        Result<UserAuthModel> qr = queryByUserId(userId);
        if (!qr.isSuccess() || qr.getResult() == null) {
            return Result.failure("用户不存在");
        }
        Result<Boolean> updated = userManager.updateStatus(userId, 1, "system");
        if (!updated.isSuccess() || !Boolean.TRUE.equals(updated.getResult())) {
            return Result.failure(updated.getMessage());
        }
        return Result.success(true);
    }

    /**
     * 禁用用户
     */
    @Override
    public Result<Boolean> disableUser(String userId) {
        Result<UserAuthModel> qr = queryByUserId(userId);
        if (!qr.isSuccess() || qr.getResult() == null) {
            return Result.failure("用户不存在");
        }
        Result<Boolean> updated = userManager.updateStatus(userId, 0, "system");
        if (!updated.isSuccess() || !Boolean.TRUE.equals(updated.getResult())) {
            return Result.failure(updated.getMessage());
        }
        return Result.success(true);
    }

    /**
     * 删除用户
     */
    @Override
    public Result<Boolean> deleteUser(String userId) {
        Result<UserAuthModel> qr = queryByUserId(userId);
        if (!qr.isSuccess() || qr.getResult() == null) {
            return Result.failure("用户不存在");
        }
        Result<Boolean> deleted = userManager.deleteUser(userId);
        if (!deleted.isSuccess() || !Boolean.TRUE.equals(deleted.getResult())) {
            return Result.failure(deleted.getMessage());
        }
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
        Result<UserAuthModel> qr = queryByUserId(userAuth.getUserId());
        if (!qr.isSuccess() || qr.getResult() == null) {
            return Result.failure("用户不存在");
        }
        UserDomain d = new UserDomain();
        d.setUserId(userAuth.getUserId());
        d.setUsername(userAuth.getUsername());
        d.setEmail(userAuth.getEmail());
        d.setPhone(userAuth.getPhone());
        d.setStatus(userAuth.getStatus());
        Result<Boolean> updated = userManager.updateUser(d);
        if (!updated.isSuccess() || !Boolean.TRUE.equals(updated.getResult())) {
            return Result.failure(updated.getMessage());
        }
        return Result.success(true);
    }
}
