package xyz.kip.auth.application.service;

import xyz.kip.auth.domain.model.BizInfoModel;
import xyz.kip.open.common.base.Result;

/**
 * @author xiaoshichuan
 * @version 2024-03-07 15:01
 */
public interface BizInfoService {
    public Result<BizInfoModel> queryByBizCode(String bizCode);
}
