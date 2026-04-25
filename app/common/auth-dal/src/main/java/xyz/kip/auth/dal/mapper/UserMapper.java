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
     * 根据主键查询用户.
     *
     * @param id 主键ID
     * @return 用户实体
     */
    UserEntity selectById(@Param("id") Long id);

    /**
     * 根据手机号查询用户.
     *
     * @param phone 手机号
     * @return 用户实体
     */
    UserEntity selectByPhone(@Param("phone") String phone);

    /**
     * 根据邮箱查询用户.
     *
     * @param email 邮箱
     * @return 用户实体
     */
    UserEntity selectByEmail(@Param("email") String email);

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
     * @param id       主键ID
     * @param status   状态
     * @param updateBy 更新人
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status, @Param("updateBy") Long updateBy);

    /**
     * 更新用户密码.
     *
     * @param id       主键ID
     * @param password 新密码
     * @param salt     新盐值
     * @param updateBy 更新人
     * @return 影响行数
     */
    int updatePassword(@Param("id") Long id, @Param("password") String password,
                       @Param("salt") String salt, @Param("updateBy") Long updateBy);

    /**
     * 删除用户.
     *
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

}
