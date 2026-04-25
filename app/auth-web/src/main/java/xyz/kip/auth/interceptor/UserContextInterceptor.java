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

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userId = request.getHeader(HEADER_USER_ID);
        String userRoles = request.getHeader(HEADER_USER_ROLES);

        if (StringUtils.hasText(userId)) {
            UserContext.setUserId(userId);
        }
        if (StringUtils.hasText(userRoles)) {
            UserContext.setUserRoles(userRoles);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
