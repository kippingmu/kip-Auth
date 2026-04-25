package xyz.kip.auth.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.kip.auth.manager.UserManager;
import xyz.kip.auth.manager.cache.CacheManager;
import xyz.kip.auth.manager.domain.UserDomain;
import xyz.kip.auth.manager.util.RedisKeyUtil;
import xyz.kip.auth.service.model.LoginRequestModel;
import xyz.kip.auth.service.model.LoginResponseModel;
import xyz.kip.auth.service.model.RegisterRequestModel;
import xyz.kip.auth.service.model.UserAuthModel;
import xyz.kip.auth.service.utils.JwtUtil;
import xyz.kip.auth.service.utils.PasswordEncoder;
import xyz.kip.open.common.base.Result;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAuthServiceImplTest {

    private static final String PHONE_REGISTER_IDENTIFIER = "phone:13900000002";
    private static final String EMAIL_REGISTER_IDENTIFIER = "email:new@example.com";
    private static final String PHONE_ONLY_REGISTER_IDENTIFIER = "phone:13900000003";

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserManager userManager;

    @Mock
    private CacheManager cacheManager;

    @InjectMocks
    private UserAuthServiceImpl userAuthService;

    @Test
    void loginShouldReturnTokenAndCacheUserInfo() {
        LoginRequestModel request = new LoginRequestModel();
        request.setAuthType("PHONE");
        request.setPhone("13900000001");
        request.setPassword("Pass@123456");

        UserDomain user = buildUser("1001", "13900000001", "alice@example.com", "Alice", "encoded-pass", 1);
        user.setPhone("13900000001");

        when(userManager.findByPhone("13900000001")).thenReturn(Result.success(user));
        when(passwordEncoder.matchPassword("Pass@123456", "encoded-pass")).thenReturn(true);
        when(jwtUtil.generateToken(eq("1001"), eq("alice@example.com"), any())).thenReturn("jwt-token");
        when(jwtUtil.getExpiresIn()).thenReturn(3600L);

        Result<LoginResponseModel> result = userAuthService.login(request);

        assertTrue(result.isSuccess());
        assertNotNull(result.getResult());
        assertEquals("jwt-token", result.getResult().getToken());
        assertEquals("Bearer", result.getResult().getTokenType());
        assertEquals(3600L, result.getResult().getExpiresIn());
        assertEquals("alice@example.com", result.getResult().getEmail());
        verify(userManager).findByPhone("13900000001");
        verify(cacheManager).set(RedisKeyUtil.userTokenKey("1001"), "jwt-token", 3600L);

        ArgumentCaptor<Object> cachedUserCaptor = ArgumentCaptor.forClass(Object.class);
        verify(cacheManager).set(eq(RedisKeyUtil.userInfoKey("1001")), cachedUserCaptor.capture(), eq(3600L));
        Object cachedValue = cachedUserCaptor.getValue();
        assertInstanceOf(UserAuthModel.class, cachedValue);
        UserAuthModel cachedUser = (UserAuthModel) cachedValue;
        assertEquals("1001", cachedUser.getUserId());
        assertEquals("alice@example.com", cachedUser.getEmail());
        assertEquals("Alice", cachedUser.getName());
    }

    @Test
    void loginShouldFailWhenPasswordDoesNotMatch() {
        LoginRequestModel request = new LoginRequestModel();
        request.setAuthType("PHONE");
        request.setPhone("13900000001");
        request.setPassword("wrong-password");

        UserDomain user = buildUser("1001", "13900000001", null, "Alice", "encoded-pass", 1);
        when(userManager.findByPhone("13900000001")).thenReturn(Result.success(user));
        when(passwordEncoder.matchPassword("wrong-password", "encoded-pass")).thenReturn(false);

        Result<LoginResponseModel> result = userAuthService.login(request);

        assertFalse(result.isSuccess());
        assertEquals("手机号/邮箱或密码错误", result.getMessage());
    }

    @Test
    void loginShouldAllowEmailAccount() {
        LoginRequestModel request = new LoginRequestModel();
        request.setAuthType("EMAIL");
        request.setEmail("alice@example.com");
        request.setPassword("Pass@123456");

        UserDomain user = buildUser("1001", "13900000001", "alice@example.com", "Alice", "encoded-pass", 1);

        when(userManager.findByEmail("alice@example.com")).thenReturn(Result.success(user));
        when(passwordEncoder.matchPassword("Pass@123456", "encoded-pass")).thenReturn(true);
        when(jwtUtil.generateToken(eq("1001"), eq("alice@example.com"), any())).thenReturn("jwt-token");
        when(jwtUtil.getExpiresIn()).thenReturn(3600L);

        Result<LoginResponseModel> result = userAuthService.login(request);

        assertTrue(result.isSuccess());
        assertNotNull(result.getResult());
        assertEquals("jwt-token", result.getResult().getToken());
        assertEquals("alice@example.com", result.getResult().getEmail());
        verify(userManager).findByEmail("alice@example.com");
    }

    @Test
    void loginShouldFailWhenUserIsDisabled() {
        LoginRequestModel request = new LoginRequestModel();
        request.setAuthType("PHONE");
        request.setPhone("13900000001");
        request.setPassword("Pass@123456");

        UserDomain user = buildUser("1001", "13900000001", null, "Alice", "encoded-pass", 0);
        when(userManager.findByPhone("13900000001")).thenReturn(Result.success(user));

        Result<LoginResponseModel> result = userAuthService.login(request);

        assertFalse(result.isSuccess());
        assertEquals("账号已被禁用", result.getMessage());
    }

    @Test
    void registerShouldCreateUserWithNewSchemaFields() {
        RegisterRequestModel request = new RegisterRequestModel();
        request.setAuthType("PHONE");
        request.setPhone("13900000002");
        request.setEmail("new@example.com");
        request.setName("New User");
        request.setPassword("Pass@123456");
        request.setConfirmPassword("Pass@123456");
        request.setVerifyCode("123456");

        when(cacheManager.get(RedisKeyUtil.verifyCodeKey(PHONE_REGISTER_IDENTIFIER), String.class)).thenReturn("123456");
        when(userManager.findByPhone("13900000002")).thenReturn(Result.success(null));
        when(userManager.findByEmail("new@example.com")).thenReturn(Result.success(null));
        when(passwordEncoder.encodePassword("Pass@123456")).thenReturn("encoded-pass");
        when(userManager.createUser(any(UserDomain.class))).thenAnswer(invocation -> {
            UserDomain created = invocation.getArgument(0);
            created.setUserId("1002");
            return Result.success(created);
        });

        Result<UserAuthModel> result = userAuthService.register(request);

        assertTrue(result.isSuccess());
        assertNotNull(result.getResult());
        assertEquals("1002", result.getResult().getUserId());
        assertEquals("13900000002", result.getResult().getPhone());
        assertEquals("new@example.com", result.getResult().getEmail());
        assertEquals("New User", result.getResult().getName());
        assertEquals(java.util.List.of("USER"), result.getResult().getRoleCodes());
        assertEquals(1, result.getResult().getStatus());

        ArgumentCaptor<UserDomain> captor = ArgumentCaptor.forClass(UserDomain.class);
        verify(userManager).createUser(captor.capture());
        UserDomain created = captor.getValue();
        assertEquals("13900000002", created.getPhone());
        assertEquals("new@example.com", created.getEmail());
        assertEquals("New User", created.getName());
        assertEquals("encoded-pass", created.getPassword());
        assertEquals(1, created.getStatus());
        assertEquals(created.getPhone(), result.getResult().getPhone());
        assertNotNull(created.getSalt());
        assertFalse(created.getSalt().isBlank());
        verify(cacheManager).delete(RedisKeyUtil.verifyCodeKey(PHONE_REGISTER_IDENTIFIER));
    }

    @Test
    void registerShouldFailWhenEmailAlreadyExists() {
        RegisterRequestModel request = new RegisterRequestModel();
        request.setAuthType("EMAIL");
        request.setPhone("13900000002");
        request.setEmail("new@example.com");
        request.setName("New User");
        request.setPassword("Pass@123456");
        request.setConfirmPassword("Pass@123456");
        request.setVerifyCode("123456");

        UserDomain existing = buildUser("1003", "13900000088", "new@example.com", "Taken", "encoded-pass", 1);
        when(cacheManager.get(RedisKeyUtil.verifyCodeKey(EMAIL_REGISTER_IDENTIFIER), String.class)).thenReturn("123456");
        when(userManager.findByPhone("13900000002")).thenReturn(Result.success(null));
        when(userManager.findByEmail("new@example.com")).thenReturn(Result.success(existing));

        Result<UserAuthModel> result = userAuthService.register(request);

        assertFalse(result.isSuccess());
        assertEquals("邮箱已注册", result.getMessage());
    }

    @Test
    void sendRegisterVerifyCodeShouldReturnSixDigitsAndCacheForSixtySeconds() {
        RegisterRequestModel request = new RegisterRequestModel();
        request.setAuthType("EMAIL");
        request.setEmail("new@example.com");
        request.setPassword("Pass@123456");
        request.setConfirmPassword("Pass@123456");

        when(userManager.findByEmail("new@example.com")).thenReturn(Result.success(null));
        when(cacheManager.zRangeByScore(eq(RedisKeyUtil.registerVerifySendKey(EMAIL_REGISTER_IDENTIFIER)), anyDouble(), anyDouble(), eq(String.class)))
                .thenReturn(Set.of());

        Result<String> result = userAuthService.sendRegisterVerifyCode(request);

        assertTrue(result.isSuccess());
        assertNotNull(result.getResult());
        assertTrue(result.getResult().matches("\\d{6}"));
        verify(cacheManager).zAdd(eq(RedisKeyUtil.registerVerifySendKey(EMAIL_REGISTER_IDENTIFIER)), any(), anyDouble());
        verify(cacheManager).expire(RedisKeyUtil.registerVerifySendKey(EMAIL_REGISTER_IDENTIFIER), 86400L);
        verify(cacheManager).set(RedisKeyUtil.verifyCodeKey(EMAIL_REGISTER_IDENTIFIER), result.getResult(), 60L);
    }

    @Test
    void sendRegisterVerifyCodeShouldRejectHourlyLimit() {
        RegisterRequestModel request = new RegisterRequestModel();
        request.setAuthType("PHONE");
        request.setPhone("13900000003");
        request.setPassword("Pass@123456");
        request.setConfirmPassword("Pass@123456");

        when(userManager.findByPhone("13900000003")).thenReturn(Result.success(null));
        when(cacheManager.zRangeByScore(eq(RedisKeyUtil.registerVerifySendKey(PHONE_ONLY_REGISTER_IDENTIFIER)), anyDouble(), anyDouble(), eq(String.class)))
                .thenReturn(Set.of())
                .thenReturn(Set.of("a", "b", "c"));

        Result<String> result = userAuthService.sendRegisterVerifyCode(request);

        assertFalse(result.isSuccess());
        assertEquals("同一邮箱/手机号1小时最多发送3次验证码", result.getMessage());
        verify(cacheManager, never()).set(eq(RedisKeyUtil.verifyCodeKey(PHONE_ONLY_REGISTER_IDENTIFIER)), any(), eq(60L));
    }

    @Test
    void validateTokenShouldRejectWhenCachedTokenMissingOrOutdated() {
        when(jwtUtil.validateToken("jwt-token")).thenReturn(true);
        when(jwtUtil.getUserIdFromToken("jwt-token")).thenReturn("1001");
        when(jwtUtil.getUsernameFromToken("jwt-token")).thenReturn("alice@example.com");
        when(cacheManager.get(RedisKeyUtil.userTokenKey("1001"), String.class)).thenReturn("other-token");

        Result<UserAuthModel> result = userAuthService.validateToken("jwt-token");

        assertFalse(result.isSuccess());
        assertEquals("Token revoked or not latest", result.getMessage());
        verify(userManager, never()).findByUserId("1001");
    }

    @Test
    void validateTokenShouldAllowWhitelistedIdentityToBypassLatestTokenCheck() {
        ReflectionTestUtils.setField(userAuthService, "tokenUserWhitelistCsv", "kipmu@kip.xyz,admin@example.com");

        UserDomain user = buildUser("1004", "13900000009", "kipmu@kip.xyz", "Admin", "encoded-pass", 1);
        when(jwtUtil.validateToken("jwt-token")).thenReturn(true);
        when(jwtUtil.getUserIdFromToken("jwt-token")).thenReturn("1004");
        when(jwtUtil.getUsernameFromToken("jwt-token")).thenReturn("kipmu@kip.xyz");
        when(userManager.findByUserId("1004")).thenReturn(Result.success(user));

        Result<UserAuthModel> result = userAuthService.validateToken("jwt-token");

        assertTrue(result.isSuccess());
        assertNotNull(result.getResult());
        assertEquals("kipmu@kip.xyz", result.getResult().getEmail());
        verify(cacheManager, never()).get(RedisKeyUtil.userTokenKey("1004"), String.class);
    }

    private static UserDomain buildUser(String userId, String phone, String email, String name, String password, Integer status) {
        UserDomain user = new UserDomain();
        user.setUserId(userId);
        user.setPhone(phone);
        user.setEmail(email);
        user.setName(name);
        user.setPassword(password);
        user.setStatus(status);
        return user;
    }
}
