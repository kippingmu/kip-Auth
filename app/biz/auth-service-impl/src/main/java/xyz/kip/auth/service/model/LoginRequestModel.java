package xyz.kip.auth.service.model;

import xyz.kip.open.common.base.ToString;

/**
 * 登录请求DTO
 * @author xiaoshichuan
 * @version 2026-02-28
 */
public class LoginRequestModel extends ToString {
    /**
     * 认证类型：PHONE-手机号，EMAIL-邮箱
     */
    private String authType;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 密码
     */
    private String password;

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
