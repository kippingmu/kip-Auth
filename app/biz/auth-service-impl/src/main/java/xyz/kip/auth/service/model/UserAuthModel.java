package xyz.kip.auth.service.model;

import xyz.kip.open.common.base.ToString;

import java.util.List;

/**
 * 用户认证信息DTO
 * @author xiaoshichuan
 * @version 2026-02-28
 */
public class UserAuthModel extends ToString {
    /**
     * 用户ID
     */
    private String userId;

    /**
     * 密码
     */
    private String password;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 姓名
     */
    private String name;

    /**
     * 账号状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 出生年份
     */
    private Integer birthYear;

    /**
     * 个人特征
     */
    private String personalFeature;

    /**
     * 职业
     */
    private String occupation;

    private List<String> roleCodes;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(Integer birthYear) {
        this.birthYear = birthYear;
    }

    public String getPersonalFeature() {
        return personalFeature;
    }

    public void setPersonalFeature(String personalFeature) {
        this.personalFeature = personalFeature;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public List<String> getRoleCodes() {
        return roleCodes;
    }

    public void setRoleCodes(List<String> roleCodes) {
        this.roleCodes = roleCodes;
    }
}
