package xyz.kip.service;

import xyz.kip.open.common.base.Result;
import xyz.kip.service.model.BizInfoModel;

/**
 * @author xiaoshichuan
 * @version 2024-03-07 15:01
 */
public interface BizInfoService {
    public Result<BizInfoModel> queryByBizCode(String bizCode);
}
