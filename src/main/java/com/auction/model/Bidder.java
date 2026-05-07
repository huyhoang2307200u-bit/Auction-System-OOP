package com.auction.model;

public class Bidder extends User {
    public Bidder() { super(); }

    public Bidder(int id, String name, String email, String password, String role) {
        // Truyền biến role nhận được lên lớp cha User
        super(id, name, email, password, role);
    }
}