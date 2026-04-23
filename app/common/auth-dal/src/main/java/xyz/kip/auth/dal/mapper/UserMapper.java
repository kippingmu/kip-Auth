package xyz.kip.auth.dal.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xyz.kip.auth.dal.entity.UserEntity;

/**
 * 用户数据访问接口.
 *
 * @author xiaoshichuan
 */
@Mapper
public interface UserMapper {

    /**
     * 根据用户ID查询用户.
     *
     * @param userId 用户ID
     * @return 用户实体
     */
    UserEntity selectByUserId(@Param("userId") String userId);

    /**
     * 根据手机号查询 C 端用户.
     *
     * @param username 手机号
     * @return 用户实体
     */
    UserEntity selectByUsername(@Param("username") String username);

    /**
     * 根据登录类型和账号查询 C 端用户.
     *
     * @param loginType 登录类型：PHONE/EMAIL
     * @param account 手机号或邮箱
     * @return 用户实体
     */
    UserEntity selectByLoginAccount(@Param("loginType") String loginType, @Param("account") String account);

    /**
     * 插入用户.
     *
     * @param user 用户实体
     * @return 影响行数
     */
    int insert(UserEntity user);

    /**
     * 更新用户.
     *
     * @param user 用户实体
     * @return 影响行数
     */
    int update(UserEntity user);

    /**
     * 更新用户状态.
     *
     * @param userId   用户ID
     * @param status   状态
     * @param updateBy 更新人
     * @return 影响行数
     */
    int updateStatus(@Param("userId") String userId, @Param("status") Integer status, @Param("updateBy") String updateBy);

    /**
     * 更新用户密码.
     *
     * @param userId   用户ID
     * @param password 新密码
     * @param salt     新盐值
     * @param updateBy 更新人
     * @return 影响行数
     */
    int updatePassword(@Param("userId") String userId, @Param("password") String password,
                       @Param("salt") String salt, @Param("updateBy") String updateBy);

    /**
     * 删除用户.
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteByUserId(@Param("userId") String userId);

}
