package xyz.kip.auth.manager.impl;

import org.springframework.stereotype.Service;
import xyz.kip.auth.dal.entity.BizInfoEntity;
import xyz.kip.auth.dal.mapper.BizInfoMapper;
import xyz.kip.auth.manager.BizInfoManager;
import xyz.kip.auth.manager.domain.BizInfoDomain;
import xyz.kip.open.common.base.Result;

/**
 * Implementation of BizInfoManager backed by MyBatis mappers.
 */
@Service
public class BizInfoManagerImpl implements BizInfoManager {
    private final BizInfoMapper bizInfoMapper;

    public BizInfoManagerImpl(BizInfoMapper bizInfoMapper) {
        this.bizInfoMapper = bizInfoMapper;
    }

    @Override
    public Result<BizInfoDomain> queryByBizCode(String bizCode) {
        BizInfoEntity entity = bizInfoMapper.selectByBizCode(bizCode);
        if (entity == null) {
            return Result.success(null);
        }
        BizInfoDomain d = new BizInfoDomain();
        d.setBizCode(entity.getBizCode());
        d.setPhone(entity.getPhone());
        d.setTenantId(entity.getTenantId());
        d.setBizName(entity.getBizName());
        d.setEmail(entity.getEmail());
        d.setPassword(entity.getPassword());
        d.setRemark(entity.getRemark());
        return Result.success(d);
    }
}

