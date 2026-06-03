package com.auction.service;

import com.auction.model.Admin;
import com.auction.model.Bidder;
import com.auction.model.Role;
import com.auction.model.Seller;
import com.auction.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AuthService {
    private static final List<User> users = new CopyOnWriteArrayList<>();

    static {
        seedDefaultUsers();
    }

    private AuthService() {
    }

    public static boolean register(User user) {
        if (user == null || user.getUsername() == null || user.getUsername().isBlank()) {
            return false;
        }
        // Không cho người dùng tự đăng ký Admin. Admin duy nhất được seed sẵn.
        if (user.getRole() == Role.ADMIN) {
            return false;
        }
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(user.getUsername())) {
                return false;
            }
        }
        users.add(user);
        return true;
    }

    public static User findUserById(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        for (User user : users) {
            if (user.getId().equals(userId)) {
                return user;
            }
        }
        return null;
    }

    public static User findUserByUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username.trim())) {
                return user;
            }
        }
        return null;
    }

    public static User login(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username.trim()) && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null;
    }

    public static List<User> snapshotUsers() {
        return new ArrayList<>(users);
    }

    public static void restoreUsers(List<User> savedUsers) {
        users.clear();
        if (savedUsers != null) {
            users.addAll(savedUsers);
        }
        ensureDefaultUsersExist();
    }

    public static void resetForTesting() {
        users.clear();
        seedDefaultUsers();
    }

    private static void seedDefaultUsers() {
        if (!users.isEmpty()) {
            return;
        }
        Admin admin = new Admin(1, "Quản trị viên", "admin", "123", "ADMIN");
        Seller seller = new Seller(2, "Người bán", "seller", "123", "SELLER");
        Bidder bidder = new Bidder(3, "Người đấu giá", "user", "123", "BIDDER");
        bidder.deposit(5000.0);
        users.add(admin);
        users.add(seller);
        users.add(bidder);
    }

    private static void ensureDefaultUsersExist() {
        if (findUserByUsername("admin") == null) {
            users.add(new Admin(1, "Quản trị viên", "admin", "123", "ADMIN"));
        }
        if (findUserByUsername("seller") == null) {
            users.add(new Seller(2, "Người bán", "seller", "123", "SELLER"));
        }
        if (findUserByUsername("user") == null) {
            Bidder bidder = new Bidder(3, "Người đấu giá", "user", "123", "BIDDER");
            bidder.deposit(5000.0);
            users.add(bidder);
        }
    }
}
