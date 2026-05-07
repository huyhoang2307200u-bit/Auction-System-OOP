package com.auction.dao;

import com.auction.model.ElectronicItem;
import com.auction.model.Item;
import com.auction.util.DatabaseHelper;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {

    /**
     * Lấy danh sách toàn bộ sản phẩm từ DB
     */
    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM items";

        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                items.add(mapResultSetToItem(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getAllItems: " + e.getMessage());
        }
        return items;
    }

    /**
     * Lấy một sản phẩm cụ thể theo ID
     */
    public Item getItemById(String id) {
        String sql = "SELECT * FROM items WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(id));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToItem(rs);
                }
            }
        } catch (SQLException | NumberFormatException e) {
            System.err.println("Lỗi getItemById: " + e.getMessage());
        }
        return null;
    }

    /**
     * Cập nhật giá mới nhất khi có người đặt giá thành công
     */
    public void updatePrice(String id, double newPrice) {
        String sql = "UPDATE items SET current_price = ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, newPrice);
            pstmt.setInt(2, Integer.parseInt(id));
            pstmt.executeUpdate();

        } catch (SQLException | NumberFormatException e) {
            System.err.println("Lỗi updatePrice: " + e.getMessage());
        }
    }

    /**
     * Quan trọng: Lưu trạng thái đóng/mở phiên (is_active) xuống DB
     */
    public void updateStatus(String id, boolean isActive) {
        String sql = "UPDATE items SET is_active = ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Chuyển boolean thành 0 (false) hoặc 1 (true) cho SQLite
            pstmt.setInt(1, isActive ? 1 : 0);
            pstmt.setInt(2, Integer.parseInt(id));
            pstmt.executeUpdate();
            System.out.println(">>> DB: Đã cập nhật is_active = " + isActive + " cho SP ID: " + id);

        } catch (SQLException | NumberFormatException e) {
            System.err.println("Lỗi updateStatus: " + e.getMessage());
        }
    }

    /**
     * Thêm sản phẩm mới vào Database
     */
    public boolean insertItem(Item item) {
        // Luôn mặc định is_active = 1 khi mới thêm
        String sql = "INSERT INTO items (name, current_price, end_time, is_active) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, item.getName());
            pstmt.setDouble(2, item.getCurrentPrice());
            pstmt.setString(3, "2026-12-31"); // Có thể thay bằng item.getEndTime() nếu cần
            pstmt.setInt(4, 1);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi insertItem: " + e.getMessage());
            return false;
        }
    }

    /**
     * Hàm quan trọng nhất: Chuyển dữ liệu từ DB sang Object Java
     * Đã cập nhật để đọc cột is_active
     */
    private Item mapResultSetToItem(ResultSet rs) throws SQLException {
        String id = String.valueOf(rs.getInt("id"));
        String name = rs.getString("name");
        double price = rs.getDouble("current_price");

        // ĐỌC TRẠNG THÁI TỪ CỘT is_active (Nếu không có cột này sẽ lỗi - hãy nhớ xóa file .db cũ)
        boolean isActive = rs.getInt("is_active") == 1;

        // Tạo object (Sử dụng ElectronicItem làm ví dụ)
        Item item = new ElectronicItem(id, name, "Sản phẩm đấu giá", price, price);

        // Gán trạng thái vào Object để Controller có thể hiển thị màu xám nếu phiên đã đóng
        item.setAuctionActive(isActive);

        return item;
    }
}