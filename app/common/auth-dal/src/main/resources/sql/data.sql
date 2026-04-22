-- ====================================
-- 认证服务初始化数据
-- ====================================

-- 默认管理员用户。密码需要按当前 PasswordEncoder 生成后替换 CHANGE_ME。
INSERT INTO `auth_user` (`user_id`, `username`, `phone`, `email`, `password`, `salt`, `status`, `role_code`, `tenant_id`, `create_by`, `update_by`)
VALUES ('user_admin', '13800138000', '13800138000', 'admin@example.com', 'CHANGE_ME', 'CHANGE_ME', 1, 'ADMIN', 'default', 'system', 'system');
