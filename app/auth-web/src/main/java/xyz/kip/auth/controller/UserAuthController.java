package xyz.kip.auth.controller;

import jakarta.annotation.Resource;
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
import xyz.kip.auth.request.RegisterRequest;
import xyz.kip.auth.request.UserAuthRequest;
import xyz.kip.auth.resonse.LoginResponse;
import xyz.kip.auth.resonse.UserAuthResponse;
import xyz.kip.auth.service.UserAuthService;
import xyz.kip.auth.service.model.LoginRequestModel;
import xyz.kip.auth.service.model.LoginResponseModel;
import xyz.kip.auth.service.model.RegisterRequestModel;
import xyz.kip.auth.service.model.UserAuthModel;
import xyz.kip.open.common.base.AbstractApiTemplate;
import xyz.kip.open.common.base.Result;

/**
 * 用户认证控制器
 *
 * @author xiaoshichuan
 * @version 2026-02-28
 */
@RestController
@RequestMapping("/api/auth")
public class UserAuthController {

    private static final String BEARER_PREFIX = "Bearer ";

    @Resource
    private UserAuthService userAuthService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequestRequest req) {
        return new AbstractApiTemplate<LoginRequestRequest, LoginResponse>() {
            @Override
            protected Result<Void> doValidate(LoginRequestRequest request) {
                if (request == null) {
                    return Result.failure("请求体不能为空");
                }
                if (isBlank(request.getUsername())) {
                    return Result.failure("用户名不能为空");
                }
                if (isBlank(request.getPassword())) {
                    return Result.failure("密码不能为空");
                }
                return null;
            }

            @Override
            protected Result<LoginResponse> execute(LoginRequestRequest request) {
                Result<LoginResponseModel> result = userAuthService.login(toLoginModel(request));
                if (!result.isSuccess() || result.getResult() == null) {
                    return Result.failure(result.getMessage());
                }
                return Result.success(toLoginResponse(result.getResult()));
            }
        }.handle(req);
    }

    @PostMapping("/register")
    public Result<UserAuthRequest> register(@RequestBody RegisterRequest req) {
        return new AbstractApiTemplate<RegisterRequest, UserAuthRequest>() {
            @Override
            protected Result<Void> doValidate(RegisterRequest request) {
                if (request == null) {
                    return Result.failure("请求体不能为空");
                }
                if (isBlank(request.getEmail())) {
                    return Result.failure("邮箱不能为空");
                }
                return null;
            }

            @Override
            protected Result<UserAuthRequest> execute(RegisterRequest request) {
                Result<UserAuthModel> result = userAuthService.register(toRegisterModel(request));
                if (!result.isSuccess() || result.getResult() == null) {
                    return Result.failure(result.getMessage());
                }
                return Result.success(toUserAuthRequest(result.getResult()));
            }
        }.handle(req);
    }

    @GetMapping("/user/info")
    public Result<UserAuthModel> getUserInfo(@RequestHeader(value = "Authorization", required = false) String token) {
        return new AbstractApiTemplate<String, UserAuthModel>() {
            @Override
            protected Result<Void> doValidate(String request) {
                return validateTokenHeader(request);
            }

            @Override
            protected Result<UserAuthModel> execute(String request) {
                return userAuthService.validateToken(normalizeToken(request));
            }
        }.handle(token);
    }

    @PostMapping("/token/validate")
    public Result<UserAuthModel> validateToken(@RequestHeader(value = "Authorization", required = false) String token) {
        return new AbstractApiTemplate<String, UserAuthModel>() {
            @Override
            protected Result<Void> doValidate(String request) {
                return validateTokenHeader(request);
            }

            @Override
            protected Result<UserAuthModel> execute(String request) {
                return userAuthService.validateToken(normalizeToken(request));
            }
        }.handle(token);
    }

    @PostMapping("/password/change")
    public Result<Boolean> changePassword(
            @RequestParam String userId,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        return new AbstractApiTemplate<ChangePasswordCommand, Boolean>() {
            @Override
            protected Result<Void> doValidate(ChangePasswordCommand request) {
                if (isBlank(request.getUserId())) {
                    return Result.failure("userId不能为空");
                }
                if (isBlank(request.getOldPassword())) {
                    return Result.failure("oldPassword不能为空");
                }
                if (isBlank(request.getNewPassword())) {
                    return Result.failure("newPassword不能为空");
                }
                return null;
            }

            @Override
            protected Result<Boolean> execute(ChangePasswordCommand request) {
                return userAuthService.changePassword(request.getUserId(), request.getOldPassword(), request.getNewPassword());
            }
        }.handle(new ChangePasswordCommand(userId, oldPassword, newPassword));
    }

    @PostMapping("/password/reset")
    public Result<Boolean> resetPassword(
            @RequestParam String userId,
            @RequestParam String newPassword) {
        return new AbstractApiTemplate<ResetPasswordCommand, Boolean>() {
            @Override
            protected Result<Void> doValidate(ResetPasswordCommand request) {
                if (isBlank(request.getUserId())) {
                    return Result.failure("userId不能为空");
                }
                if (isBlank(request.getNewPassword())) {
                    return Result.failure("newPassword不能为空");
                }
                return null;
            }

            @Override
            protected Result<Boolean> execute(ResetPasswordCommand request) {
                return userAuthService.resetPassword(request.getUserId(), request.getNewPassword());
            }
        }.handle(new ResetPasswordCommand(userId, newPassword));
    }

    @PostMapping("/user/enable")
    public Result<Boolean> enableUser(@RequestParam String userId) {
        return new AbstractApiTemplate<String, Boolean>() {
            @Override
            protected Result<Void> doValidate(String request) {
                if (isBlank(request)) {
                    return Result.failure("userId不能为空");
                }
                return null;
            }

            @Override
            protected Result<Boolean> execute(String request) {
                return userAuthService.enableUser(request);
            }
        }.handle(userId);
    }

    @PostMapping("/user/disable")
    public Result<Boolean> disableUser(@RequestParam String userId) {
        return new AbstractApiTemplate<String, Boolean>() {
            @Override
            protected Result<Void> doValidate(String request) {
                if (isBlank(request)) {
                    return Result.failure("userId不能为空");
                }
                return null;
            }

            @Override
            protected Result<Boolean> execute(String request) {
                return userAuthService.disableUser(request);
            }
        }.handle(userId);
    }

    @DeleteMapping("/user/{userId}")
    public Result<Boolean> deleteUser(@PathVariable String userId) {
        return new AbstractApiTemplate<String, Boolean>() {
            @Override
            protected Result<Void> doValidate(String request) {
                if (isBlank(request)) {
                    return Result.failure("userId不能为空");
                }
                return null;
            }

            @Override
            protected Result<Boolean> execute(String request) {
                return userAuthService.deleteUser(request);
            }
        }.handle(userId);
    }

    @GetMapping("/user/{userId}")
    public Result<UserAuthResponse> getUser(@PathVariable String userId) {
        return new AbstractApiTemplate<String, UserAuthResponse>() {
            @Override
            protected Result<Void> doValidate(String request) {
                if (isBlank(request)) {
                    return Result.failure("userId不能为空");
                }
                return null;
            }

            @Override
            protected Result<UserAuthResponse> execute(String request) {
                Result<UserAuthModel> result = userAuthService.queryByUserId(request);
                if (!result.isSuccess() || result.getResult() == null) {
                    return Result.failure(result.getMessage());
                }
                return Result.success(toUserAuthResponse(result.getResult()));
            }
        }.handle(userId);
    }

    @GetMapping("/user/name/{username}")
    public Result<UserAuthResponse> getUserByName(@PathVariable String username) {
        return new AbstractApiTemplate<String, UserAuthResponse>() {
            @Override
            protected Result<Void> doValidate(String request) {
                if (isBlank(request)) {
                    return Result.failure("username不能为空");
                }
                return null;
            }

            @Override
            protected Result<UserAuthResponse> execute(String request) {
                Result<UserAuthModel> result = userAuthService.queryByUsername(request);
                if (!result.isSuccess() || result.getResult() == null) {
                    return Result.failure(result.getMessage());
                }
                return Result.success(toUserAuthResponse(result.getResult()));
            }
        }.handle(username);
    }

    @PutMapping("/user")
    public Result<Boolean> updateUser(@RequestBody UserAuthRequest req) {
        return new AbstractApiTemplate<UserAuthRequest, Boolean>() {
            @Override
            protected Result<Void> doValidate(UserAuthRequest request) {
                if (request == null) {
                    return Result.failure("请求体不能为空");
                }
                if (isBlank(request.getUserId())) {
                    return Result.failure("userId不能为空");
                }
                return null;
            }

            @Override
            protected Result<Boolean> execute(UserAuthRequest request) {
                return userAuthService.updateUser(toUserAuthModel(request));
            }
        }.handle(req);
    }

    @GetMapping("/health")
    public Result<String> health() {
        return new AbstractApiTemplate<String, String>() {
            @Override
            protected Result<Void> doValidate(String request) {
                return null;
            }

            @Override
            protected Result<String> execute(String request) {
                return Result.success("Auth service is running");
            }
        }.handle("health");
    }

    private Result<Void> validateTokenHeader(String token) {
        if (isBlank(token)) {
            return Result.failure("缺少authorization token");
        }
        return null;
    }

    private String normalizeToken(String token) {
        String normalized = token.trim();
        if (normalized.startsWith(BEARER_PREFIX)) {
            return normalized.substring(BEARER_PREFIX.length());
        }
        return normalized;
    }

    private LoginRequestModel toLoginModel(LoginRequestRequest request) {
        LoginRequestModel model = new LoginRequestModel();
        model.setUsername(request.getUsername());
        model.setPassword(request.getPassword());
        model.setVerifyCode(request.getVerifyCode());
        model.setTenantId(request.getTenantId());
        return model;
    }

    private LoginResponse toLoginResponse(LoginResponseModel model) {
        LoginResponse response = new LoginResponse();
        response.setUserId(model.getUserId());
        response.setUsername(model.getUsername());
        response.setEmail(model.getEmail());
        response.setPhone(model.getPhone());
        response.setNickname(model.getNickname());
        response.setToken(model.getToken());
        response.setTokenType(model.getTokenType());
        response.setExpiresIn(model.getExpiresIn());
        return response;
    }

    private RegisterRequestModel toRegisterModel(RegisterRequest request) {
        RegisterRequestModel model = new RegisterRequestModel();
        model.setUsername(request.getUsername());
        model.setPassword(request.getPassword());
        model.setConfirmPassword(request.getConfirmPassword());
        model.setEmail(request.getEmail());
        model.setPhone(request.getPhone());
        model.setNickname(request.getNickname());
        model.setVerifyCode(request.getVerifyCode());
        model.setTenantId(request.getTenantId());
        return model;
    }

    private UserAuthRequest toUserAuthRequest(UserAuthModel model) {
        UserAuthRequest response = new UserAuthRequest();
        response.setUserId(model.getUserId());
        response.setUsername(model.getUsername());
        response.setEmail(model.getEmail());
        response.setPhone(model.getPhone());
        response.setNickname(model.getNickname());
        response.setStatus(model.getStatus());
        response.setTenantId(model.getTenantId());
        return response;
    }

    private UserAuthResponse toUserAuthResponse(UserAuthModel model) {
        UserAuthResponse response = new UserAuthResponse();
        response.setUserId(model.getUserId());
        response.setUsername(model.getUsername());
        response.setEmail(model.getEmail());
        response.setPhone(model.getPhone());
        response.setNickname(model.getNickname());
        response.setStatus(model.getStatus());
        response.setTenantId(model.getTenantId());
        return response;
    }

    private UserAuthModel toUserAuthModel(UserAuthRequest request) {
        UserAuthModel model = new UserAuthModel();
        model.setUserId(request.getUserId());
        model.setUsername(request.getUsername());
        model.setEmail(request.getEmail());
        model.setPhone(request.getPhone());
        model.setNickname(request.getNickname());
        model.setStatus(request.getStatus());
        model.setTenantId(request.getTenantId());
        return model;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static final class ChangePasswordCommand {
        private final String userId;
        private final String oldPassword;
        private final String newPassword;

        private ChangePasswordCommand(String userId, String oldPassword, String newPassword) {
            this.userId = userId;
            this.oldPassword = oldPassword;
            this.newPassword = newPassword;
        }

        private String getUserId() {
            return userId;
        }

        private String getOldPassword() {
            return oldPassword;
        }

        private String getNewPassword() {
            return newPassword;
        }
    }

    private static final class ResetPasswordCommand {
        private final String userId;
        private final String newPassword;

        private ResetPasswordCommand(String userId, String newPassword) {
            this.userId = userId;
            this.newPassword = newPassword;
        }

        private String getUserId() {
            return userId;
        }

        private String getNewPassword() {
            return newPassword;
        }
    }
}
