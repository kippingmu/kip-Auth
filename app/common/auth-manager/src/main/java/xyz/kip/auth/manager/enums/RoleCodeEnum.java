package xyz.kip.auth.manager.enums;

/**
 * Role codes stored in auth_user.role_code.
 *
 * @author xiaoshichuan
 * @version 2026-04-23 13:20, Thu
 */
public enum RoleCodeEnum {
    USER("USER", "Regular user"),
    ADMIN("ADMIN", "Administrator");

    private final String code;
    private final String description;

    RoleCodeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static RoleCodeEnum fromCode(String code) {
        if (code == null || code.isBlank()) {
            return USER;
        }
        String normalizedCode = code.trim().toUpperCase();
        for (RoleCodeEnum roleCode : values()) {
            if (roleCode.code.equals(normalizedCode)) {
                return roleCode;
            }
        }
        return USER;
    }

    public static String codeOf(String code) {
        return fromCode(code).getCode();
    }
}
