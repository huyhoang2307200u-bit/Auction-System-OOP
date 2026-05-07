package com.auction.model;

public class Seller extends User {
    private static final long serialVersionUID = 1L;

    public Seller() {
        super();
    }

    public Seller(String username, String displayName, String passwordSalt, String passwordHash) {
        super(username, displayName, passwordSalt, passwordHash, Role.SELLER);
    }

    @Override
    public String dashboardTitle() {
        return "Seller Dashboard";
    }
}
