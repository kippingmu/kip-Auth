-- ====================================
-- 认证服务初始化数据
-- ====================================

-- 默认管理员用户。密码需要按当前 PasswordEncoder 生成后替换 CHANGE_ME。
INSERT INTO `auth_user` (`user_id`, `phone`, `nickname`, `password`, `salt`, `status`, `role_code`, `create_by`, `update_by`)
VALUES ('user_admin', '13800138000', '系统管理员', 'CHANGE_ME', 'CHANGE_ME', 1, 'ADMIN', 'system', 'system');
