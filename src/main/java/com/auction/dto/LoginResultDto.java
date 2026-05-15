package com.auction.dto;

import com.auction.model.Role;

public class LoginResultDto {
    private String userId;
    private String username;
    private String displayName;
    private Role role;
    private double balance;

    public LoginResultDto() {
    }

    public LoginResultDto(String userId, String username, String displayName, Role role, double balance) {
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
        this.role = role;
        this.balance = balance;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
