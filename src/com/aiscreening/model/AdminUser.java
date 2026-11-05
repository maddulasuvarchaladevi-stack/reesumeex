package com.aiscreening.model;

/**
 * Represents an HR Administrator account for the Authentication module.
 */
public class AdminUser extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private final String username;
    private final String passwordHash;
    private final String fullName;
    private final String role;

    public AdminUser(String username, String passwordHash, String fullName, String role) {
        super();
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }

    @Override
    public String getEntityType() {
        return "ADMIN_USER";
    }
}
