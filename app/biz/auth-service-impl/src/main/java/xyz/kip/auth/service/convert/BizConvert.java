package xyz.kip.auth.service.convert;

import xyz.kip.auth.manager.domain.BizInfoDomain;
import xyz.kip.auth.service.model.BizInfoModel;

/**
 * @author xiaoshichuan
 * @version 2024-03-07 16:10
 */
public class BizConvert {
    public static BizInfoModel convertDoMainToModel(BizInfoDomain bizInfoDomain){
        BizInfoModel bizInfoModel = new BizInfoModel();
        bizInfoModel.setBizCode(bizInfoDomain.getBizCode());
        bizInfoModel.setPhone(bizInfoDomain.getPhone());
        bizInfoModel.setTenantId(bizInfoDomain.getTenantId());
        bizInfoModel.setBizName(bizInfoDomain.getBizName());
        bizInfoModel.setEmail(bizInfoDomain.getEmail());
        bizInfoModel.setPassword(bizInfoDomain.getPassword());
        bizInfoModel.setRemark(bizInfoDomain.getRemark());
        return bizInfoModel;
    }
}
