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

    public String getUserRole(String username) {
        return userDAO.getUserRole(username);
    }
}