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
                SELECT id, item_name, description, current_price, status, seller_username, winner_username, end_time, image_url
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
                SELECT id, item_name, description, current_price, status, seller_username, winner_username, end_time, image_url
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
        return createAuction(null, itemName, description, "ELECTRONICS", startPrice, 10, status, null);
    }

    public boolean createAuction(String sellerUsername, String itemName, String description, double startPrice, String status) {
        return createAuction(sellerUsername, itemName, description, "ELECTRONICS", startPrice, 10, status, null);
    }

    public boolean createAuction(String sellerUsername, String itemName, String description,
                                 String category, double startPrice, Integer durationMinutes, String status, String imageUrl) {
        String sql = """
                INSERT INTO auctions (seller_username, item_name, description, category, starting_price, current_price, end_time, status, image_url)
                VALUES (?, ?, ?, ?, ?, ?, DATE_ADD(NOW(), INTERVAL ? MINUTE), ?, ?)
                """;

        int safeDuration = durationMinutes == null || durationMinutes <= 0 ? 10 : durationMinutes;
        String safeCategory = category == null || category.isBlank() ? "ELECTRONICS" : category.trim().toUpperCase();

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, sellerUsername);
            statement.setString(2, itemName);
            statement.setString(3, description);
            statement.setString(4, safeCategory);
            statement.setDouble(5, startPrice);
            statement.setDouble(6, startPrice);
            statement.setInt(7, safeDuration);
            statement.setString(8, status);
            statement.setString(9, imageUrl);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("[AuctionDAO] Lỗi khi tạo phiên đấu giá: " + e.getMessage());
            return false;
        }
    }


    public boolean approveAuction(int auctionId, String adminUsername) {
        String sql = """
                UPDATE auctions
                SET status = 'OPEN', reviewed_by = ?, reviewed_at = NOW(), rejection_reason = NULL
                WHERE id = ? AND status = 'PENDING_APPROVAL'
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, adminUsername);
            statement.setInt(2, auctionId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[AuctionDAO] Lỗi khi duyệt phiên đấu giá: " + e.getMessage());
            return false;
        }
    }

    public boolean rejectAuction(int auctionId, String adminUsername, String reason) {
        String sql = """
                UPDATE auctions
                SET status = 'REJECTED', reviewed_by = ?, reviewed_at = NOW(), rejection_reason = ?
                WHERE id = ? AND status = 'PENDING_APPROVAL'
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, adminUsername);
            statement.setString(2, reason);
            statement.setInt(3, auctionId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[AuctionDAO] Lỗi khi từ chối phiên đấu giá: " + e.getMessage());
            return false;
        }
    }

    public boolean finishAuction(int auctionId) {
        String sql = """
                UPDATE auctions
                SET status = 'FINISHED'
                WHERE id = ? AND status IN ('OPEN', 'RUNNING')
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, auctionId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[AuctionDAO] Lỗi khi kết thúc phiên đấu giá: " + e.getMessage());
            return false;
        }
    }

    public List<AuctionDTO> finishExpiredAuctions() {
        List<AuctionDTO> expiredAuctions = new ArrayList<>();

        String selectSql = """
                SELECT id, item_name, description, current_price, status, seller_username, winner_username, end_time, image_url
                FROM auctions
                WHERE status IN ('OPEN', 'RUNNING')
                  AND end_time IS NOT NULL
                  AND end_time <= NOW()
                """;

        String updateSql = """
                UPDATE auctions
                SET status = 'FINISHED'
                WHERE id = ? AND status IN ('OPEN', 'RUNNING')
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement selectStatement = connection.prepareStatement(selectSql);
                 PreparedStatement updateStatement = connection.prepareStatement(updateSql);
                 ResultSet resultSet = selectStatement.executeQuery()) {

                while (resultSet.next()) {
                    AuctionDTO auction = mapResultSetToAuctionDTO(resultSet);
                    updateStatement.setInt(1, auction.getId());
                    int affected = updateStatement.executeUpdate();
                    if (affected > 0) {
                        auction.setStatus("FINISHED");
                        expiredAuctions.add(auction);
                    }
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                System.out.println("[AuctionDAO] Lỗi khi tự kết thúc phiên hết hạn: " + e.getMessage());
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("[AuctionDAO] Lỗi kết nối khi tự kết thúc phiên hết hạn: " + e.getMessage());
        }

        return expiredAuctions;
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
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                BidResult result = placeBidInternal(connection, auctionId, username, amount, false);
                if (result.isSuccess()) {
                    runAutoBidding(connection, auctionId);
                    connection.commit();
                } else {
                    connection.rollback();
                }
                return result;
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

    private BidResult placeBidInternal(Connection connection, int auctionId, String username, double amount, boolean autoGenerated) throws SQLException {
        String selectAuctionSql = """
                SELECT current_price, status, winner_username, end_time
                FROM auctions
                WHERE id = ?
                FOR UPDATE
                """;

        String selectBalanceSql = """
                SELECT balance
                FROM users
                WHERE username = ?
                FOR UPDATE
                """;

        String updateBalanceSql = """
                UPDATE users
                SET balance = ?
                WHERE username = ?
                """;

        String logWalletSql = """
                INSERT INTO wallet_transactions (username, amount, transaction_type, note)
                VALUES (?, ?, ?, ?)
                """;

        String updateAuctionSql = """
                UPDATE auctions
                SET current_price = ?, winner_username = ?, status = 'RUNNING', version = version + 1, end_time = ?
                WHERE id = ?
                """;

        String insertBidSql = """
                INSERT INTO bids (auction_id, username, bid_amount, bid_time, auto_generated)
                VALUES (?, ?, ?, NOW(), ?)
                """;

        try (
                PreparedStatement selectAuctionStatement = connection.prepareStatement(selectAuctionSql)
        ) {
                selectAuctionStatement.setInt(1, auctionId);

                try (ResultSet resultSet = selectAuctionStatement.executeQuery()) {
                    if (!resultSet.next()) {
                        connection.rollback();
                        return new BidResult(false, "Không tìm thấy phiên đấu giá.", null);
                    }

                    double currentPrice = resultSet.getDouble("current_price");
                    String status = resultSet.getString("status");
                    String previousWinner = resultSet.getString("winner_username");

                    if (!isAuctionOpenForBidding(status)) {
                        return new BidResult(false, "Phiên đấu giá đã đóng hoặc không thể đặt giá.", currentPrice);
                    }

                    java.time.LocalDateTime endTime = null;
                    if (resultSet.getTimestamp("end_time") != null) {
                        endTime = resultSet.getTimestamp("end_time").toLocalDateTime();
                        if (!endTime.isAfter(java.time.LocalDateTime.now())) {
                            return new BidResult(false, "Phiên đấu giá đã hết thời gian. Vui lòng tải lại danh sách.", currentPrice);
                        }
                    }

                    java.time.LocalDateTime now = java.time.LocalDateTime.now();
                    boolean timeExtended = false;
                    if (endTime != null) {
                        java.time.Duration duration = java.time.Duration.between(now, endTime);
                        if (duration.getSeconds() <= 30 && duration.getSeconds() >= 0) {
                            endTime = endTime.plusMinutes(1);
                            timeExtended = true;
                        }
                    }

                    if (amount <= currentPrice) {
                        return new BidResult(false, "Giá đặt phải lớn hơn giá hiện tại.", currentPrice);
                    }

                    Double bidderBalance = getLockedBalance(connection, selectBalanceSql, username);
                    if (bidderBalance == null) {
                        return new BidResult(false, "Không tìm thấy ví tiền của người đặt giá.", currentPrice);
                    }

                    boolean sameWinner = previousWinner != null && previousWinner.equalsIgnoreCase(username);
                    double availableBalance = sameWinner ? bidderBalance + currentPrice : bidderBalance;
                    if (availableBalance < amount) {
                        return new BidResult(false,
                                "Số dư không đủ để đặt giá. Số dư khả dụng: " + availableBalance,
                                currentPrice);
                    }

                    try (
                            PreparedStatement updateBalanceStatement = connection.prepareStatement(updateBalanceSql);
                            PreparedStatement logWalletStatement = connection.prepareStatement(logWalletSql);
                            PreparedStatement updateAuctionStatement = connection.prepareStatement(updateAuctionSql);
                            PreparedStatement insertBidStatement = connection.prepareStatement(insertBidSql)
                    ) {
                        if (sameWinner) {
                            // Người đang dẫn đầu tự nâng giá: thay khoản giữ cũ bằng khoản giữ mới.
                            double newBalance = availableBalance - amount;
                            updateUserBalance(updateBalanceStatement, username, newBalance);
                            logWallet(logWalletStatement, username, currentPrice,
                                    "REFUND_BID_HOLD", "Refund previous hold before increasing bid");
                            logWallet(logWalletStatement, username, -amount,
                                    "BID_HOLD", "Hold new leading bid amount");
                        } else {
                            // Hoàn tiền cho người dẫn đầu cũ trước khi giữ tiền người dẫn đầu mới.
                            if (previousWinner != null && !previousWinner.isBlank()) {
                                Double previousBalance = getLockedBalance(connection, selectBalanceSql, previousWinner);
                                if (previousBalance != null) {
                                    updateUserBalance(updateBalanceStatement, previousWinner, previousBalance + currentPrice);
                                    logWallet(logWalletStatement, previousWinner, currentPrice,
                                            "REFUND_BID_HOLD", "Refund because another bidder outbid this user");
                                }
                            }

                            updateUserBalance(updateBalanceStatement, username, bidderBalance - amount);
                            logWallet(logWalletStatement, username, -amount,
                                    "BID_HOLD", "Hold leading bid amount");
                        }

                        updateAuctionStatement.setDouble(1, amount);
                        updateAuctionStatement.setString(2, username);
                        if (endTime != null) {
                            updateAuctionStatement.setTimestamp(3, java.sql.Timestamp.valueOf(endTime));
                        } else {
                            updateAuctionStatement.setNull(3, java.sql.Types.TIMESTAMP);
                        }
                        updateAuctionStatement.setInt(4, auctionId);
                        updateAuctionStatement.executeUpdate();

                        insertBidStatement.setInt(1, auctionId);
                        insertBidStatement.setString(2, username);
                        insertBidStatement.setDouble(3, amount);
                        insertBidStatement.setBoolean(4, autoGenerated);
                        insertBidStatement.executeUpdate();
                    }

                    return new BidResult(true, "Đặt giá thành công. Hệ thống đã tạm giữ số tiền bid trong ví.", amount, timeExtended);
                }
        }
    }

    private Double getLockedBalance(Connection connection, String selectBalanceSql, String username) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(selectBalanceSql)) {
            statement.setString(1, username == null ? null : username.trim());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble("balance");
                }
            }
        }
        return null;
    }

    private void updateUserBalance(PreparedStatement statement, String username, double newBalance) throws SQLException {
        statement.setDouble(1, newBalance);
        statement.setString(2, username == null ? null : username.trim());
        statement.executeUpdate();
    }

    private void logWallet(PreparedStatement statement, String username, double amount,
                           String type, String note) throws SQLException {
        statement.setString(1, username == null ? null : username.trim());
        statement.setDouble(2, amount);
        statement.setString(3, type);
        statement.setString(4, note);
        statement.executeUpdate();
    }

    private AuctionDTO mapResultSetToAuctionDTO(ResultSet resultSet) throws SQLException {
        return new AuctionDTO(
                resultSet.getInt("id"),
                resultSet.getString("item_name"),
                resultSet.getString("description"),
                resultSet.getDouble("current_price"),
                resultSet.getString("status"),
                resultSet.getString("seller_username"),
                resultSet.getString("winner_username"),
                resultSet.getTimestamp("end_time") == null ? null : resultSet.getTimestamp("end_time").toLocalDateTime().toString(),
                resultSet.getString("image_url")
        );
    }

    private boolean isAuctionOpenForBidding(String status) {
        if (status == null) {
            return false;
        }

        return status.equalsIgnoreCase("OPEN")
                || status.equalsIgnoreCase("RUNNING");
    }
    // Thêm hàm này vào class AuctionDAO
    public List<com.auction.dto.BidDto> getTransactionHistory() {
        List<com.auction.dto.BidDto> history = new java.util.ArrayList<>();
        String sql = """
            SELECT b.id, b.auction_id, b.username, a.item_name, b.bid_amount, b.bid_time, b.auto_generated
            FROM bids b
            JOIN auctions a ON b.auction_id = a.id
            ORDER BY b.bid_time DESC
            """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                com.auction.dto.BidDto dto = new com.auction.dto.BidDto();
                dto.setBidId(String.valueOf(rs.getInt("id")));
                dto.setBidderId(rs.getString("username"));
                dto.setBidderName(rs.getString("username"));
                dto.setItemName(rs.getString("item_name"));
                dto.setAmount(rs.getBigDecimal("bid_amount"));
                java.sql.Timestamp ts = rs.getTimestamp("bid_time");
                if (ts != null) {
                    dto.setTimestamp(ts.toLocalDateTime().toString());
                }
                dto.setAutoGenerated(rs.getBoolean("auto_generated"));
                history.add(dto);
            }
        } catch (SQLException e) {
            System.out.println("[AuctionDAO] Lỗi lấy lịch sử: " + e.getMessage());
        }
        return history;
    }

    public boolean registerAutoBid(int auctionId, String username, double maxBid, double increment) {
        String deactivateOldSql = "UPDATE auto_bids SET active = FALSE WHERE auction_id = ? AND username = ?";
        String insertSql = """
                INSERT INTO auto_bids (auction_id, username, max_bid, increment_amount, priority_order, active)
                VALUES (?, ?, ?, ?, ?, TRUE)
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (
                PreparedStatement deactivateStmt = connection.prepareStatement(deactivateOldSql);
                PreparedStatement insertStmt = connection.prepareStatement(insertSql)
            ) {
                deactivateStmt.setInt(1, auctionId);
                deactivateStmt.setString(2, username);
                deactivateStmt.executeUpdate();

                insertStmt.setInt(1, auctionId);
                insertStmt.setString(2, username);
                insertStmt.setDouble(3, maxBid);
                insertStmt.setDouble(4, increment);
                insertStmt.setLong(5, System.currentTimeMillis());
                insertStmt.executeUpdate();

                runAutoBidding(connection, auctionId);
                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                System.out.println("[AuctionDAO] Lỗi registerAutoBid: " + e.getMessage());
                return false;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("[AuctionDAO] Lỗi kết nối khi registerAutoBid: " + e.getMessage());
            return false;
        }
    }

    private void runAutoBidding(Connection connection, int auctionId) throws SQLException {
        String selectAutoBidsSql = """
                SELECT username, max_bid, increment_amount
                FROM auto_bids
                WHERE auction_id = ? AND active = TRUE
                ORDER BY max_bid DESC, priority_order ASC
                """;

        int safetyCounter = 0;
        while (safetyCounter++ < 100) {
            double currentPriceNow = 0;
            String previousWinner = "";
            String selectAuctionSql = "SELECT current_price, winner_username FROM auctions WHERE id = ? FOR UPDATE";
            try (PreparedStatement stmt = connection.prepareStatement(selectAuctionSql)) {
                stmt.setInt(1, auctionId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        currentPriceNow = rs.getDouble("current_price");
                        previousWinner = rs.getString("winner_username");
                    } else {
                        break;
                    }
                }
            }

            String bestBidder = null;
            double maxBid = 0;
            double increment = 0;

            try (PreparedStatement stmt = connection.prepareStatement(selectAutoBidsSql)) {
                stmt.setInt(1, auctionId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String user = rs.getString("username");
                        if (!user.equalsIgnoreCase(previousWinner) && rs.getDouble("max_bid") > currentPriceNow) {
                            bestBidder = user;
                            maxBid = rs.getDouble("max_bid");
                            increment = rs.getDouble("increment_amount");
                            break;
                        }
                    }
                }
            }

            if (bestBidder == null) {
                break;
            }

            double nextAmount = currentPriceNow + increment;
            if (nextAmount > maxBid) {
                nextAmount = maxBid;
            }
            if (nextAmount <= currentPriceNow) {
                deactivateAutoBid(connection, auctionId, bestBidder);
                continue;
            }

            BidResult result = placeBidInternal(connection, auctionId, bestBidder, nextAmount, true);
            if (!result.isSuccess()) {
                deactivateAutoBid(connection, auctionId, bestBidder);
                continue;
            }

            if (nextAmount >= maxBid) {
                deactivateAutoBid(connection, auctionId, bestBidder);
            }
        }
    }

    private void deactivateAutoBid(Connection connection, int auctionId, String username) throws SQLException {
        String sql = "UPDATE auto_bids SET active = FALSE WHERE auction_id = ? AND username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, auctionId);
            stmt.setString(2, username);
            stmt.executeUpdate();
        }
    }
}