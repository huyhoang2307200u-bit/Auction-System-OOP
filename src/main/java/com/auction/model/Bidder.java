package com.auction.model;

public class Bidder extends User {
    private static final long serialVersionUID = 1L;

    public Bidder() {
        super();
    }

    public Bidder(String username, String displayName, String passwordSalt, String passwordHash) {
        super(username, displayName, passwordSalt, passwordHash, Role.BIDDER);
    }

    @Override
    public String dashboardTitle() {
        return "Bidder Dashboard";
    }
}
