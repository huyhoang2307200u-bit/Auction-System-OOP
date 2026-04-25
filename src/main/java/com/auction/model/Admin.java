package com.auction.model;

public class Admin extends User {
    public Admin() {
        super();
    }

    public Admin(int id, String name, String email, String password, String role) {
        super(id, name, email, password, role);
    }
}