package com.auction.server.security;

public class AuthenticatedUser {
    private final int userId;
    private final String username;
    private final String role;

    public AuthenticatedUser(int userId, String username, String role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public boolean hasRole(String requiredRole) {
        return requiredRole != null && requiredRole.equalsIgnoreCase(role);
    }
}
