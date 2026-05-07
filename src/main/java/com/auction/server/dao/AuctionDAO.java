package com.auction.server.dao;

import com.auction.common.AuctionDTO;
import com.auction.common.BidResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class AuctionDAO {

    public List<AuctionDTO> getAllAuctions() {
        List<AuctionDTO> auctions = new ArrayList<>();

        String sql = "SELECT id, item_name, description, current_price, status FROM auctions";

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                AuctionDTO auction = new AuctionDTO(
                        resultSet.getInt("id"),
                        resultSet.getString("item_name"),
                        resultSet.getString("description"),
                        resultSet.getDouble("current_price"),
                        resultSet.getString("status")
                );

                auctions.add(auction);
            }

        } catch (Exception e) {
            System.out.println("[AuctionDAO] Lỗi database khi lấy danh sách đấu giá: " + e.getMessage());
        }

        return auctions;
    }

    public BidResult placeBid(int auctionId, String username, double amount) {
        String selectSql = "SELECT current_price, status FROM auctions WHERE id = ? FOR UPDATE";
        String updateSql = "UPDATE auctions SET current_price = ? WHERE id = ?";
        String insertSql = "INSERT INTO bids (auction_id, username, bid_amount, bid_time) VALUES (?, ?, ?, NOW())";

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try (
                    PreparedStatement selectStmt = connection.prepareStatement(selectSql)
            ) {
                selectStmt.setInt(1, auctionId);

                try (ResultSet resultSet = selectStmt.executeQuery()) {
                    if (!resultSet.next()) {
                        connection.rollback();
                        return new BidResult(false, "Không tìm thấy phiên đấu giá.", null);
                    }

                    double currentPrice = resultSet.getDouble("current_price");
                    String status = resultSet.getString("status");

                    if (!"OPEN".equalsIgnoreCase(status) && !"RUNNING".equalsIgnoreCase(status)) {
                        connection.rollback();
                        return new BidResult(false, "Phiên đấu giá đã đóng hoặc không thể đặt giá.", currentPrice);
                    }

                    if (amount <= currentPrice) {
                        connection.rollback();
                        return new BidResult(false, "Giá đặt phải lớn hơn giá hiện tại.", currentPrice);
                    }

                    try (
                            PreparedStatement updateStmt = connection.prepareStatement(updateSql);
                            PreparedStatement insertStmt = connection.prepareStatement(insertSql)
                    ) {
                        updateStmt.setDouble(1, amount);
                        updateStmt.setInt(2, auctionId);
                        updateStmt.executeUpdate();

                        insertStmt.setInt(1, auctionId);
                        insertStmt.setString(2, username);
                        insertStmt.setDouble(3, amount);
                        insertStmt.executeUpdate();
                    }

                    connection.commit();
                    return new BidResult(true, "Đặt giá thành công.", amount);
                }

            } catch (Exception e) {
                connection.rollback();
                return new BidResult(false, "Lỗi database khi đặt giá: " + e.getMessage(), null);
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (Exception e) {
            return new BidResult(false, "Lỗi kết nối database: " + e.getMessage(), null);
        }
    }
}