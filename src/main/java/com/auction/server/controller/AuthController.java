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
}