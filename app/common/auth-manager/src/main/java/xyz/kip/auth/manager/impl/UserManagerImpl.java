package xyz.kip.auth.manager.impl;

import org.springframework.stereotype.Service;
import xyz.kip.auth.dal.entity.UserEntity;
import xyz.kip.auth.dal.mapper.UserMapper;
import xyz.kip.auth.manager.UserManager;
import xyz.kip.auth.manager.domain.UserDomain;
import xyz.kip.auth.manager.enums.RoleCodeEnum;
import xyz.kip.open.common.base.Result;

import java.util.List;

/**
 * Implementation of UserManager backed by MyBatis mappers.
 * @author xiaoshichuan
 */
@Service
public class UserManagerImpl implements UserManager {
    private static final Long DEFAULT_OPERATOR_ID = 1L;

    private final UserMapper userMapper;

    public UserManagerImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 根据手机号查询用户
     * @param phone 手机号
     * @return  用户
     */
    @Override
    public Result<UserDomain> findByPhone(String phone) {
        UserEntity entity = userMapper.selectByPhone(phone);
        if (entity == null) {
            return Result.success(null);
        }
        return Result.success(toDomain(entity));
    }

    @Override
    public Result<UserDomain> findByEmail(String email) {
        UserEntity entity = userMapper.selectByEmail(email);
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
        Long id = parseUserId(userId);
        if (id == null) {
            return Result.failure("用户ID格式不正确");
        }
        UserEntity entity = userMapper.selectById(id);
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
    public Result<UserDomain> createUser(UserDomain user) {
        UserEntity e = new UserEntity();
        e.setPhone(user.getPhone());
        e.setEmail(user.getEmail());
        e.setName(user.getName());
        e.setPassword(user.getPassword());
        e.setSalt(user.getSalt());
        e.setStatus(user.getStatus());
        e.setRoleCode(firstRoleCodeOrDefault(user.getRoleCodes()));
        e.setBirthYear(user.getBirthYear());
        e.setPersonalFeature(user.getPersonalFeature());
        e.setOccupation(user.getOccupation());
        e.setCreateBy(DEFAULT_OPERATOR_ID);
        e.setUpdateBy(DEFAULT_OPERATOR_ID);
        int rows = userMapper.insert(e);
        return rows > 0 ? Result.success(toDomain(e)) : Result.failure("插入用户失败");
    }

    /**
     * 更新用户信息
     * @param user 用户领域对象，包含需要更新的用户信息（userId、username、phone、email、status）
     * @return 更新结果，成功返回 Result.success(true)，失败返回 Result.failure("更新用户失败")
     */
    @Override
    public Result<Boolean> updateUser(UserDomain user) {
        Long id = parseUserId(user.getUserId());
        if (id == null) {
            return Result.failure("用户ID格式不正确");
        }
        UserEntity e = new UserEntity();
        e.setId(id);
        e.setPhone(user.getPhone());
        e.setEmail(user.getEmail());
        e.setName(user.getName());
        e.setStatus(user.getStatus());
        e.setRoleCode(firstRoleCodeOrNull(user.getRoleCodes()));
        e.setBirthYear(user.getBirthYear());
        e.setPersonalFeature(user.getPersonalFeature());
        e.setOccupation(user.getOccupation());
        e.setUpdateBy(DEFAULT_OPERATOR_ID);
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
    public Result<Boolean> updateStatus(String userId, Integer status, Long updateBy) {
        Long id = parseUserId(userId);
        if (id == null) {
            return Result.failure("用户ID格式不正确");
        }
        int rows = userMapper.updateStatus(id, status, updateBy);
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
    public Result<Boolean> updatePassword(String userId, String encodedPassword, String salt, Long updateBy) {
        Long id = parseUserId(userId);
        if (id == null) {
            return Result.failure("用户ID格式不正确");
        }
        int rows = userMapper.updatePassword(id, encodedPassword, salt, updateBy);
        return rows > 0 ? Result.success(true) : Result.failure("更新密码失败");
    }

    /**
     * 删除用户
     * @param userId 用户 ID
     * @return 删除结果，成功返回 true，失败返回 false 并附带错误信息
     */
    @Override
    public Result<Boolean> deleteUser(String userId) {
        Long id = parseUserId(userId);
        if (id == null) {
            return Result.failure("用户ID格式不正确");
        }
        int rows = userMapper.deleteById(id);
        return rows > 0 ? Result.success(true) : Result.failure("删除用户失败");
    }

    private static UserDomain toDomain(UserEntity entity) {
        UserDomain d = new UserDomain();
        d.setUserId(entity.getId() == null ? null : entity.getId().toString());
        d.setPhone(entity.getPhone());
        d.setEmail(entity.getEmail());
        d.setName(entity.getName());
        d.setPassword(entity.getPassword());
        d.setSalt(entity.getSalt());
        d.setStatus(entity.getStatus());
        d.setBirthYear(entity.getBirthYear());
        d.setPersonalFeature(entity.getPersonalFeature());
        d.setOccupation(entity.getOccupation());
        d.setRoleCodes(roleCodes(entity.getRoleCode()));
        return d;
    }

    private static String firstRoleCodeOrDefault(List<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty() || roleCodes.get(0) == null || roleCodes.get(0).isBlank()) {
            return RoleCodeEnum.USER.getCode();
        }
        return RoleCodeEnum.codeOf(roleCodes.get(0));
    }

    private static String firstRoleCodeOrNull(List<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty() || roleCodes.get(0) == null || roleCodes.get(0).isBlank()) {
            return null;
        }
        return RoleCodeEnum.codeOf(roleCodes.get(0));
    }

    private static List<String> roleCodes(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            return List.of(RoleCodeEnum.USER.getCode());
        }
        return List.of(RoleCodeEnum.codeOf(roleCode));
    }

    private static Long parseUserId(String userId) {
        try {
            return userId == null || userId.isBlank() ? null : Long.parseLong(userId);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
