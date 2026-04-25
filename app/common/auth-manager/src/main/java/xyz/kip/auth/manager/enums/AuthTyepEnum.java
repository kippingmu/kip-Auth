package xyz.kip.auth.manager.enums;

/**
 * Supported authentication types.
 *
 * @author xiaoshichuan
 * @version 2026-04-26 02:28, Sun
 */
public enum AuthTyepEnum {
    PHONE("PHONE", "Phone"),
    EMAIL("EMAIL", "Email");

    private final String code;
    private final String description;

    AuthTyepEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static AuthTyepEnum fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        String normalizedCode = code.trim().toUpperCase();
        for (AuthTyepEnum authType : values()) {
            if (authType.code.equals(normalizedCode)) {
                return authType;
            }
        }
        return null;
    }

    public static String codeOf(String code) {
        AuthTyepEnum authType = fromCode(code);
        return authType == null ? null : authType.getCode();
    }
}
