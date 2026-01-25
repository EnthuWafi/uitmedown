package com.enth.uitmedown.model;

public enum UserRole {
    USER("user"),
    ADMIN("admin"),
    SUPERADMIN("superadmin");
    private final String roleName;

    UserRole(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }
}
