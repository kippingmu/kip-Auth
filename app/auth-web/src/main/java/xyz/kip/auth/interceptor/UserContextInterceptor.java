package xyz.kip.auth.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import xyz.kip.auth.context.UserContext;

/**
 * Interceptor that stores gateway injected user headers in a thread-local context.
 */
@Component
public class UserContextInterceptor implements HandlerInterceptor {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_ROLES = "X-User-Roles";
    private static final String HEADER_USERNAME = "X-Username";
    private static final String HEADER_TENANT_ID = "X-Tenant-Id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userId = request.getHeader(HEADER_USER_ID);
        String userRoles = request.getHeader(HEADER_USER_ROLES);
        String username = request.getHeader(HEADER_USERNAME);
        String tenantId = request.getHeader(HEADER_TENANT_ID);

        if (StringUtils.hasText(userId)) {
            UserContext.setUserId(userId);
        }
        if (StringUtils.hasText(userRoles)) {
            UserContext.setUserRoles(userRoles);
        }
        if (StringUtils.hasText(username)) {
            UserContext.setUsername(username);
        }
        if (StringUtils.hasText(tenantId)) {
            UserContext.setTenantId(tenantId);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
