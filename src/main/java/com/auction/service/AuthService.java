package com.auction.service;

import com.auction.model.Admin;
import com.auction.model.Bidder;
import com.auction.model.User;
import java.util.ArrayList;
import java.util.List;

public class AuthService {
    // Nơi lưu trữ tài khoản tạm thời khi chưa nối Socket Server
    private static final List<User> users = new ArrayList<>();

    static {
        // Khởi tạo sẵn 2 tài khoản mặc định để bạn test cho nhanh (khỏi cần đăng ký lại mỗi lần chạy app)
        // Tài khoản 1: Username = admin | Pass = 123
        users.add(new Admin(1, "Quản trị viên", "admin", "123", "ADMIN"));
        // Tài khoản 2: Username = user | Pass = 123
        users.add(new Bidder(2, "Người đấu giá", "user", "123", "BIDDER"));
    }

    public static boolean register(User user) {
        // Kiểm tra xem tên đăng nhập đã tồn tại chưa
        for (User u : users) {
            if (u.getUsername().equals(user.getUsername())) {
                return false; // Trùng tên đăng nhập -> Đăng ký thất bại
            }
        }
        users.add(user);
        return true;
    }

    public static User login(String username, String password) {
        for (User u : users) {
            // So sánh tên đăng nhập và mật khẩu
            if (u.getUsername().equals(username) && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null; // Không tìm thấy hoặc sai mật khẩu
    }
}