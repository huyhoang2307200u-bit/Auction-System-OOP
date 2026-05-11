package com.auction.server.dao;

import com.auction.model.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public boolean authenticate(String username, String password) {
        String sql = """
                SELECT id
                FROM users
                WHERE username = ? AND password = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, username);
            statement.setString(2, password);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            System.out.println("[UserDAO] Lỗi đăng nhập: " + e.getMessage());
            return false;
        }
    }

    public boolean register(String username, String password, String role) {
        if (username == null || username.isBlank()
                || password == null || password.isBlank()
                || role == null || role.isBlank()) {
            return false;
        }

        String normalizedUsername = username.trim();
        String normalizedRole = role.trim().toUpperCase();

        if (!isValidRole(normalizedRole)) {
            System.out.println("[UserDAO] Role không hợp lệ: " + role);
            return false;
        }

        if (usernameExists(normalizedUsername)) {
            System.out.println("[UserDAO] Username đã tồn tại: " + normalizedUsername);
            return false;
        }

        String sql = """
                INSERT INTO users (username, password, role)
                VALUES (?, ?, ?)
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, normalizedUsername);
            statement.setString(2, password);
            statement.setString(3, normalizedRole);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("[UserDAO] Lỗi đăng ký tài khoản: " + e.getMessage());
            return false;
        }
    }

    public boolean usernameExists(String username) {
        String sql = """
                SELECT id
                FROM users
                WHERE username = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            System.out.println("[UserDAO] Lỗi kiểm tra username: " + e.getMessage());
            return true;
        }
    }

    public String getUserRole(String username) {
        String sql = """
                SELECT role
                FROM users
                WHERE username = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("role");
                }
            }

        } catch (SQLException e) {
            System.out.println("[UserDAO] Lỗi lấy role người dùng: " + e.getMessage());
        }

        return null;
    }

    public Integer getUserIdByUsername(String username) {
        String sql = """
                SELECT id
                FROM users
                WHERE username = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }

        } catch (SQLException e) {
            System.out.println("[UserDAO] Lỗi lấy id người dùng: " + e.getMessage());
        }

        return null;
    }

    public boolean isAdmin(String username) {
        String role = getUserRole(username);
        return Role.ADMIN.name().equalsIgnoreCase(role);
    }

    public boolean isSeller(String username) {
        String role = getUserRole(username);
        return Role.SELLER.name().equalsIgnoreCase(role);
    }

    public boolean isBidder(String username) {
        String role = getUserRole(username);
        return Role.BIDDER.name().equalsIgnoreCase(role);
    }

    private boolean isValidRole(String role) {
        try {
            Role.valueOf(role);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}