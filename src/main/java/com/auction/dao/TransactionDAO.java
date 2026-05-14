package com.auction.dao;

import com.auction.util.DatabaseHelper;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    // Định dạng thời gian: Ngày/Tháng/Năm Giờ:Phút
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Lấy toàn bộ lịch sử giao dịch (Đã thêm cột ID và làm đẹp thời gian)
     */
    public List<String[]> getFullHistory() {
        List<String[]> history = new ArrayList<>();
        // 1. Thêm t.id vào câu lệnh SELECT
        String sql = "SELECT t.id, u.username, i.name as item_name, t.amount, t.time " +
                "FROM transactions t " +
                "JOIN users u ON t.user_id = u.user_id " +
                "JOIN items i ON t.item_id = i.id " +
                "ORDER BY t.id DESC"; // Sắp xếp theo ID mới nhất lên đầu

        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // 2. Xử lý thời gian cho đẹp
                String rawTime = rs.getString("time");
                String formattedTime = rawTime;
                try {
                    LocalDateTime dt = LocalDateTime.parse(rawTime);
                    formattedTime = dt.format(formatter);
                } catch (Exception e) {
                    // Nếu lỗi định dạng thì giữ nguyên bản gốc
                }

                // 3. Thêm vào mảng String (Thứ tự: ID, Username, Item, Amount, Time)
                history.add(new String[]{
                        rs.getString("id"),         // Index 0
                        rs.getString("username"),   // Index 1
                        rs.getString("item_name"),  // Index 2
                        String.valueOf(rs.getDouble("amount")), // Index 3
                        formattedTime               // Index 4
                });
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn lịch sử: " + e.getMessage());
        }
        return history;
    }

    /**
     * Lưu giao dịch mới
     */
    public void saveTransaction(int userId, int itemId, double amount) {
        String sql = "INSERT INTO transactions (user_id, item_id, amount, time) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, itemId);
            pstmt.setDouble(3, amount);
            pstmt.setString(4, LocalDateTime.now().toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}