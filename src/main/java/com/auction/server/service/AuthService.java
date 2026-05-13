package com.auction.server.service;

import com.auction.server.dao.UserDAO;

public class AuthService {
    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public boolean authenticate(String username, String password) {
        return userDAO.authenticate(username, password);
    }

    public boolean register(String username, String password, String role) {
        return userDAO.register(username, password, role);
    }

    public String getUserRole(String username) {
        return userDAO.getUserRole(username);
    }

    public Integer getUserIdByUsername(String username) {
        return userDAO.getUserIdByUsername(username);
    }
}