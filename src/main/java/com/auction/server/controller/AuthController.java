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

        if (isBlank(username) || isBlank(password)) {
            return new Response(false, "Tên đăng nhập hoặc mật khẩu không được để trống.", null);
        }

        boolean isAuthenticated = authService.authenticate(username, password);

        if (!isAuthenticated) {
            return new Response(false, "Tên đăng nhập hoặc mật khẩu không đúng.", null);
        }

        String role = authService.getUserRole(username);

        return new Response(
                true,
                "Đăng nhập thành công.",
                "Xin chào " + username + " | Vai trò: " + role
        );
    }

    public Response register(Request request) {
        String username = request.getUsername();
        String password = request.getPassword();
        String role = request.getRole();

        if (isBlank(username)) {
            return new Response(false, "Tên đăng nhập không được để trống.", null);
        }

        if (isBlank(password)) {
            return new Response(false, "Mật khẩu không được để trống.", null);
        }

        if (isBlank(role)) {
            return new Response(false, "Vai trò không được để trống.", null);
        }

        role = role.trim().toUpperCase();

        if (!role.equals("BIDDER") && !role.equals("SELLER") && !role.equals("ADMIN")) {
            return new Response(false, "Vai trò không hợp lệ. Chỉ chấp nhận BIDDER, SELLER hoặc ADMIN.", null);
        }

        if (authService.existsByUsername(username)) {
            return new Response(false, "Tên đăng nhập đã tồn tại.", null);
        }

        boolean registered = authService.register(username, password, role);

        if (!registered) {
            return new Response(false, "Đăng ký thất bại. Vui lòng thử lại.", null);
        }

        return new Response(
                true,
                "Đăng ký thành công.",
                "Tài khoản: " + username + " | Vai trò: " + role
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
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