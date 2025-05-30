package org.glue.glue_be.user.entity;

public enum UserRole {
    ROLE_USER(0, "USER"),
    ROLE_ADMIN(1, "ADMIN");

    private final int code;
    private final String label;

    UserRole(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public String getAuthority() {
        return name();
    }
}
