package xyz.kip.auth.manager;

import xyz.kip.auth.manager.domain.UserDomain;
import xyz.kip.open.common.base.Result;

/**
 * Manager for user operations (wraps DAL).
 * @author xiaoshichuan
 */
public interface UserManager {
    /**
     * Find user by phone.
     */
    Result<UserDomain> findByPhone(String phone);

    /**
     * Find user by email.
     */
    Result<UserDomain> findByEmail(String email);

    /**
     * Find user by userId.
     */
    Result<UserDomain> findByUserId(String userId);

    /**
     * Create new user.
     */
    Result<UserDomain> createUser(UserDomain user);

    /**
     * Update existing user basic fields.
     */
    Result<Boolean> updateUser(UserDomain user);

    /**
     * Update user status.
     */
    Result<Boolean> updateStatus(String userId, Integer status, Long updateBy);

    /**
     * Update user password and salt.
     */
    Result<Boolean> updatePassword(String userId, String encodedPassword, String salt, Long updateBy);

    /**
     * Delete user by userId.
     */
    Result<Boolean> deleteUser(String userId);
}
