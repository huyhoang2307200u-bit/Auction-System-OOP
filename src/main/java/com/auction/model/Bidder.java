package com.auction.model;

public class Bidder extends User {
    private static final long serialVersionUID = 1L;

    public Bidder() {
        super();
    }

    public Bidder(String username, String displayName, String password) {
        super(username, displayName, password, Role.BIDDER);
    }

    public Bidder(int id, String name, String username, String password, String roleName) {
        super(id, name, username, password, roleName, Role.BIDDER);
    }

    @Override
    public String dashboardTitle() {
        return "Bảng điều khiển người đấu giá";
    }
}