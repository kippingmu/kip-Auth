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
        request.setLoginType("PHONE");
        request.setPhone("13900000001");
        request.setPassword("Pass@123456");

        UserDomain user = buildUser("user-1", "13900000001", "encoded-pass", 1, null);
        user.setPhone("13900000001");
        user.setNickname("Alice");

        when(userManager.findByLoginAccount("PHONE", "13900000001")).thenReturn(Result.success(user));
        when(passwordEncoder.matchPassword("Pass@123456", "encoded-pass")).thenReturn(true);
        when(jwtUtil.generateToken(eq("user-1"), eq("13900000001"), any())).thenReturn("jwt-token");
        when(jwtUtil.getExpiresIn()).thenReturn(3600L);

        Result<LoginResponseModel> result = userAuthService.login(request);

        assertTrue(result.isSuccess());
        assertNotNull(result.getResult());
        assertEquals("jwt-token", result.getResult().getToken());
        assertEquals("Bearer", result.getResult().getTokenType());
        assertEquals(3600L, result.getResult().getExpiresIn());
        verify(userManager).findByLoginAccount("PHONE", "13900000001");
        verify(cacheManager).set(RedisKeyUtil.userTokenKey("user-1"), "jwt-token", 3600L);

        ArgumentCaptor<Object> cachedUserCaptor = ArgumentCaptor.forClass(Object.class);
        verify(cacheManager).set(eq(RedisKeyUtil.userInfoKey("user-1")), cachedUserCaptor.capture(), eq(3600L));
        Object cachedValue = cachedUserCaptor.getValue();
        assertInstanceOf(UserAuthModel.class, cachedValue);
        UserAuthModel cachedUser = (UserAuthModel) cachedValue;
        assertEquals("user-1", cachedUser.getUserId());
        assertEquals("13900000001", cachedUser.getUsername());
        assertEquals("Alice", cachedUser.getNickname());
    }

    @Test
    void loginShouldFailWhenPasswordDoesNotMatch() {
        LoginRequestModel request = new LoginRequestModel();
        request.setLoginType("PHONE");
        request.setPhone("13900000001");
        request.setPassword("wrong-password");

        UserDomain user = buildUser("user-1", "13900000001", "encoded-pass", 1, null);
        when(userManager.findByLoginAccount("PHONE", "13900000001")).thenReturn(Result.success(user));
        when(passwordEncoder.matchPassword("wrong-password", "encoded-pass")).thenReturn(false);

        Result<LoginResponseModel> result = userAuthService.login(request);

        assertFalse(result.isSuccess());
        assertEquals("手机号/邮箱或密码错误", result.getMessage());
    }

    @Test
    void loginShouldAllowEmailAccount() {
        LoginRequestModel request = new LoginRequestModel();
        request.setLoginType("EMAIL");
        request.setEmail("alice@example.com");
        request.setPassword("Pass@123456");

        UserDomain user = buildUser("user-1", "13900000001", "encoded-pass", 1, "1");
        user.setPhone("13900000001");
        user.setEmail("alice@example.com");

        when(userManager.findByLoginAccount("EMAIL", "alice@example.com")).thenReturn(Result.success(user));
        when(passwordEncoder.matchPassword("Pass@123456", "encoded-pass")).thenReturn(true);
        when(jwtUtil.generateToken(eq("user-1"), eq("13900000001"), any())).thenReturn("jwt-token");
        when(jwtUtil.getExpiresIn()).thenReturn(3600L);

        Result<LoginResponseModel> result = userAuthService.login(request);

        assertTrue(result.isSuccess());
        assertNotNull(result.getResult());
        assertEquals("jwt-token", result.getResult().getToken());
        assertEquals("alice@example.com", result.getResult().getEmail());
        verify(userManager).findByLoginAccount("EMAIL", "alice@example.com");
    }

    @Test
    void loginShouldFailWhenUserIsDisabled() {
        LoginRequestModel request = new LoginRequestModel();
        request.setLoginType("PHONE");
        request.setPhone("13900000001");
        request.setPassword("Pass@123456");

        UserDomain user = buildUser("user-1", "13900000001", "encoded-pass", 0, null);
        when(userManager.findByLoginAccount("PHONE", "13900000001")).thenReturn(Result.success(user));

        Result<LoginResponseModel> result = userAuthService.login(request);

        assertFalse(result.isSuccess());
        assertEquals("账号已被禁用", result.getMessage());
    }

    @Test
    void registerShouldCreateUserWithPhonePasswordAndVerifyCode() {
        RegisterRequestModel request = new RegisterRequestModel();
        request.setPhone("13900000002");
        request.setPassword("Pass@123456");
        request.setConfirmPassword("Pass@123456");
        request.setVerifyCode("123456");
        request.setNickname("newbie");

        when(cacheManager.get(RedisKeyUtil.verifyCodeKey("13900000002"), String.class)).thenReturn("123456");
        when(userManager.findByUsername("13900000002")).thenReturn(Result.failure("not found"));
        when(passwordEncoder.encodePassword("Pass@123456")).thenReturn("encoded-pass");
        when(userManager.createUser(any(UserDomain.class))).thenReturn(Result.success(true));

        Result<UserAuthModel> result = userAuthService.register(request);

        assertTrue(result.isSuccess());
        assertNotNull(result.getResult());
        assertEquals("13900000002", result.getResult().getUsername());
        assertEquals("13900000002", result.getResult().getPhone());
        assertEquals("newbie", result.getResult().getNickname());
        assertNull(result.getResult().getTenantId());
        assertEquals(java.util.List.of("USER"), result.getResult().getRoleCodes());
        assertEquals(1, result.getResult().getStatus());

        ArgumentCaptor<UserDomain> captor = ArgumentCaptor.forClass(UserDomain.class);
        verify(userManager).createUser(captor.capture());
        UserDomain created = captor.getValue();
        assertEquals("13900000002", created.getUsername());
        assertEquals("13900000002", created.getPhone());
        assertEquals("newbie", created.getNickname());
        assertEquals("encoded-pass", created.getPassword());
        assertEquals(1, created.getStatus());
        assertEquals(created.getPhone(), result.getResult().getPhone());
        assertNotNull(created.getUserId());
        assertFalse(created.getUserId().isBlank());
        assertNotNull(created.getSalt());
        assertFalse(created.getSalt().isBlank());
        verify(cacheManager).delete(RedisKeyUtil.verifyCodeKey("13900000002"));
    }

    @Test
    void registerShouldFailWhenUserAlreadyExists() {
        RegisterRequestModel request = new RegisterRequestModel();
        request.setPhone("13900000002");
        request.setPassword("Pass@123456");
        request.setConfirmPassword("Pass@123456");
        request.setVerifyCode("123456");

        UserDomain existing = buildUser("user-2", "13900000002", "encoded-pass", 1, null);
        when(cacheManager.get(RedisKeyUtil.verifyCodeKey("13900000002"), String.class)).thenReturn("123456");
        when(userManager.findByUsername("13900000002")).thenReturn(Result.success(existing));

        Result<UserAuthModel> result = userAuthService.register(request);

        assertFalse(result.isSuccess());
        assertEquals("用户已存在", result.getMessage());
    }

    @Test
    void sendRegisterVerifyCodeShouldReturnSixDigitsAndCacheForSixtySeconds() {
        RegisterRequestModel request = new RegisterRequestModel();
        request.setPhone("13900000003");
        request.setPassword("Pass@123456");
        request.setConfirmPassword("Pass@123456");

        when(userManager.findByUsername("13900000003")).thenReturn(Result.success(null));
        when(cacheManager.zRangeByScore(eq(RedisKeyUtil.registerVerifySendKey("13900000003")), anyDouble(), anyDouble(), eq(String.class)))
                .thenReturn(Set.of());

        Result<String> result = userAuthService.sendRegisterVerifyCode(request);

        assertTrue(result.isSuccess());
        assertNotNull(result.getResult());
        assertTrue(result.getResult().matches("\\d{6}"));
        verify(cacheManager).zAdd(eq(RedisKeyUtil.registerVerifySendKey("13900000003")), any(), anyDouble());
        verify(cacheManager).expire(RedisKeyUtil.registerVerifySendKey("13900000003"), 86400L);
        verify(cacheManager).set(RedisKeyUtil.verifyCodeKey("13900000003"), result.getResult(), 60L);
    }

    @Test
    void sendRegisterVerifyCodeShouldRejectHourlyLimit() {
        RegisterRequestModel request = new RegisterRequestModel();
        request.setPhone("13900000003");
        request.setPassword("Pass@123456");
        request.setConfirmPassword("Pass@123456");

        when(userManager.findByUsername("13900000003")).thenReturn(Result.success(null));
        when(cacheManager.zRangeByScore(eq(RedisKeyUtil.registerVerifySendKey("13900000003")), anyDouble(), anyDouble(), eq(String.class)))
                .thenReturn(Set.of())
                .thenReturn(Set.of("a", "b", "c"));

        Result<String> result = userAuthService.sendRegisterVerifyCode(request);

        assertFalse(result.isSuccess());
        assertEquals("同一手机号1小时最多发送3次验证码", result.getMessage());
        verify(cacheManager, never()).set(eq(RedisKeyUtil.verifyCodeKey("13900000003")), any(), eq(60L));
    }

    @Test
    void validateTokenShouldRejectWhenCachedTokenMissingOrOutdated() {
        when(jwtUtil.validateToken("jwt-token")).thenReturn(true);
        when(jwtUtil.getUserIdFromToken("jwt-token")).thenReturn("user-1");
        when(jwtUtil.getUsernameFromToken("jwt-token")).thenReturn("alice");
        when(cacheManager.get(RedisKeyUtil.userTokenKey("user-1"), String.class)).thenReturn("other-token");

        Result<UserAuthModel> result = userAuthService.validateToken("jwt-token");

        assertFalse(result.isSuccess());
        assertEquals("Token revoked or not latest", result.getMessage());
        verify(userManager, never()).findByUserId("user-1");
    }

    @Test
    void validateTokenShouldAllowWhitelistedUsernameToBypassLatestTokenCheck() {
        ReflectionTestUtils.setField(userAuthService, "tokenUserWhitelistCsv", "kipmu@kip.xyz,admin@example.com");

        UserDomain user = buildUser("user-2", "13900000009", "encoded-pass", 1, null);
        user.setPhone("13900000009");
        when(jwtUtil.validateToken("jwt-token")).thenReturn(true);
        when(jwtUtil.getUserIdFromToken("jwt-token")).thenReturn("user-2");
        when(jwtUtil.getUsernameFromToken("jwt-token")).thenReturn("kipmu@kip.xyz");
        when(userManager.findByUserId("user-2")).thenReturn(Result.success(user));

        Result<UserAuthModel> result = userAuthService.validateToken("jwt-token");

        assertTrue(result.isSuccess());
        assertNotNull(result.getResult());
        assertEquals("13900000009", result.getResult().getUsername());
        verify(cacheManager, never()).get(RedisKeyUtil.userTokenKey("user-2"), String.class);
    }

    private static UserDomain buildUser(String userId, String username, String password, Integer status, String tenantId) {
        UserDomain user = new UserDomain();
        user.setUserId(userId);
        user.setUsername(username);
        user.setPassword(password);
        user.setStatus(status);
        user.setTenantId(tenantId);
        return user;
    }
}
