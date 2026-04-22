-- ====================================
-- 认证服务初始化数据
-- ====================================

-- 插入默认租户
INSERT INTO `auth_tenant` (`tenant_id`, `tenant_code`, `tenant_name`, `contact_name`, `contact_phone`, `status`, `remark`)
VALUES ('default', 'DEFAULT', '默认租户', '系统管理员', '13800138000', 1, '系统默认租户');

-- 插入默认角色
INSERT INTO `auth_role` (`role_id`, `role_code`, `role_name`, `tenant_id`, `status`, `remark`, `create_by`)
VALUES
('role_admin', 'ADMIN', '系统管理员', 'default', 1, '系统管理员角色，拥有所有权限', 'system'),
('role_user', 'USER', '普通用户', 'default', 1, '普通用户角色', 'system'),
('role_biz', 'BIZ', '业务员', 'default', 1, '业务员角色', 'system');

-- 插入默认权限
INSERT INTO `auth_permission` (`permission_id`, `permission_code`, `permission_name`, `resource_type`, `resource_path`, `status`, `sort_order`)
VALUES
-- 用户管理
('perm_user_view', 'user:view', '查看用户', 'api', '/api/user/view', 1, 1),
('perm_user_add', 'user:add', '添加用户', 'api', '/api/user/add', 1, 2),
('perm_user_edit', 'user:edit', '编辑用户', 'api', '/api/user/edit', 1, 3),
('perm_user_delete', 'user:delete', '删除用户', 'api', '/api/user/delete', 1, 4),
-- 角色管理
('perm_role_view', 'role:view', '查看角色', 'api', '/api/role/view', 1, 5),
('perm_role_add', 'role:add', '添加角色', 'api', '/api/role/add', 1, 6),
('perm_role_edit', 'role:edit', '编辑角色', 'api', '/api/role/edit', 1, 7),
('perm_role_delete', 'role:delete', '删除角色', 'api', '/api/role/delete', 1, 8),
-- 业务员管理
('perm_biz_view', 'biz:view', '查看业务员', 'api', '/api/biz/view', 1, 9),
('perm_biz_add', 'biz:add', '添加业务员', 'api', '/api/biz/add', 1, 10),
('perm_biz_edit', 'biz:edit', '编辑业务员', 'api', '/api/biz/edit', 1, 11),
('perm_biz_delete', 'biz:delete', '删除业务员', 'api', '/api/biz/delete', 1, 12);

-- 为管理员角色分配所有权限
INSERT INTO `auth_role_permission` (`role_id`, `permission_id`, `create_by`)
SELECT 'role_admin', `permission_id`, 'system'
FROM `auth_permission`;

-- 为普通用户角色分配查看权限
INSERT INTO `auth_role_permission` (`role_id`, `permission_id`, `create_by`)
VALUES
('role_user', 'perm_user_view', 'system'),
('role_user', 'perm_role_view', 'system'),
('role_user', 'perm_biz_view', 'system');

-- 插入默认管理员用户 (密码: admin123, 需要在应用层加密)
INSERT INTO `auth_user` (`user_id`, `username`, `phone`, `email`, `password`, `salt`, `status`, `role_code`, `tenant_id`, `create_by`)
VALUES ('user_admin', 'admin', '13800138000', 'admin@example.com', 'CHANGE_ME', 'CHANGE_ME', 1, 'ADMIN', 'default', 'system');

-- 为管理员用户分配管理员角色
INSERT INTO `auth_user_role` (`user_id`, `role_id`, `tenant_id`, `create_by`)
VALUES ('user_admin', 'role_admin', 'default', 'system');
