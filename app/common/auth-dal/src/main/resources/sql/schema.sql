-- ====================================
-- 认证服务数据库表结构
-- To C 模型：手机号是注册账号，登录支持手机号/邮箱；租户字段仅兼容旧表结构。
-- ====================================

CREATE TABLE IF NOT EXISTS `auth_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
    `username` VARCHAR(64) NOT NULL COMMENT '兼容字段，C端默认写入手机号',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `email` VARCHAR(128) COMMENT '邮箱，用于兼容老用户邮箱登录',
    `password` VARCHAR(128) NOT NULL COMMENT '加密后的密码',
    `salt` VARCHAR(64) NOT NULL COMMENT '密码盐值',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `role_code` VARCHAR(32) NOT NULL DEFAULT 'USER' COMMENT 'C端用户角色：USER/ADMIN',
    `tenant_id` VARCHAR(64) NOT NULL DEFAULT '1' COMMENT '兼容旧数据，C端业务不再使用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) COMMENT '创建人',
    `update_by` VARCHAR(64) COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_email` (`email`),
    KEY `idx_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='C端用户认证表';
