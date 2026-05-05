package com.auction.model;

public class User {
    // 1. Khai báo đầy đủ các thuộc tính (fields)
    private int id;
    private String username;
    private String name;
    private String email;
    private String password;
    private String role; // "ADMIN" hoặc "USER"

    // 2. Constructor mặc định
    public User() {
    }

    // 3. Constructor chính (Sử dụng cho logic đăng nhập)
    public User(String username, String name, String role) {
        this.username = username;
        this.name = name;
        this.role = role;
    }

    // 4. Constructor đầy đủ (Sử dụng cho quản lý dữ liệu)
    public User(int id, String name, String email, String password, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // 5. Getter và Setter (Đủ cho tất cả thuộc tính)
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    @Override
    public String toString() {
        return "User{" + "username='" + username + '\'' + ", name='" + name + '\'' + ", role='" + role + '\'' + '}';
    }
}