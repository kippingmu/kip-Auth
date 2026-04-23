package xyz.kip.auth.manager.impl;

import org.springframework.stereotype.Service;
import xyz.kip.auth.dal.entity.UserEntity;
import xyz.kip.auth.dal.mapper.UserMapper;
import xyz.kip.auth.manager.UserManager;
import xyz.kip.auth.manager.domain.UserDomain;
import xyz.kip.open.common.base.Result;

import java.util.List;

/**
 * Implementation of UserManager backed by MyBatis mappers.
 * @author xiaoshichuan
 */
@Service
public class UserManagerImpl implements UserManager {
    private final UserMapper userMapper;

    public UserManagerImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return  用户
     */
    @Override
    public Result<UserDomain> findByUsername(String username) {
        UserEntity entity = userMapper.selectByUsername(username);
        if (entity == null) {
            return Result.success(null);
        }
        return Result.success(toDomain(entity));
    }

    @Override
    public Result<UserDomain> findByLoginAccount(String loginType, String account) {
        UserEntity entity = userMapper.selectByLoginAccount(loginType, account);
        if (entity == null) {
            return Result.success(null);
        }
        return Result.success(toDomain(entity));
    }

    /**
     * 根据用户ID查询用户
     * @param userId 用户ID
     * @return  用户
     */
    @Override
    public Result<UserDomain> findByUserId(String userId) {
        UserEntity entity = userMapper.selectByUserId(userId);
        if (entity == null) {
            return Result.success(null);
        }
        return Result.success(toDomain(entity));
    }

    /**
     * 创建新用户
     * @param user 用户领域对象，包含用户信息
     * @return 创建结果，成功返回 true，失败返回 false 并附带错误信息
     */
    @Override
    public Result<Boolean> createUser(UserDomain user) {
        UserEntity e = new UserEntity();
        e.setUserId(user.getUserId());
        e.setUsername(user.getUsername());
        e.setPhone(user.getPhone());
        e.setEmail(user.getEmail());
        e.setPassword(user.getPassword());
        e.setSalt(user.getSalt());
        e.setStatus(user.getStatus());
        e.setRoleCode(firstRoleCode(user.getRoleCodes()));
        e.setTenantId("1");
        e.setCreateBy("system");
        e.setUpdateBy("system");
        int rows = userMapper.insert(e);
        return rows > 0 ? Result.success(true) : Result.failure("插入用户失败");
    }

    /**
     * 更新用户信息
     * @param user 用户领域对象，包含需要更新的用户信息（userId、username、phone、email、status）
     * @return 更新结果，成功返回 Result.success(true)，失败返回 Result.failure("更新用户失败")
     */
    @Override
    public Result<Boolean> updateUser(UserDomain user) {
        UserEntity e = new UserEntity();
        e.setUserId(user.getUserId());
        e.setUsername(user.getUsername());
        e.setPhone(user.getPhone());
        e.setEmail(user.getEmail());
        e.setStatus(user.getStatus());
        e.setRoleCode(firstRoleCode(user.getRoleCodes()));
        e.setUpdateBy("system");
        int rows = userMapper.update(e);
        return rows > 0 ? Result.success(true) : Result.failure("更新用户失败");
    }

    /**
     * 更新用户状态
     * @param userId 用户 ID
     * @param status 新的用户状态
     * @param updateBy 更新人标识
     * @return 更新结果，成功返回 true，失败返回 false 并附带错误信息
     */
    @Override
    public Result<Boolean> updateStatus(String userId, Integer status, String updateBy) {
        int rows = userMapper.updateStatus(userId, status, updateBy);
        return rows > 0 ? Result.success(true) : Result.failure("更新状态失败");
    }

    /**
     * 更新用户密码
     * @param userId 用户 ID
     * @param encodedPassword 加密后的密码
     * @param salt 密码盐值
     * @param updateBy 更新人标识
     * @return 更新结果，成功返回 true，失败返回 false 并附带错误信息
     */
    @Override
    public Result<Boolean> updatePassword(String userId, String encodedPassword, String salt, String updateBy) {
        int rows = userMapper.updatePassword(userId, encodedPassword, salt, updateBy);
        return rows > 0 ? Result.success(true) : Result.failure("更新密码失败");
    }

    /**
     * 删除用户
     * @param userId 用户 ID
     * @return 删除结果，成功返回 true，失败返回 false 并附带错误信息
     */
    @Override
    public Result<Boolean> deleteUser(String userId) {
        int rows = userMapper.deleteByUserId(userId);
        return rows > 0 ? Result.success(true) : Result.failure("删除用户失败");
    }

    private static UserDomain toDomain(UserEntity entity) {
        UserDomain d = new UserDomain();
        d.setUserId(entity.getUserId());
        d.setPhone(entity.getPhone());
        d.setUsername(entity.getPhone());
        d.setEmail(entity.getEmail());
        d.setPassword(entity.getPassword());
        d.setSalt(entity.getSalt());
        d.setStatus(entity.getStatus());
        d.setTenantId(entity.getTenantId());
        d.setRoleCodes(roleCodes(entity.getRoleCode()));
        return d;
    }

    private static String firstRoleCode(List<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty() || roleCodes.get(0) == null || roleCodes.get(0).isBlank()) {
            return "USER";
        }
        return roleCodes.get(0).trim().toUpperCase();
    }

    private static List<String> roleCodes(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            return List.of("USER");
        }
        return List.of(roleCode.trim().toUpperCase());
    }
}
