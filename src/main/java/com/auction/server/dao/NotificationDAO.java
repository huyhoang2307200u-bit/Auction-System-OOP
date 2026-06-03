package com.auction.server.dao;

import com.auction.dto.NotificationDto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public boolean createNotification(String recipientUsername, String title, String message) {
        if (recipientUsername == null || recipientUsername.isBlank()) {
            return false;
        }
        String sql = """
                INSERT INTO notifications (recipient_username, title, message, is_read)
                VALUES (?, ?, ?, FALSE)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, recipientUsername.trim());
            statement.setString(2, title == null ? "Thông báo" : title.trim());
            statement.setString(3, message == null ? "" : message.trim());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[NotificationDAO] Lỗi tạo thông báo: " + e.getMessage());
            return false;
        }
    }

    public List<NotificationDto> getUnreadNotifications(String username) {
        List<NotificationDto> notifications = new ArrayList<>();
        String sql = """
                SELECT id, recipient_username, title, message, is_read, created_at
                FROM notifications
                WHERE recipient_username = ? AND is_read = FALSE
                ORDER BY created_at ASC, id ASC
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username == null ? null : username.trim());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    notifications.add(mapNotification(resultSet));
                }
            }
        } catch (SQLException e) {
            System.out.println("[NotificationDAO] Lỗi lấy thông báo: " + e.getMessage());
        }
        return notifications;
    }

    public boolean markRead(String username, int notificationId) {
        String sql = """
                UPDATE notifications
                SET is_read = TRUE
                WHERE id = ? AND recipient_username = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, notificationId);
            statement.setString(2, username == null ? null : username.trim());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[NotificationDAO] Lỗi đánh dấu đã đọc: " + e.getMessage());
            return false;
        }
    }

    private NotificationDto mapNotification(ResultSet rs) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        return new NotificationDto(
                rs.getInt("id"),
                rs.getString("recipient_username"),
                rs.getString("title"),
                rs.getString("message"),
                rs.getBoolean("is_read"),
                createdAt == null ? null : createdAt.toLocalDateTime().toString()
        );
    }
}
