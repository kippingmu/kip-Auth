package xyz.kip.auth.domain.model;


import xyz.kip.open.common.base.ToString;

/**
 * @author xiaoshichuan
 * @version 2024-02-29 17:49
 */
public class BizLoginLogModel extends ToString {
    /**
     * 租户id
     */
    private String tenantId;

    /**
     * 业务Code
     */
    private String bizCode;

    /**
     * 登录设备
     */
    private String device;

    /**
     * 登录ip
     */
    private String ip;

    /**
     * MAC地址
     */
    private String mac;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getBizCode() {
        return bizCode;
    }

    public void setBizCode(String bizCode) {
        this.bizCode = bizCode;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }
}
