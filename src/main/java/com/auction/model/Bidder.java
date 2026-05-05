package com.auction.model;

public class Bidder extends User {
    public Bidder() {
        super();
    }

    // Truyền thêm "BIDDER" vào làm tham số thứ 5 cho super
    public Bidder(int id, String name, String email, String password, String role) {
        super(id, name, email, password, "BIDDER");
    }
}