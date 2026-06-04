package com.auction.model;

public class Seller extends User {
    private static final long serialVersionUID = 1L;

    public Seller() {
        super();
    }

    public Seller(String username, String displayName, String password) {
        super(username, displayName, password, Role.SELLER);
    }

    public Seller(int id, String name, String username, String password, String roleName) {
        super(id, name, username, password, roleName, Role.SELLER);
    }

    @Override
    public String dashboardTitle() {
        return "Bảng điều khiển người bán";
    }
}