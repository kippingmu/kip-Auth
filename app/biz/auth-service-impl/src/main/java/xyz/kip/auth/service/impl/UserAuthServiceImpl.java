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
import xyz.kip.auth.manager.UserManager;
import xyz.kip.auth.manager.domain.UserDomain;
import xyz.kip.auth.manager.cache.CacheManager;
import xyz.kip.auth.manager.enums.AuthTyepEnum;
import xyz.kip.auth.manager.util.RedisKeyUtil;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 用户认证业务服务实现
 * @author xiaoshichuan
 * @version 2026-02-28
 */
@RefreshScope
@Service
public class UserAuthServiceImpl implements UserAuthService {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int VERIFY_CODE_TTL_SECONDS = 60;
    private static final int ONE_HOUR_SEND_LIMIT = 3;
    private static final int ONE_DAY_SEND_LIMIT = 5;
    private static final long ONE_HOUR_MILLIS = 60L * 60L * 1000L;
    private static final long ONE_DAY_MILLIS = 24L * ONE_HOUR_MILLIS;
    private static final long DEFAULT_OPERATOR_ID = 1L;
    private static final String DEFAULT_PERSONAL_FEATURE = "幽默";
    private static final String DEFAULT_OCCUPATION = "软件开发";
    private static final SecureRandom RANDOM = new SecureRandom();

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
        Result<UserDomain> dbRes = findLoginUser(loginRequest);
        if (!dbRes.isSuccess()) {
            return Result.failure(dbRes.getMessage());
        }
        if (!dbRes.isSuccess() || dbRes.getResult() == null) {
            return Result.failure("手机号/邮箱未注册");
        }
        UserAuthModel user = toModel(dbRes.getResult());

        // 验证账号状态
        if (user.getStatus() == 0) {
            return Result.failure("账号已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matchPassword(loginRequest.getPassword(), user.getPassword())) {
            return Result.failure("手机号/邮箱或密码错误");
        }

        // 生成JWT token
        Map<String, Object> claims = new HashMap<>();
        claims.put("roleCodes", user.getRoleCodes());
        String token = jwtUtil.generateToken(user.getUserId(), tokenIdentity(user), claims);

        // 构建响应
        LoginResponseModel response = new LoginResponseModel();
        response.setUserId(user.getUserId());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
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
        cacheUser.setEmail(user.getEmail());
        cacheUser.setPhone(user.getPhone());
        cacheUser.setName(user.getName());
        cacheUser.setBirthYear(user.getBirthYear());
        cacheUser.setPersonalFeature(user.getPersonalFeature());
        cacheUser.setOccupation(user.getOccupation());
        cacheUser.setRoleCodes(user.getRoleCodes());
        cacheManager.set(infoKey, cacheUser, ttl);

        return Result.success(response);
    }

    /**
     * 用户注册
     */
    @Override
    public Result<UserAuthModel> register(RegisterRequestModel registerRequest) {
        AuthTyepEnum authType = AuthTyepEnum.fromCode(registerRequest.getAuthType());
        if (authType == null) {
            return Result.failure("认证类型不正确");
        }
        String phone = normalizePhone(registerRequest.getPhone());
        String email = normalizeEmail(registerRequest.getEmail());
        Result<Void> authIdentityValidation = validateRequiredIdentity(authType, phone, email);
        if (!authIdentityValidation.isSuccess()) {
            return Result.failure(authIdentityValidation.getMessage());
        }
        String identifier = buildRegisterIdentifier(authType, phone, email);
        String verifyCode = normalize(registerRequest.getVerifyCode());
        String cachedCode = cacheManager.get(RedisKeyUtil.verifyCodeKey(identifier), String.class);
        if (!verifyCode.equals(cachedCode)) {
            return Result.failure("验证码错误或已过期");
        }

        Result<Void> duplicateValidation = validateRegisterIdentityAvailable(phone, email);
        if (!duplicateValidation.isSuccess()) {
            return Result.failure(duplicateValidation.getMessage());
        }

        String encoded = passwordEncoder.encodePassword(registerRequest.getPassword());
        String salt = UUID.randomUUID().toString().replace("-", "");
        UserDomain d = new UserDomain();
        d.setPhone(phone);
        d.setEmail(email);
        d.setName(normalize(registerRequest.getName()));
        d.setPersonalFeature(DEFAULT_PERSONAL_FEATURE);
        d.setOccupation(DEFAULT_OCCUPATION);
        d.setPassword(encoded);
        d.setSalt(salt);
        d.setStatus(1);
        d.setRoleCodes(List.of("USER"));
        Result<UserDomain> created = userManager.createUser(d);
        if (!created.isSuccess() || created.getResult() == null) {
            return Result.failure(created.getMessage() != null ? created.getMessage() : "创建用户失败");
        }
        cacheManager.delete(RedisKeyUtil.verifyCodeKey(identifier));
        return Result.success(toModel(created.getResult()));
    }

    @Override
    public Result<String> sendRegisterVerifyCode(RegisterRequestModel registerRequest) {
        AuthTyepEnum authType = AuthTyepEnum.fromCode(registerRequest.getAuthType());
        if (authType == null) {
            return Result.failure("认证类型不正确");
        }
        String phone = normalizePhone(registerRequest.getPhone());
        String email = normalizeEmail(registerRequest.getEmail());
        Result<Void> authIdentityValidation = validateRequiredIdentity(authType, phone, email);
        if (!authIdentityValidation.isSuccess()) {
            return Result.failure(authIdentityValidation.getMessage());
        }
        String identifier = buildRegisterIdentifier(authType, phone, email);

        Result<Void> duplicateValidation = validateRegisterIdentityAvailable(phone, email);
        if (!duplicateValidation.isSuccess()) {
            return Result.failure(duplicateValidation.getMessage());
        }

        Result<Void> limitResult = checkAndRecordVerifySend(identifier);
        if (!limitResult.isSuccess()) {
            return Result.failure(limitResult.getMessage());
        }

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        cacheManager.set(RedisKeyUtil.verifyCodeKey(identifier), code, VERIFY_CODE_TTL_SECONDS);
        return Result.success(code);
    }

    private static UserAuthModel toModel(UserDomain d) {
        UserAuthModel m = new UserAuthModel();
        m.setUserId(d.getUserId());
        m.setPhone(d.getPhone());
        m.setEmail(d.getEmail());
        m.setName(d.getName());
        m.setPassword(d.getPassword());
        m.setStatus(d.getStatus());
        m.setBirthYear(d.getBirthYear());
        m.setPersonalFeature(d.getPersonalFeature());
        m.setOccupation(d.getOccupation());
        m.setRoleCodes(d.getRoleCodes());
        return m;
    }

    private Result<UserDomain> findLoginUser(LoginRequestModel loginRequest) {
        AuthTyepEnum authType = AuthTyepEnum.fromCode(loginRequest.getAuthType());
        if (authType == null) {
            return Result.failure("认证类型不正确");
        }
        String phone = normalizePhone(loginRequest.getPhone());
        String email = normalizeEmail(loginRequest.getEmail());
        Result<Void> authIdentityValidation = validateRequiredIdentity(authType, phone, email);
        if (!authIdentityValidation.isSuccess()) {
            return Result.failure(authIdentityValidation.getMessage());
        }
        if (AuthTyepEnum.EMAIL == authType) {
            return userManager.findByEmail(email);
        }
        if (AuthTyepEnum.PHONE == authType) {
            return userManager.findByPhone(phone);
        }
        return Result.failure("认证类型不正确");
    }

    private Result<Void> validateRegisterIdentityAvailable(String phone, String email) {
        if (StringUtils.hasText(phone)) {
            Result<UserDomain> byPhone = userManager.findByPhone(phone);
            if (!byPhone.isSuccess()) {
                return Result.failure(byPhone.getMessage());
            }
            if (byPhone.getResult() != null) {
                return Result.failure("手机号已注册");
            }
        }
        if (StringUtils.hasText(email)) {
            Result<UserDomain> byEmail = userManager.findByEmail(email);
            if (!byEmail.isSuccess()) {
                return Result.failure(byEmail.getMessage());
            }
            if (byEmail.getResult() != null) {
                return Result.failure("邮箱已注册");
            }
        }
        return Result.success(null);
    }

    private Result<Void> checkAndRecordVerifySend(String identifier) {
        String key = RedisKeyUtil.registerVerifySendKey(identifier);
        long now = System.currentTimeMillis();
        removeHistoryBefore(key, now - ONE_DAY_MILLIS);

        int hourCount = cacheManager.zRangeByScore(key, now - ONE_HOUR_MILLIS, now, String.class).size();
        if (hourCount >= ONE_HOUR_SEND_LIMIT) {
            return Result.failure("同一邮箱/手机号1小时最多发送3次验证码");
        }
        int dayCount = cacheManager.zRangeByScore(key, now - ONE_DAY_MILLIS, now, String.class).size();
        if (dayCount >= ONE_DAY_SEND_LIMIT) {
            return Result.failure("同一邮箱/手机号1天最多发送5次验证码");
        }

        cacheManager.zAdd(key, now + ":" + UUID.randomUUID(), now);
        cacheManager.expire(key, ONE_DAY_MILLIS / 1000L);
        return Result.success(null);
    }

    private void removeHistoryBefore(String key, long cutoffMillis) {
        Set<String> expired = cacheManager.zRangeByScore(key, 0, cutoffMillis, String.class);
        if (!expired.isEmpty()) {
            cacheManager.zRemove(key, expired.toArray());
        }
    }

    private static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    private static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    private static String normalizePhone(String phone) {
        String normalized = normalize(phone);
        return normalized.isEmpty() ? null : normalized;
    }

    private static String normalizeEmail(String email) {
        String normalized = normalize(email);
        return normalized.isEmpty() ? null : normalized;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private static String normalizeToNull(String value) {
        String normalized = normalize(value);
        return normalized.isEmpty() ? null : normalized;
    }

    private Result<Void> validateRequiredIdentity(AuthTyepEnum authType, String phone, String email) {
        if (AuthTyepEnum.EMAIL == authType && !StringUtils.hasText(email)) {
            return Result.failure("邮箱不能为空");
        }
        if (AuthTyepEnum.PHONE == authType && !StringUtils.hasText(phone)) {
            return Result.failure("手机号不能为空");
        }
        return Result.success(null);
    }

    private static String buildRegisterIdentifier(AuthTyepEnum authType, String phone, String email) {
        if (AuthTyepEnum.EMAIL == authType) {
            return "email:" + email;
        }
        return "phone:" + phone;
    }

    private static String tokenIdentity(UserAuthModel user) {
        if (StringUtils.hasText(user.getEmail())) {
            return user.getEmail();
        }
        return user.getPhone();
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
        String identity = jwtUtil.getUsernameFromToken(token);
        if (!StringUtils.hasText(userId)) {
            return Result.failure("无效的token");
        }
        if (!isTokenBypassUser(identity)) {
            String cachedToken = normalizeCachedToken(cacheManager.get(RedisKeyUtil.userTokenKey(userId), String.class));
            if (!StringUtils.hasText(cachedToken) || !cachedToken.equals(token)) {
                return Result.failure("Token revoked or not latest");
            }
        }
        return queryByUserId(userId);
    }

    private boolean isTokenBypassUser(String identity) {
        if (!StringUtils.hasText(identity)) {
            return false;
        }
        List<String> whitelist = Arrays.stream(tokenUserWhitelistCsv.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
        return whitelist.contains(identity);
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
     * 通过邮箱查询用户
     */
    @Override
    public Result<UserAuthModel> queryByEmail(String email) {
        Result<UserDomain> res = userManager.findByEmail(email);
        if (res.isSuccess() && res.getResult() != null) {
            return Result.success(toModel(res.getResult()));
        }
        return Result.failure("用户不存在");
    }

    /**
     * 通过手机号查询用户
     */
    @Override
    public Result<UserAuthModel> queryByPhone(String phone) {
        Result<UserDomain> res = userManager.findByPhone(phone);
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
        Result<Boolean> updated = userManager.updatePassword(userId, encoded, salt, DEFAULT_OPERATOR_ID);
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
        Result<Boolean> updated = userManager.updatePassword(userId, encoded, salt, DEFAULT_OPERATOR_ID);
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
        Result<Boolean> updated = userManager.updateStatus(userId, 1, DEFAULT_OPERATOR_ID);
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
        Result<Boolean> updated = userManager.updateStatus(userId, 0, DEFAULT_OPERATOR_ID);
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
        String phone = normalizePhone(userAuth.getPhone());
        String email = normalizeEmail(userAuth.getEmail());
        if (phone != null && !isValidPhone(phone)) {
            return Result.failure("手机号格式不正确");
        }
        if (email != null && !isValidEmail(email)) {
            return Result.failure("邮箱格式不正确");
        }
        Result<UserAuthModel> qr = queryByUserId(userAuth.getUserId());
        if (!qr.isSuccess() || qr.getResult() == null) {
            return Result.failure("用户不存在");
        }
        UserDomain d = new UserDomain();
        d.setUserId(userAuth.getUserId());
        d.setPhone(phone);
        d.setEmail(email);
        d.setName(normalizeToNull(userAuth.getName()));
        d.setStatus(userAuth.getStatus());
        d.setBirthYear(userAuth.getBirthYear());
        d.setPersonalFeature(normalizeToNull(userAuth.getPersonalFeature()));
        d.setOccupation(normalizeToNull(userAuth.getOccupation()));
        d.setRoleCodes(userAuth.getRoleCodes());
        Result<Boolean> updated = userManager.updateUser(d);
        if (!updated.isSuccess() || !Boolean.TRUE.equals(updated.getResult())) {
            return Result.failure(updated.getMessage());
        }
        return Result.success(true);
    }
}
