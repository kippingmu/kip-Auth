package xyz.kip.auth.builder;

import jakarta.servlet.http.HttpServletRequest;
import xyz.kip.open.common.dto.ApiHeader;

/**
 * @author xiaoshichuan
 * @version 2026-03-05 19:16
 */
public class ApiHeaderBuilder {
    private ApiHeaderBuilder() {}

    public static ApiHeader build(HttpServletRequest req) {
        ApiHeader h = new ApiHeader();
        h.setTraceId(req.getHeader("X-Trace-Id"));
        h.setAppId(req.getHeader("X-App-Id"));
        h.setUserId(req.getHeader("X-User-Id"));
        h.setTenantId(req.getHeader("X-Tenant-Id"));
        h.setTs(System.currentTimeMillis());
        return h;
    }
}
