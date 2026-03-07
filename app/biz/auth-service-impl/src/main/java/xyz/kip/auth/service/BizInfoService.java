package xyz.kip.auth.service;

import xyz.kip.open.common.base.Result;
import xyz.kip.auth.service.model.BizInfoModel;

/**
 * @author xiaoshichuan
 * @version 2024-03-07 15:01
 */
public interface BizInfoService {
    public Result<BizInfoModel> queryByBizCode(String bizCode);
}
