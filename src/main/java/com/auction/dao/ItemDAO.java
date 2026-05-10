package com.auction.dao;

import com.auction.model.ElectronicItem;
import com.auction.model.Item;
import com.auction.util.DatabaseHelper;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {

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
     * SỬA LỖI TẠI ĐÂY: Trả về true nếu update thành công
     */
    public boolean updatePrice(String id, double newPrice) {
        String sql = "UPDATE items SET current_price = ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, newPrice);
            pstmt.setInt(2, Integer.parseInt(id));

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0; // Trả về true nếu có ít nhất 1 dòng được cập nhật

        } catch (SQLException | NumberFormatException e) {
            System.err.println("Lỗi updatePrice: " + e.getMessage());
            return false;
        }
    }

    public void updateStatus(String id, boolean isActive) {
        String sql = "UPDATE items SET is_active = ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, isActive ? 1 : 0);
            pstmt.setInt(2, Integer.parseInt(id));
            pstmt.executeUpdate();
        } catch (SQLException | NumberFormatException e) {
            System.err.println("Lỗi updateStatus: " + e.getMessage());
        }
    }

    public boolean insertItem(Item item) {
        String sql = "INSERT INTO items (name, current_price, is_active) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, item.getName());
            pstmt.setDouble(2, item.getCurrentPrice());
            pstmt.setInt(3, 1);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi insertItem: " + e.getMessage());
            return false;
        }
    }

    private Item mapResultSetToItem(ResultSet rs) throws SQLException {
        String id = String.valueOf(rs.getInt("id"));
        String name = rs.getString("name");
        double price = rs.getDouble("current_price");
        boolean isActive = rs.getInt("is_active") == 1;

        Item item = new ElectronicItem(id, name, "Sản phẩm đấu giá", price, price);
        item.setAuctionActive(isActive);
        return item;
    }
}