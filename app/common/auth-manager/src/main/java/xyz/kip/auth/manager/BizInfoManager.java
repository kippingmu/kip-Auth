package xyz.kip.auth.manager;

import xyz.kip.auth.manager.domain.BizInfoDomain;
import xyz.kip.open.common.base.Result;

/**
 * Manager for business info operations.
 * @author xiaoshichuan
 */
public interface BizInfoManager {
    Result<BizInfoDomain> queryByBizCode(String bizCode);
}
