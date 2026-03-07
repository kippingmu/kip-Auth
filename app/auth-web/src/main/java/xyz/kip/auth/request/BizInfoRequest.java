package xyz.kip.auth.request;

import xyz.kip.open.common.base.ToString;

/**
 * @author xiaoshichuan
 * @version 2024-03-07 16:54
 */
public class BizInfoRequest extends ToString {
    /**
     * 业务Code
     */
    private String bizCode;

    /**
     * 业务联系电话(电话就是账号)
     */
    private String phone;

    /**
     * 租户id
     */
    private String tenantId;

    /**
     * 业务员名字
     */
    private String bizName;

    /**
     * 电子邮件
     */
    private String email;

    /**
     * 加密后的密码
     */
    private String password;

    /**
     * 备注
     */
    private String remark;

    public String getBizCode() {
        return bizCode;
    }

    public void setBizCode(String bizCode) {
        this.bizCode = bizCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getBizName() {
        return bizName;
    }

    public void setBizName(String bizName) {
        this.bizName = bizName;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
