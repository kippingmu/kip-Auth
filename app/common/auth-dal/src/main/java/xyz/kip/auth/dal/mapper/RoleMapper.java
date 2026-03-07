package xyz.kip.auth.dal.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xyz.kip.auth.dal.entity.RoleEntity;

import java.util.List;

/**
 * 角色数据访问接口.
 *
 * @author xiaoshichuan
 */
@Mapper
public interface RoleMapper {

    /**
     * 根据角色ID查询.
     *
     * @param roleId 角色ID
     * @return 角色实体
     */
    RoleEntity selectByRoleId(@Param("roleId") String roleId);

    /**
     * 根据角色编码和租户ID查询.
     *
     * @param roleCode 角色编码
     * @param tenantId 租户ID
     * @return 角色实体
     */
    RoleEntity selectByRoleCodeAndTenant(@Param("roleCode") String roleCode, @Param("tenantId") String tenantId);

    /**
     * 根据用户ID查询角色列表.
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<RoleEntity> selectByUserId(@Param("userId") String userId);

    /**
     * 插入角色.
     *
     * @param role 角色实体
     * @return 影响行数
     */
    int insert(RoleEntity role);

    /**
     * 更新角色.
     *
     * @param role 角色实体
     * @return 影响行数
     */
    int update(RoleEntity role);

    /**
     * 更新角色状态.
     *
     * @param roleId   角色ID
     * @param status   状态
     * @param updateBy 更新人
     * @return 影响行数
     */
    int updateStatus(@Param("roleId") String roleId, @Param("status") Integer status, @Param("updateBy") String updateBy);

    /**
     * 删除角色.
     *
     * @param roleId 角色ID
     * @return 影响行数
     */
    int deleteByRoleId(@Param("roleId") String roleId);

    /**
     * 根据租户ID查询角色列表.
     *
     * @param tenantId 租户ID
     * @return 角色列表
     */
    List<RoleEntity> selectByTenantId(@Param("tenantId") String tenantId);
}
