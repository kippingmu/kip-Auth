package xyz.kip.service.impl;

import xyz.kip.auth.manager.BizInfoManager;
import xyz.kip.service.convert.BizConvert;
import xyz.kip.service.BizInfoService;
import xyz.kip.auth.manager.domain.BizInfoDomain;
import xyz.kip.open.common.base.Result;
import xyz.kip.open.common.utils.AssertUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xyz.kip.service.model.BizInfoModel;

/**
 * @author xiaoshichuan
 * @version 2024-03-07 15:36
 */
@Service
public class BizInfoServiceImpl implements BizInfoService {
    @Resource
    private BizInfoManager bizInfoManager;
    @Override
    public Result<BizInfoModel> queryByBizCode(String bizCode) {
        AssertUtil.notBlank(bizCode, "bizCode不能为空");
        Result<BizInfoDomain> domainResult = bizInfoManager.queryByBizCode(bizCode);
        AssertUtil.isTrue(domainResult.isSuccess(), "查询业务信息失败");
        BizInfoDomain result = domainResult.getResult();
        if (result == null) {
            return Result.failure("未找到对应的业务信息");
        }
        BizInfoModel bizInfoModel = BizConvert.convertDoMainToModel(result);
        return Result.success(bizInfoModel);
    }
}
