-- ====================================
-- 认证服务数据库表结构
-- ====================================

-- 用户表
CREATE TABLE IF NOT EXISTS `auth_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `email` VARCHAR(128) COMMENT '邮箱',
    `password` VARCHAR(128) NOT NULL COMMENT '加密后的密码',
    `salt` VARCHAR(64) NOT NULL COMMENT '密码盐值',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `role_code` VARCHAR(32) NOT NULL DEFAULT 'USER' COMMENT 'C端用户角色：USER/ADMIN',
    `tenant_id` VARCHAR(64) NOT NULL DEFAULT 'default' COMMENT '兼容旧数据，C端业务不再使用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) COMMENT '创建人',
    `update_by` VARCHAR(64) COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 业务员表
CREATE TABLE IF NOT EXISTS `auth_biz_info` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `biz_code` VARCHAR(64) NOT NULL COMMENT '业务员编码',
    `biz_name` VARCHAR(64) NOT NULL COMMENT '业务员名称',
    `phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
    `email` VARCHAR(128) COMMENT '电子邮件',
    `password` VARCHAR(128) NOT NULL COMMENT '加密后的密码',
    `salt` VARCHAR(64) NOT NULL COMMENT '密码盐值',
    `tenant_id` VARCHAR(64) NOT NULL COMMENT '租户ID',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `remark` VARCHAR(512) COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) COMMENT '创建人',
    `update_by` VARCHAR(64) COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_biz_code` (`biz_code`),
    UNIQUE KEY `uk_phone_tenant` (`phone`, `tenant_id`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务员信息表';

-- 角色表
CREATE TABLE IF NOT EXISTS `auth_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_id` VARCHAR(64) NOT NULL COMMENT '角色ID',
    `role_code` VARCHAR(64) NOT NULL COMMENT '角色编码',
    `role_name` VARCHAR(64) NOT NULL COMMENT '角色名称',
    `tenant_id` VARCHAR(64) NOT NULL COMMENT '租户ID',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `remark` VARCHAR(512) COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) COMMENT '创建人',
    `update_by` VARCHAR(64) COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_id` (`role_id`),
    UNIQUE KEY `uk_role_code_tenant` (`role_code`, `tenant_id`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS `auth_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `permission_id` VARCHAR(64) NOT NULL COMMENT '权限ID',
    `permission_code` VARCHAR(128) NOT NULL COMMENT '权限编码',
    `permission_name` VARCHAR(128) NOT NULL COMMENT '权限名称',
    `resource_type` VARCHAR(32) NOT NULL COMMENT '资源类型：menu-菜单，button-按钮，api-接口',
    `resource_path` VARCHAR(256) COMMENT '资源路径',
    `parent_id` VARCHAR(64) COMMENT '父权限ID',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `remark` VARCHAR(512) COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_id` (`permission_id`),
    UNIQUE KEY `uk_permission_code` (`permission_code`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS `auth_user_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
    `role_id` VARCHAR(64) NOT NULL COMMENT '角色ID',
    `tenant_id` VARCHAR(64) NOT NULL COMMENT '租户ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` VARCHAR(64) COMMENT '创建人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS `auth_role_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_id` VARCHAR(64) NOT NULL COMMENT '角色ID',
    `permission_id` VARCHAR(64) NOT NULL COMMENT '权限ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` VARCHAR(64) COMMENT '创建人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 租户表
CREATE TABLE IF NOT EXISTS `auth_tenant` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` VARCHAR(64) NOT NULL COMMENT '租户ID',
    `tenant_code` VARCHAR(64) NOT NULL COMMENT '租户编码',
    `tenant_name` VARCHAR(128) NOT NULL COMMENT '租户名称',
    `contact_name` VARCHAR(64) COMMENT '联系人',
    `contact_phone` VARCHAR(20) COMMENT '联系电话',
    `contact_email` VARCHAR(128) COMMENT '联系邮箱',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `expire_time` DATETIME COMMENT '过期时间',
    `remark` VARCHAR(512) COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_id` (`tenant_id`),
    UNIQUE KEY `uk_tenant_code` (`tenant_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户表';

-- 登录日志表
CREATE TABLE IF NOT EXISTS `auth_login_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
    `username` VARCHAR(64) COMMENT '用户名',
    `tenant_id` VARCHAR(64) NOT NULL COMMENT '租户ID',
    `login_type` VARCHAR(32) NOT NULL COMMENT '登录类型：password-密码，sms-短信，oauth-第三方',
    `login_ip` VARCHAR(64) COMMENT '登录IP',
    `login_location` VARCHAR(128) COMMENT '登录地点',
    `browser` VARCHAR(64) COMMENT '浏览器',
    `os` VARCHAR(64) COMMENT '操作系统',
    `status` TINYINT NOT NULL COMMENT '登录状态：0-失败，1-成功',
    `message` VARCHAR(256) COMMENT '提示信息',
    `login_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS `auth_operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
    `username` VARCHAR(64) COMMENT '用户名',
    `tenant_id` VARCHAR(64) NOT NULL COMMENT '租户ID',
    `operation_type` VARCHAR(32) NOT NULL COMMENT '操作类型',
    `operation_name` VARCHAR(128) NOT NULL COMMENT '操作名称',
    `method` VARCHAR(256) COMMENT '方法名',
    `request_url` VARCHAR(512) COMMENT '请求URL',
    `request_method` VARCHAR(16) COMMENT '请求方式',
    `request_params` TEXT COMMENT '请求参数',
    `response_result` TEXT COMMENT '响应结果',
    `operation_ip` VARCHAR(64) COMMENT '操作IP',
    `operation_location` VARCHAR(128) COMMENT '操作地点',
    `status` TINYINT NOT NULL COMMENT '操作状态：0-失败，1-成功',
    `error_msg` TEXT COMMENT '错误信息',
    `cost_time` INT COMMENT '耗时(毫秒)',
    `operation_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_operation_time` (`operation_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';
