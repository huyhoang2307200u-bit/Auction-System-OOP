package com.auction.model;

public class Seller extends User {
    public Seller() {
        super();
    }

    public Seller(int id, String name, String email, String password) {
        // Truyền thêm "SELLER" vào tham số cuối cùng
        super(id, name, email, password, "SELLER");
    }
}