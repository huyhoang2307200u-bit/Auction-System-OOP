package com.auction.server.dao;

import com.auction.model.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    public static final String DEFAULT_ADMIN_USERNAME = "admin";
    public static final String DEFAULT_ADMIN_PASSWORD = "123";
    public static final String DEFAULT_ADMIN_DISPLAY_NAME = "Quản trị viên";

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
            statement.setString(1, username == null ? null : username.trim());
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
        return register(username, password, role, null);
    }

    public boolean register(String username, String password, String role, String displayName) {
        if (username == null || username.isBlank()
                || password == null || password.isBlank()
                || role == null || role.isBlank()) {
            return false;
        }

        String normalizedUsername = username.trim();
        String normalizedRole = role.trim().toUpperCase();
        String normalizedDisplayName = displayName == null || displayName.isBlank()
                ? normalizedUsername
                : displayName.trim();

        if (!isValidRole(normalizedRole)) {
            System.out.println("[UserDAO] Role không hợp lệ: " + role);
            return false;
        }

        // Quy định bảo mật: người dùng không được tự đăng ký Admin.
        // Tài khoản Admin duy nhất chỉ được seed sẵn bởi server/database.
        if (Role.ADMIN.name().equals(normalizedRole)) {
            System.out.println("[UserDAO] Từ chối đăng ký Admin từ client: " + normalizedUsername);
            return false;
        }

        if (usernameExists(normalizedUsername)) {
            System.out.println("[UserDAO] Username đã tồn tại: " + normalizedUsername);
            return false;
        }

        String sql = """
                INSERT INTO users (username, password, display_name, role)
                VALUES (?, ?, ?, ?)
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, normalizedUsername);
            statement.setString(2, password);
            statement.setString(3, normalizedDisplayName);
            statement.setString(4, normalizedRole);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("[UserDAO] Lỗi đăng ký tài khoản: " + e.getMessage());
            return false;
        }
    }

    public void seedDefaultAdminIfMissing() {
        if (adminExists()) {
            return;
        }

        String sql = """
                INSERT INTO users (username, password, display_name, role)
                VALUES (?, ?, ?, 'ADMIN')
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, DEFAULT_ADMIN_USERNAME);
            statement.setString(2, DEFAULT_ADMIN_PASSWORD);
            statement.setString(3, DEFAULT_ADMIN_DISPLAY_NAME);
            statement.executeUpdate();
            System.out.println("[UserDAO] Đã seed tài khoản Admin mặc định.");
        } catch (SQLException e) {
            System.out.println("[UserDAO] Không thể seed Admin mặc định: " + e.getMessage());
        }
    }

    public boolean adminExists() {
        String sql = """
                SELECT id
                FROM users
                WHERE role = 'ADMIN'
                LIMIT 1
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            return resultSet.next();
        } catch (SQLException e) {
            System.out.println("[UserDAO] Lỗi kiểm tra Admin: " + e.getMessage());
            return true;
        }
    }

    public int countAdmins() {
        String sql = """
                SELECT COUNT(*) AS total
                FROM users
                WHERE role = 'ADMIN'
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            if (resultSet.next()) {
                return resultSet.getInt("total");
            }
        } catch (SQLException e) {
            System.out.println("[UserDAO] Lỗi đếm Admin: " + e.getMessage());
        }
        return 0;
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
            statement.setString(1, username == null ? null : username.trim());

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
            statement.setString(1, username == null ? null : username.trim());

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

    public String getDisplayNameByUsername(String username) {
        String sql = """
                SELECT display_name
                FROM users
                WHERE username = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, username == null ? null : username.trim());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String displayName = resultSet.getString("display_name");
                    return displayName == null || displayName.isBlank() ? username : displayName;
                }
            }

        } catch (SQLException e) {
            System.out.println("[UserDAO] Lỗi lấy display name: " + e.getMessage());
        }

        return username;
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
            statement.setString(1, username == null ? null : username.trim());

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

    public double getBalanceByUsername(String username) {
        String sql = """
                SELECT balance
                FROM users
                WHERE username = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, username == null ? null : username.trim());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble("balance");
                }
            }
        } catch (SQLException e) {
            System.out.println("[UserDAO] Lỗi lấy số dư: " + e.getMessage());
        }

        return 0.0;
    }

    public boolean userExists(String username) {
        return usernameExists(username);
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
