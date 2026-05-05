package com.auction.model;

public class Admin extends Bidder { // Sửa từ User thành Bidder

    public Admin() {
        super();
    }

    // Constructor này sẽ gọi super() của lớp Bidder
    public Admin(int id, String name, String email, String password, String role) {
        super(id, name, email, password, role);
    }

    // Bạn có thể thêm các phương thức riêng của Admin ở đây nếu cần
}