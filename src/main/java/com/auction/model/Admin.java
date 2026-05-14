package com.auction.model;

public class Admin extends User {
    private static final long serialVersionUID = 1L;

    public Admin() {
        super();
    }

    public Admin(String username, String displayName, String passwordSalt, String passwordHash) {
        super(username, displayName, passwordSalt, passwordHash, Role.ADMIN);
    }

    public Admin(int id, String name, String username, String password, String roleName) {
        super(id, name, username, password, roleName, Role.ADMIN);
    }

    @Override
    public String dashboardTitle() {
        return "Bảng điều khiển quản trị viên";
    }
}