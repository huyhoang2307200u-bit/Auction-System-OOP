package com.auction.server.service;

public class AuthService {

    public boolean authenticate(String username, String password) {
        return "admin".equals(username) && "123456".equals(password);
    }
}