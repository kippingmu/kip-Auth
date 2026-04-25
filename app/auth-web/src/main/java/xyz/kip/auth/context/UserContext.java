package xyz.kip.auth.context;

/**
 * Thread-local user context holder for gateway injected headers.
 */
public class UserContext {
    private static final ThreadLocal<String> USER_ID_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_ROLES_HOLDER = new ThreadLocal<>();

    public static void setUserId(String userId) {
        USER_ID_HOLDER.set(userId);
    }

    public static String getUserId() {
        return USER_ID_HOLDER.get();
    }

    public static void setUserRoles(String userRoles) {
        USER_ROLES_HOLDER.set(userRoles);
    }

    public static String getUserRoles() {
        return USER_ROLES_HOLDER.get();
    }

    public static void clear() {
        USER_ID_HOLDER.remove();
        USER_ROLES_HOLDER.remove();
    }
}
