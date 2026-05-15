package com.auction.server.controller;

import com.auction.common.Request;
import com.auction.common.Response;
import com.auction.exception.AuthenticationException;
import com.auction.model.Role;
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

        try {
            return new Response(true, "Login successful.", authService.login(username, password));
        } catch (AuthenticationException | IllegalStateException e) {
            return new Response(false, e.getMessage(), null);
        }
    }

    public Response register(Request request) {
        String username = request.getUsername();
        String password = request.getPassword();
        String role = request.getRole();
        String displayName = request.getDisplayName();

        if (username == null || username.isBlank()
                || password == null || password.isBlank()
                || role == null || role.isBlank()) {
            return new Response(false, "Tên đăng nhập, mật khẩu và vai trò không được để trống.", null);
        }

        if (Role.ADMIN.name().equalsIgnoreCase(role.trim())) {
            return new Response(false,
                    "Không được phép đăng ký tài khoản Admin. Admin duy nhất được seed sẵn bởi hệ thống.",
                    null);
        }

        boolean success = authService.register(username, password, role, displayName);

        if (success) {
            return new Response(true, "Đăng ký thành công.", null);
        }

        return new Response(false, "Đăng ký thất bại. Tên đăng nhập có thể đã tồn tại hoặc vai trò không hợp lệ.", null);
    }
}
