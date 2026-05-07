package com.auction.model;

public abstract class User extends Entity {
    private static final long serialVersionUID = 1L;

    private String username;
    private String displayName;
    private String passwordSalt;
    private String passwordHash;
    private Role role;

    protected User() {
        super();
    }

    protected User(String username, String displayName, String passwordSalt, String passwordHash, Role role) {
        super();
        this.username = username;
        this.displayName = displayName;
        this.passwordSalt = passwordSalt;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public abstract String dashboardTitle();

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}
