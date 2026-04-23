package xyz.kip.auth.manager;

import xyz.kip.auth.manager.domain.UserDomain;
import xyz.kip.open.common.base.Result;

/**
 * Manager for user operations (wraps DAL).
 * @author xiaoshichuan
 */
public interface UserManager {
    /**
     * Find user by username and tenant.
     */
    Result<UserDomain> findByUsername(String username);

    /**
     * Find C-end user by login type and account.
     */
    Result<UserDomain> findByLoginAccount(String loginType, String account);

    /**
     * Find user by userId.
     */
    Result<UserDomain> findByUserId(String userId);

    /**
     * Create new user.
     */
    Result<Boolean> createUser(UserDomain user);

    /**
     * Update existing user basic fields.
     */
    Result<Boolean> updateUser(UserDomain user);

    /**
     * Update user status.
     */
    Result<Boolean> updateStatus(String userId, Integer status, String updateBy);

    /**
     * Update user password and salt.
     */
    Result<Boolean> updatePassword(String userId, String encodedPassword, String salt, String updateBy);

    /**
     * Delete user by userId.
     */
    Result<Boolean> deleteUser(String userId);
}
