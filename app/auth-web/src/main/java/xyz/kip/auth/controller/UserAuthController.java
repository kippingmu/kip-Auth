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
import xyz.kip.auth.response.LoginResponse;
import xyz.kip.auth.response.UserAuthResponse;
import xyz.kip.auth.manager.enums.AuthTyepEnum;
import xyz.kip.auth.service.UserAuthService;
import xyz.kip.auth.context.UserContext;
import xyz.kip.auth.service.model.LoginRequestModel;
import xyz.kip.auth.service.model.LoginResponseModel;
import xyz.kip.auth.service.model.RegisterRequestModel;
import xyz.kip.auth.service.model.UserAuthModel;
import xyz.kip.open.common.base.AbstractApiTemplate;
import xyz.kip.open.common.base.Result;

import java.util.regex.Pattern;

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
    private static final String ADMIN_ROLE = "ADMIN";
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern VERIFY_CODE_PATTERN = Pattern.compile("^\\d{6}$");

    @Resource
    private UserAuthService userAuthService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequestRequest req) {
        return new AbstractApiTemplate<LoginRequestRequest, LoginResponse>() {
            @Override
            protected Result<Void> doValidate(LoginRequestRequest request) {
                return validateLoginRequest(request);
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
    public Result<UserAuthResponse> register(@RequestBody RegisterRequest req) {
        return new AbstractApiTemplate<RegisterRequest, UserAuthResponse>() {
            @Override
            protected Result<Void> doValidate(RegisterRequest request) {
                return validateRegisterRequest(request, true, true);
            }

            @Override
            protected Result<UserAuthResponse> execute(RegisterRequest request) {
                Result<UserAuthModel> result = userAuthService.register(toRegisterModel(request));
                if (!result.isSuccess() || result.getResult() == null) {
                    return Result.failure(result.getMessage());
                }
                return Result.success(toUserAuthResponse(result.getResult()));
            }
        }.handle(req);
    }

    @PostMapping("/register/verify-code")
    public Result<String> sendRegisterVerifyCode(@RequestBody RegisterRequest req) {
        return new AbstractApiTemplate<RegisterRequest, String>() {
            @Override
            protected Result<Void> doValidate(RegisterRequest request) {
                return validateRegisterRequest(request, false, false);
            }

            @Override
            protected Result<String> execute(RegisterRequest request) {
                return userAuthService.sendRegisterVerifyCode(toRegisterModel(request));
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
                Result<Void> adminRoleValidation = validateAdminRole();
                if (adminRoleValidation != null) {
                    return adminRoleValidation;
                }
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
                Result<Void> adminRoleValidation = validateAdminRole();
                if (adminRoleValidation != null) {
                    return adminRoleValidation;
                }
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
                Result<Void> adminRoleValidation = validateAdminRole();
                if (adminRoleValidation != null) {
                    return adminRoleValidation;
                }
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
                Result<Void> adminRoleValidation = validateAdminRole();
                if (adminRoleValidation != null) {
                    return adminRoleValidation;
                }
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
                Result<Void> adminRoleValidation = validateAdminRole();
                if (adminRoleValidation != null) {
                    return adminRoleValidation;
                }
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

    @GetMapping("/user/email/{email:.+}")
    public Result<UserAuthResponse> getUserByEmail(@PathVariable String email) {
        return new AbstractApiTemplate<String, UserAuthResponse>() {
            @Override
            protected Result<Void> doValidate(String request) {
                Result<Void> adminRoleValidation = validateAdminRole();
                if (adminRoleValidation != null) {
                    return adminRoleValidation;
                }
                if (isBlank(request)) {
                    return Result.failure("email不能为空");
                }
                return null;
            }

            @Override
            protected Result<UserAuthResponse> execute(String request) {
                Result<UserAuthModel> result = userAuthService.queryByEmail(request);
                if (!result.isSuccess() || result.getResult() == null) {
                    return Result.failure(result.getMessage());
                }
                return Result.success(toUserAuthResponse(result.getResult()));
            }
        }.handle(email);
    }

    @GetMapping("/user/phone/{phone}")
    public Result<UserAuthResponse> getUserByPhone(@PathVariable String phone) {
        return new AbstractApiTemplate<String, UserAuthResponse>() {
            @Override
            protected Result<Void> doValidate(String request) {
                Result<Void> adminRoleValidation = validateAdminRole();
                if (adminRoleValidation != null) {
                    return adminRoleValidation;
                }
                if (isBlank(request)) {
                    return Result.failure("phone不能为空");
                }
                if (!isValidPhone(request)) {
                    return Result.failure("手机号格式不正确");
                }
                return null;
            }

            @Override
            protected Result<UserAuthResponse> execute(String request) {
                Result<UserAuthModel> result = userAuthService.queryByPhone(request);
                if (!result.isSuccess() || result.getResult() == null) {
                    return Result.failure(result.getMessage());
                }
                return Result.success(toUserAuthResponse(result.getResult()));
            }
        }.handle(phone);
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

    private Result<Void> validateAdminRole() {
        String userRoles = UserContext.getUserRoles();
        if (isBlank(userRoles)) {
            return Result.failure("ADMIN role is required");
        }
        String[] roles = userRoles.split(",");
        for (String role : roles) {
            if (ADMIN_ROLE.equalsIgnoreCase(role.trim())) {
                return null;
            }
        }
        return Result.failure("ADMIN role is required");
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
        model.setAuthType(normalizeAuthType(request.getAuthType()));
        model.setPhone(trimToNull(request.getPhone()));
        model.setEmail(trimToNull(request.getEmail()));
        model.setPassword(request.getPassword());
        return model;
    }

    private LoginResponse toLoginResponse(LoginResponseModel model) {
        LoginResponse response = new LoginResponse();
        response.setUserId(model.getUserId());
        response.setEmail(model.getEmail());
        response.setPhone(model.getPhone());
        response.setToken(model.getToken());
        response.setTokenType(model.getTokenType());
        response.setExpiresIn(model.getExpiresIn());
        response.setRoleCodes(model.getRoleCodes());
        return response;
    }

    private RegisterRequestModel toRegisterModel(RegisterRequest request) {
        RegisterRequestModel model = new RegisterRequestModel();
        model.setAuthType(normalizeAuthType(request.getAuthType()));
        model.setPassword(request.getPassword());
        model.setConfirmPassword(request.getConfirmPassword());
        model.setPhone(trimToNull(request.getPhone()));
        model.setEmail(trimToNull(request.getEmail()));
        model.setName(trimToNull(request.getName()));
        model.setVerifyCode(trimToNull(request.getVerifyCode()));
        return model;
    }

    private UserAuthResponse toUserAuthResponse(UserAuthModel model) {
        UserAuthResponse response = new UserAuthResponse();
        response.setUserId(model.getUserId());
        response.setEmail(model.getEmail());
        response.setPhone(model.getPhone());
        response.setName(model.getName());
        response.setStatus(model.getStatus());
        response.setBirthYear(model.getBirthYear());
        response.setPersonalFeature(model.getPersonalFeature());
        response.setOccupation(model.getOccupation());
        response.setRoleCodes(model.getRoleCodes());
        return response;
    }

    private UserAuthModel toUserAuthModel(UserAuthRequest request) {
        UserAuthModel model = new UserAuthModel();
        model.setUserId(request.getUserId());
        model.setEmail(request.getEmail());
        model.setPhone(request.getPhone());
        model.setName(request.getName());
        model.setStatus(request.getStatus());
        model.setBirthYear(request.getBirthYear());
        model.setPersonalFeature(request.getPersonalFeature());
        model.setOccupation(request.getOccupation());
        model.setRoleCodes(request.getRoleCodes());
        return model;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    private Result<Void> validateLoginRequest(LoginRequestRequest request) {
        if (request == null) {
            return Result.failure("请求体不能为空");
        }
        Result<Void> authIdentityValidation = validateAuthIdentityRequest(
                request.getAuthType(), request.getPhone(), request.getEmail(), false);
        if (authIdentityValidation != null) {
            return authIdentityValidation;
        }
        return validatePassword(request.getPassword(), false);
    }

    private Result<Void> validateRegisterRequest(RegisterRequest request, boolean requireName, boolean requireVerifyCode) {
        if (request == null) {
            return Result.failure("请求体不能为空");
        }
        Result<Void> authIdentityValidation = validateAuthIdentityRequest(
                request.getAuthType(), request.getPhone(), request.getEmail(), true);
        if (authIdentityValidation != null) {
            return authIdentityValidation;
        }
        if (requireName && isBlank(request.getName())) {
            return Result.failure("name不能为空");
        }
        Result<Void> passwordValidation = validatePassword(request.getPassword(), true);
        if (passwordValidation != null) {
            return passwordValidation;
        }
        Result<Void> confirmPasswordValidation = validateConfirmPassword(
                request.getPassword(), request.getConfirmPassword());
        if (confirmPasswordValidation != null) {
            return confirmPasswordValidation;
        }
        if (!requireVerifyCode) {
            return null;
        }
        return validateVerifyCode(request.getVerifyCode());
    }

    private Result<Void> validateAuthIdentityRequest(String authType, String phone, String email, boolean validateOptionalIdentity) {
        Result<Void> authTypeValidation = validateAuthType(authType);
        if (authTypeValidation != null) {
            return authTypeValidation;
        }
        AuthTyepEnum parsedAuthType = parseAuthType(authType);
        Result<Void> authIdentityValidation = validateRequiredIdentity(parsedAuthType, phone, email);
        if (authIdentityValidation != null) {
            return authIdentityValidation;
        }
        if (!validateOptionalIdentity) {
            return null;
        }
        return validateOptionalIdentityFormat(phone, email);
    }

    private Result<Void> validateAuthType(String authType) {
        if (isBlank(authType)) {
            return Result.failure("认证类型不能为空");
        }
        if (parseAuthType(authType) == null) {
            return Result.failure("认证类型不正确");
        }
        return null;
    }

    private Result<Void> validatePassword(String password, boolean requireMinLength) {
        if (isBlank(password)) {
            return Result.failure("密码不能为空");
        }
        if (requireMinLength && password.length() < 8) {
            return Result.failure("密码至少8位");
        }
        return null;
    }

    private Result<Void> validateConfirmPassword(String password, String confirmPassword) {
        if (isBlank(confirmPassword)) {
            return Result.failure("确认密码不能为空");
        }
        if (!confirmPassword.equals(password)) {
            return Result.failure("两次输入的密码不一致");
        }
        return null;
    }

    private Result<Void> validateVerifyCode(String verifyCode) {
        if (isBlank(verifyCode)) {
            return Result.failure("验证码不能为空");
        }
        if (!VERIFY_CODE_PATTERN.matcher(verifyCode.trim()).matches()) {
            return Result.failure("验证码必须是6位数字");
        }
        return null;
    }

    private Result<Void> validateRequiredIdentity(AuthTyepEnum authType, String phone, String email) {
        if (AuthTyepEnum.PHONE == authType) {
            if (isBlank(phone)) {
                return Result.failure("手机号不能为空");
            }
            if (!isValidPhone(phone)) {
                return Result.failure("手机号格式不正确");
            }
        }
        if (AuthTyepEnum.EMAIL == authType) {
            if (isBlank(email)) {
                return Result.failure("邮箱不能为空");
            }
            if (!isValidEmail(email)) {
                return Result.failure("邮箱格式不正确");
            }
        }
        return null;
    }

    private Result<Void> validateOptionalIdentityFormat(String phone, String email) {
        if (!isBlank(phone) && !isValidPhone(phone)) {
            return Result.failure("手机号格式不正确");
        }
        if (!isBlank(email) && !isValidEmail(email)) {
            return Result.failure("邮箱格式不正确");
        }
        return null;
    }

    private AuthTyepEnum parseAuthType(String authType) {
        return AuthTyepEnum.fromCode(authType);
    }

    private String normalizeAuthType(String authType) {
        return AuthTyepEnum.codeOf(authType);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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
