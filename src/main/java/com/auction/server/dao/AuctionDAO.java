package com.auction.server.dao;

import com.auction.common.AuctionDTO;
import com.auction.common.BidResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AuctionDAO {

    public List<AuctionDTO> getAllAuctions() {
        List<AuctionDTO> auctions = new ArrayList<>();

        String sql = """
                SELECT id, item_name, description, current_price, status
                FROM auctions
                ORDER BY id ASC
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                AuctionDTO auction = mapResultSetToAuctionDTO(resultSet);
                auctions.add(auction);
            }

        } catch (SQLException e) {
            System.out.println("[AuctionDAO] Lỗi khi lấy danh sách phiên đấu giá: " + e.getMessage());
        }

        return auctions;
    }

    public AuctionDTO getAuctionById(int auctionId) {
        String sql = """
                SELECT id, item_name, description, current_price, status
                FROM auctions
                WHERE id = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, auctionId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToAuctionDTO(resultSet);
                }
            }

        } catch (SQLException e) {
            System.out.println("[AuctionDAO] Lỗi khi lấy chi tiết phiên đấu giá: " + e.getMessage());
        }

        return null;
    }

    public boolean createAuction(String itemName, String description, double startPrice, String status) {
        String sql = """
                INSERT INTO auctions (item_name, description, current_price, status)
                VALUES (?, ?, ?, ?)
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, itemName);
            statement.setString(2, description);
            statement.setDouble(3, startPrice);
            statement.setString(4, status);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("[AuctionDAO] Lỗi khi tạo phiên đấu giá: " + e.getMessage());
            return false;
        }
    }

    public boolean updateAuction(int auctionId, String itemName, String description, double currentPrice, String status) {
        String sql = """
                UPDATE auctions
                SET item_name = ?, description = ?, current_price = ?, status = ?
                WHERE id = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, itemName);
            statement.setString(2, description);
            statement.setDouble(3, currentPrice);
            statement.setString(4, status);
            statement.setInt(5, auctionId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("[AuctionDAO] Lỗi khi cập nhật phiên đấu giá: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteAuction(int auctionId) {
        String deleteBidsSql = """
                DELETE FROM bids
                WHERE auction_id = ?
                """;

        String deleteAuctionSql = """
                DELETE FROM auctions
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try (
                    PreparedStatement deleteBidsStatement = connection.prepareStatement(deleteBidsSql);
                    PreparedStatement deleteAuctionStatement = connection.prepareStatement(deleteAuctionSql)
            ) {
                deleteBidsStatement.setInt(1, auctionId);
                deleteBidsStatement.executeUpdate();

                deleteAuctionStatement.setInt(1, auctionId);
                int affectedRows = deleteAuctionStatement.executeUpdate();

                connection.commit();
                return affectedRows > 0;

            } catch (SQLException e) {
                connection.rollback();
                System.out.println("[AuctionDAO] Lỗi khi xóa phiên đấu giá: " + e.getMessage());
                return false;

            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.out.println("[AuctionDAO] Lỗi kết nối database khi xóa phiên đấu giá: " + e.getMessage());
            return false;
        }
    }

    public BidResult placeBid(int auctionId, String username, double amount) {
        String selectAuctionSql = """
                SELECT current_price, status
                FROM auctions
                WHERE id = ?
                FOR UPDATE
                """;

        String updateAuctionSql = """
                UPDATE auctions
                SET current_price = ?
                WHERE id = ?
                """;

        String insertBidSql = """
                INSERT INTO bids (auction_id, username, bid_amount, bid_time)
                VALUES (?, ?, ?, NOW())
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try (
                    PreparedStatement selectStatement = connection.prepareStatement(selectAuctionSql)
            ) {
                selectStatement.setInt(1, auctionId);

                try (ResultSet resultSet = selectStatement.executeQuery()) {
                    if (!resultSet.next()) {
                        connection.rollback();
                        return new BidResult(false, "Không tìm thấy phiên đấu giá.", null);
                    }

                    double currentPrice = resultSet.getDouble("current_price");
                    String status = resultSet.getString("status");

                    if (!isAuctionOpenForBidding(status)) {
                        connection.rollback();
                        return new BidResult(false, "Phiên đấu giá đã đóng hoặc không thể đặt giá.", currentPrice);
                    }

                    if (amount <= currentPrice) {
                        connection.rollback();
                        return new BidResult(false, "Giá đặt phải lớn hơn giá hiện tại.", currentPrice);
                    }

                    try (
                            PreparedStatement updateStatement = connection.prepareStatement(updateAuctionSql);
                            PreparedStatement insertBidStatement = connection.prepareStatement(insertBidSql)
                    ) {
                        updateStatement.setDouble(1, amount);
                        updateStatement.setInt(2, auctionId);
                        updateStatement.executeUpdate();

                        insertBidStatement.setInt(1, auctionId);
                        insertBidStatement.setString(2, username);
                        insertBidStatement.setDouble(3, amount);
                        insertBidStatement.executeUpdate();
                    }

                    connection.commit();
                    return new BidResult(true, "Đặt giá thành công.", amount);
                }

            } catch (SQLException e) {
                connection.rollback();
                return new BidResult(false, "Lỗi database khi đặt giá: " + e.getMessage(), null);

            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            return new BidResult(false, "Lỗi kết nối database: " + e.getMessage(), null);
        }
    }

    private AuctionDTO mapResultSetToAuctionDTO(ResultSet resultSet) throws SQLException {
        return new AuctionDTO(
                resultSet.getInt("id"),
                resultSet.getString("item_name"),
                resultSet.getString("description"),
                resultSet.getDouble("current_price"),
                resultSet.getString("status")
        );
    }

    private boolean isAuctionOpenForBidding(String status) {
        if (status == null) {
            return false;
        }

        return status.equalsIgnoreCase("OPEN")
                || status.equalsIgnoreCase("RUNNING");
    }
}