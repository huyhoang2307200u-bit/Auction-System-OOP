package com.auction.server.controller;

import com.auction.common.Request;
import com.auction.common.Response;
import com.auction.server.service.AuthService;

public class AuthController {
    private final AuthService authService;

    public AuthController() {
        this.authService = new AuthService();
    }

    public Response login(Request request) {
        String username = request.getUsername();
        String password = request.getPassword();

        if (username == null || username.isBlank() ||
                password == null || password.isBlank()) {
            return new Response(false, "Username or password cannot be empty.", null);
        }

        boolean isAuthenticated = authService.authenticate(username, password);

        if (isAuthenticated) {
            String role = authService.getUserRole(username);
            return new Response(true, "Login successful.", "Welcome " + username + " | Role: " + role);
        } else {
            return new Response(false, "Invalid username or password.", null);
        }
    }
    public Response register(Request request) {
        String username = request.getUsername();
        String password = request.getPassword();
        String role = request.getRole();

        if (username == null || username.isBlank()
                || password == null || password.isBlank()
                || role == null || role.isBlank()) {
            return new Response(false, "Tên đăng nhập, mật khẩu và vai trò không được để trống.", null);
        }

        boolean success = authService.register(username, password, role);

        if (success) {
            return new Response(true, "Đăng ký thành công.", null);
        }

        return new Response(false, "Đăng ký thất bại. Tên đăng nhập có thể đã tồn tại.", null);
    }
}