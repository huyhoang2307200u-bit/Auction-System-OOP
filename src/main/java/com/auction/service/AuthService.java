package com.auction.service;

import com.auction.model.User;
import java.util.ArrayList;
import java.util.List;

public class AuthService {
    // Tạm thời lưu user trong list, sau này có thể kết nối database
    private static List<User> users = new ArrayList<>();

    public static boolean register(User user) {
        // Kiểm tra trùng lặp email/username ở đây
        users.add(user);
        return true;
    }

    public static User login(String email, String password) {
        for (User u : users) {
            if (u.getEmail().equals(email) && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null;
    }
}