package com.auction.dao;

import com.auction.model.User;
import com.auction.model.Admin; // Cần import thêm Admin
import com.auction.model.Bidder;
import com.auction.util.DatabaseHelper;
import java.sql.*;

public class UserDAO {
    public User login(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("user_id");
                String user = rs.getString("username");
                String pass = rs.getString("password");
                String role = rs.getString("role");
                String name = rs.getString("name"); // Nếu DB có cột name, hãy lấy ra

                // KIỂM TRA ROLE ĐỂ TRẢ VỀ ĐÚNG ĐỐI TƯỢNG
                if (role != null && role.equalsIgnoreCase("ADMIN")) {
                    // Tạo Admin: ID, Tên, Username (email), Pass, Role
                    return new Admin(id, name != null ? name : user, user, pass, "ADMIN");
                } else {
                    // Tạo Bidder: ID, Tên, Username (email), Pass, Role
                    return new Bidder(id, name != null ? name : user, user, pass, "USER");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean register(String username, String password, String role) {
        // Nên thêm cột 'name' vào DB và hàm này nếu bạn muốn hiển thị tên thật
        String sql = "INSERT INTO users(username, password, role, name) VALUES(?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role);
            pstmt.setString(4, username); // Tạm thời lấy username làm tên hiển thị

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi đăng ký: " + e.getMessage());
            return false;
        }
    }
}