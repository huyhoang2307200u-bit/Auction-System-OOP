package com.auction.server.controller;

import com.auction.common.Request;
import com.auction.common.Response;

public class AuthController {

    public Response login(Request request) {
        String username = request.getUsername();
        String password = request.getPassword();

        if (username == null || username.trim().isEmpty()) {
            return new Response(false, "Tên đăng nhập không được để trống.", null);
        }

        if (password == null || password.trim().isEmpty()) {
            return new Response(false, "Mật khẩu không được để trống.", null);
        }

        if (username.equals("admin") && password.equals("123456")) {
            String role = "ADMIN";

            return new Response(
                    true,
                    "Đăng nhập thành công.",
                    "Xin chào " + username + " | Vai trò: " + role
            );
        }

        return new Response(false, "Tên đăng nhập hoặc mật khẩu không đúng.", null);
    }
}