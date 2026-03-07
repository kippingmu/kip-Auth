package xyz.kip.auth.manager;

import xyz.kip.auth.manager.domain.BizInfoDomain;
import xyz.kip.open.common.base.Result;

/**
 * Manager for business info operations.
 * @author xiaoshichuan
 */
public interface BizInfoManager {
    /**
     * Query business info by biz code.
     * @param bizCode Business code.
     * @return result
     */
    Result<BizInfoDomain> queryByBizCode(String bizCode);
}
