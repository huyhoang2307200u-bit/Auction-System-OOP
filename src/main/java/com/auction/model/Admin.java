package com.auction.model;

public class Admin extends User {
    private static final long serialVersionUID = 1L;

    public Admin() {
        super();
    }

    public Admin(String username, String displayName, String passwordSalt, String passwordHash) {
        super(username, displayName, passwordSalt, passwordHash, Role.ADMIN);
    }

    @Override
    public String dashboardTitle() {
        return "Admin Dashboard";
    }
}
