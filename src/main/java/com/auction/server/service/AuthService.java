package com.auction.server.service;

import com.auction.dto.LoginResultDto;
import com.auction.exception.AuthenticationException;
import com.auction.model.Role;
import com.auction.server.dao.UserDAO;

public class AuthService {
    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
        this.userDAO.seedDefaultAdminIfMissing();
    }

    public LoginResultDto login(String username, String password) {
        if (!authenticate(username, password)) {
            throw new AuthenticationException("Tên đăng nhập hoặc mật khẩu không đúng.");
        }

        Integer userId = userDAO.getUserIdByUsername(username);
        String role = userDAO.getUserRole(username);
        String displayName = userDAO.getDisplayNameByUsername(username);
        double balance = userDAO.getBalanceByUsername(username);

        if (userId == null || role == null) {
            throw new AuthenticationException("Không thể lấy thông tin người dùng sau đăng nhập.");
        }

        return new LoginResultDto(
                String.valueOf(userId),
                username.trim(),
                displayName,
                Role.valueOf(role),
                balance
        );
    }

    public boolean authenticate(String username, String password) {
        return userDAO.authenticate(username, password);
    }

    public boolean register(String username, String password, String role) {
        return register(username, password, role, null);
    }

    public boolean register(String username, String password, String role, String displayName) {
        if (role != null && Role.ADMIN.name().equalsIgnoreCase(role.trim())) {
            return false;
        }
        return userDAO.register(username, password, role, displayName);
    }

    public String getUserRole(String username) {
        return userDAO.getUserRole(username);
    }

    public Integer getUserIdByUsername(String username) {
        return userDAO.getUserIdByUsername(username);
    }
}
