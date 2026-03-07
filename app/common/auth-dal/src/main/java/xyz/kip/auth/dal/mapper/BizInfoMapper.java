package xyz.kip.auth.dal.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xyz.kip.auth.dal.entity.BizInfoEntity;

import java.util.List;

/**
 * 业务员数据访问接口.
 *
 * @author xiaoshichuan
 */
@Mapper
public interface BizInfoMapper {

    /**
     * 根据业务员编码查询.
     *
     * @param bizCode 业务员编码
     * @return 业务员实体
     */
    BizInfoEntity selectByBizCode(@Param("bizCode") String bizCode);

    /**
     * 根据手机号和租户ID查询.
     *
     * @param phone    手机号
     * @param tenantId 租户ID
     * @return 业务员实体
     */
    BizInfoEntity selectByPhoneAndTenant(@Param("phone") String phone, @Param("tenantId") String tenantId);

    /**
     * 插入业务员.
     *
     * @param bizInfo 业务员实体
     * @return 影响行数
     */
    int insert(BizInfoEntity bizInfo);

    /**
     * 更新业务员.
     *
     * @param bizInfo 业务员实体
     * @return 影响行数
     */
    int update(BizInfoEntity bizInfo);

    /**
     * 更新业务员状态.
     *
     * @param bizCode  业务员编码
     * @param status   状态
     * @param updateBy 更新人
     * @return 影响行数
     */
    int updateStatus(@Param("bizCode") String bizCode, @Param("status") Integer status, @Param("updateBy") String updateBy);

    /**
     * 更新业务员密码.
     *
     * @param bizCode  业务员编码
     * @param password 新密码
     * @param salt     新盐值
     * @param updateBy 更新人
     * @return 影响行数
     */
    int updatePassword(@Param("bizCode") String bizCode, @Param("password") String password,
                       @Param("salt") String salt, @Param("updateBy") String updateBy);

    /**
     * 删除业务员.
     *
     * @param bizCode 业务员编码
     * @return 影响行数
     */
    int deleteByBizCode(@Param("bizCode") String bizCode);

    /**
     * 根据租户ID查询业务员列表.
     *
     * @param tenantId 租户ID
     * @return 业务员列表
     */
    List<BizInfoEntity> selectByTenantId(@Param("tenantId") String tenantId);

    /**
     * 统计租户下的业务员数量.
     *
     * @param tenantId 租户ID
     * @return 业务员数量
     */
    int countByTenantId(@Param("tenantId") String tenantId);
}
