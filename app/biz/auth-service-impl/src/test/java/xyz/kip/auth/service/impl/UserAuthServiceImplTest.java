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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        request.setUsername("alice");
        request.setPassword("Pass@123456");

        UserDomain user = buildUser("user-1", "alice", "encoded-pass", 1, "default");
        user.setEmail("alice@example.com");
        user.setPhone("13900000001");

        when(userManager.findByUsername("alice", "default")).thenReturn(Result.success(user));
        when(passwordEncoder.matchPassword("Pass@123456", "encoded-pass")).thenReturn(true);
        when(jwtUtil.generateToken(eq("user-1"), eq("alice"), any())).thenReturn("jwt-token");
        when(jwtUtil.getExpiresIn()).thenReturn(3600L);

        Result<LoginResponseModel> result = userAuthService.login(request);

        assertTrue(result.isSuccess());
        assertNotNull(result.getResult());
        assertEquals("jwt-token", result.getResult().getToken());
        assertEquals("Bearer", result.getResult().getTokenType());
        assertEquals(3600L, result.getResult().getExpiresIn());
        verify(userManager).findByUsername("alice", "default");
        verify(cacheManager).set(RedisKeyUtil.userTokenKey("user-1"), "jwt-token", 3600L);

        ArgumentCaptor<Object> cachedUserCaptor = ArgumentCaptor.forClass(Object.class);
        verify(cacheManager).set(eq(RedisKeyUtil.userInfoKey("user-1")), cachedUserCaptor.capture(), eq(3600L));
        Object cachedValue = cachedUserCaptor.getValue();
        assertInstanceOf(UserAuthModel.class, cachedValue);
        UserAuthModel cachedUser = (UserAuthModel) cachedValue;
        assertEquals("user-1", cachedUser.getUserId());
        assertEquals("alice", cachedUser.getUsername());
        assertEquals("default", cachedUser.getTenantId());
    }

    @Test
    void loginShouldFailWhenPasswordDoesNotMatch() {
        LoginRequestModel request = new LoginRequestModel();
        request.setUsername("alice");
        request.setPassword("wrong-password");

        UserDomain user = buildUser("user-1", "alice", "encoded-pass", 1, "default");
        when(userManager.findByUsername("alice", "default")).thenReturn(Result.success(user));
        when(passwordEncoder.matchPassword("wrong-password", "encoded-pass")).thenReturn(false);

        Result<LoginResponseModel> result = userAuthService.login(request);

        assertFalse(result.isSuccess());
        assertEquals("用户名或密码错误", result.getMessage());
    }

    @Test
    void loginShouldFailWhenUserIsDisabled() {
        LoginRequestModel request = new LoginRequestModel();
        request.setUsername("alice");
        request.setPassword("Pass@123456");

        UserDomain user = buildUser("user-1", "alice", "encoded-pass", 0, "default");
        when(userManager.findByUsername("alice", "default")).thenReturn(Result.success(user));

        Result<LoginResponseModel> result = userAuthService.login(request);

        assertFalse(result.isSuccess());
        assertEquals("账号已被禁用", result.getMessage());
    }

    @Test
    void registerShouldCreateUserWithEmailAsAccountAndDefaultPassword() {
        RegisterRequestModel request = new RegisterRequestModel();
        request.setEmail("new-user@example.com");
        request.setPhone("13900000002");
        request.setNickname("newbie");

        when(userManager.findByUsername("new-user@example.com", "default")).thenReturn(Result.failure("not found"));
        when(passwordEncoder.encodePassword("Pass@123456")).thenReturn("encoded-pass");
        when(userManager.createUser(any(UserDomain.class))).thenReturn(Result.success(true));

        Result<UserAuthModel> result = userAuthService.register(request);

        assertTrue(result.isSuccess());
        assertNotNull(result.getResult());
        assertEquals("new-user@example.com", result.getResult().getUsername());
        assertEquals("new-user@example.com", result.getResult().getEmail());
        assertEquals("default", result.getResult().getTenantId());
        assertEquals(1, result.getResult().getStatus());

        ArgumentCaptor<UserDomain> captor = ArgumentCaptor.forClass(UserDomain.class);
        verify(userManager).createUser(captor.capture());
        UserDomain created = captor.getValue();
        assertEquals("new-user@example.com", created.getUsername());
        assertEquals("new-user@example.com", created.getEmail());
        assertEquals("encoded-pass", created.getPassword());
        assertEquals("default", created.getTenantId());
        assertEquals(1, created.getStatus());
        assertNotNull(created.getPhone());
        assertEquals(11, created.getPhone().length());
        assertTrue(created.getPhone().chars().allMatch(Character::isDigit));
        assertEquals(created.getPhone(), result.getResult().getPhone());
        assertNotNull(created.getUserId());
        assertFalse(created.getUserId().isBlank());
        assertNotNull(created.getSalt());
        assertFalse(created.getSalt().isBlank());
    }

    @Test
    void registerShouldFailWhenEmailIsMissing() {
        RegisterRequestModel request = new RegisterRequestModel();

        Result<UserAuthModel> result = userAuthService.register(request);

        assertFalse(result.isSuccess());
        assertEquals("邮箱不能为空", result.getMessage());
    }

    @Test
    void registerShouldFailWhenUserAlreadyExists() {
        RegisterRequestModel request = new RegisterRequestModel();
        request.setEmail("existing-user@example.com");
        request.setTenantId("tenant-a");

        UserDomain existing = buildUser("user-2", "existing-user@example.com", "encoded-pass", 1, "tenant-a");
        when(userManager.findByUsername("existing-user@example.com", "tenant-a")).thenReturn(Result.success(existing));

        Result<UserAuthModel> result = userAuthService.register(request);

        assertFalse(result.isSuccess());
        assertEquals("用户已存在", result.getMessage());
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

        UserDomain user = buildUser("user-2", "kipmu@kip.xyz", "encoded-pass", 1, "default");
        when(jwtUtil.validateToken("jwt-token")).thenReturn(true);
        when(jwtUtil.getUserIdFromToken("jwt-token")).thenReturn("user-2");
        when(jwtUtil.getUsernameFromToken("jwt-token")).thenReturn("kipmu@kip.xyz");
        when(userManager.findByUserId("user-2")).thenReturn(Result.success(user));

        Result<UserAuthModel> result = userAuthService.validateToken("jwt-token");

        assertTrue(result.isSuccess());
        assertNotNull(result.getResult());
        assertEquals("kipmu@kip.xyz", result.getResult().getUsername());
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
