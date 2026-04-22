-- ====================================
-- 认证服务数据库表结构
-- To C 模型：手机号是唯一账号，注册/登录都围绕 auth_user。
-- ====================================

CREATE TABLE IF NOT EXISTS `auth_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `nickname` VARCHAR(64) COMMENT '昵称',
    `password` VARCHAR(128) NOT NULL COMMENT '加密后的密码',
    `salt` VARCHAR(64) NOT NULL COMMENT '密码盐值',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `role_code` VARCHAR(32) NOT NULL DEFAULT 'USER' COMMENT 'C端用户角色：USER/ADMIN',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) COMMENT '创建人',
    `update_by` VARCHAR(64) COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='C端用户认证表';
