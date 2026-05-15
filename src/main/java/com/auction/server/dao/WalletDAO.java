package com.auction.server.dao;

import com.auction.dto.DepositRequestDto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class WalletDAO {

    public double getBalance(String username) {
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
            System.out.println("[WalletDAO] Lỗi lấy số dư: " + e.getMessage());
        }
        return 0.0;
    }

    public boolean createDepositRequest(String username, double amount) {
        if (username == null || username.isBlank() || amount <= 0) {
            return false;
        }

        String sql = """
                INSERT INTO wallet_deposit_requests (username, amount, status)
                VALUES (?, ?, 'PENDING')
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, username.trim());
            statement.setDouble(2, amount);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[WalletDAO] Lỗi tạo yêu cầu nạp tiền: " + e.getMessage());
            return false;
        }
    }

    public List<DepositRequestDto> getPendingDepositRequests() {
        String sql = """
                SELECT id, username, amount, status, requested_at, reviewed_by, reviewed_at, rejection_reason
                FROM wallet_deposit_requests
                WHERE status = 'PENDING'
                ORDER BY requested_at ASC
                """;
        List<DepositRequestDto> result = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()
        ) {
            while (rs.next()) {
                result.add(mapDepositRequest(rs));
            }
        } catch (SQLException e) {
            System.out.println("[WalletDAO] Lỗi lấy danh sách yêu cầu nạp tiền: " + e.getMessage());
        }
        return result;
    }

    public boolean approveDepositRequest(int requestId, String adminUsername) {
        if (requestId <= 0 || adminUsername == null || adminUsername.isBlank()) {
            return false;
        }

        String selectSql = """
                SELECT username, amount, status
                FROM wallet_deposit_requests
                WHERE id = ?
                FOR UPDATE
                """;
        String updateBalanceSql = """
                UPDATE users
                SET balance = balance + ?
                WHERE username = ?
                """;
        String updateRequestSql = """
                UPDATE wallet_deposit_requests
                SET status = 'APPROVED', reviewed_by = ?, reviewed_at = NOW(), rejection_reason = NULL
                WHERE id = ? AND status = 'PENDING'
                """;
        String logSql = """
                INSERT INTO wallet_transactions (username, amount, transaction_type, note)
                VALUES (?, ?, 'DEPOSIT', ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (
                    PreparedStatement selectStatement = connection.prepareStatement(selectSql);
                    PreparedStatement updateBalanceStatement = connection.prepareStatement(updateBalanceSql);
                    PreparedStatement updateRequestStatement = connection.prepareStatement(updateRequestSql);
                    PreparedStatement logStatement = connection.prepareStatement(logSql)
            ) {
                selectStatement.setInt(1, requestId);
                String username;
                double amount;
                String status;
                try (ResultSet rs = selectStatement.executeQuery()) {
                    if (!rs.next()) {
                        connection.rollback();
                        return false;
                    }
                    username = rs.getString("username");
                    amount = rs.getDouble("amount");
                    status = rs.getString("status");
                }

                if (!"PENDING".equalsIgnoreCase(status)) {
                    connection.rollback();
                    return false;
                }

                updateBalanceStatement.setDouble(1, amount);
                updateBalanceStatement.setString(2, username);
                if (updateBalanceStatement.executeUpdate() == 0) {
                    connection.rollback();
                    return false;
                }

                updateRequestStatement.setString(1, adminUsername.trim());
                updateRequestStatement.setInt(2, requestId);
                if (updateRequestStatement.executeUpdate() == 0) {
                    connection.rollback();
                    return false;
                }

                logStatement.setString(1, username);
                logStatement.setDouble(2, amount);
                logStatement.setString(3, "Admin " + adminUsername.trim() + " approved deposit request #" + requestId);
                logStatement.executeUpdate();

                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                System.out.println("[WalletDAO] Lỗi duyệt yêu cầu nạp tiền: " + e.getMessage());
                return false;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("[WalletDAO] Lỗi kết nối database: " + e.getMessage());
            return false;
        }
    }

    public boolean rejectDepositRequest(int requestId, String adminUsername, String reason) {
        if (requestId <= 0 || adminUsername == null || adminUsername.isBlank()) {
            return false;
        }

        String sql = """
                UPDATE wallet_deposit_requests
                SET status = 'REJECTED', reviewed_by = ?, reviewed_at = NOW(), rejection_reason = ?
                WHERE id = ? AND status = 'PENDING'
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, adminUsername.trim());
            statement.setString(2, reason == null || reason.isBlank() ? "Không có lý do cụ thể." : reason.trim());
            statement.setInt(3, requestId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[WalletDAO] Lỗi từ chối yêu cầu nạp tiền: " + e.getMessage());
            return false;
        }
    }

    /**
     * Backward-compatible method name. It now creates a PENDING request instead of crediting immediately.
     */
    public boolean deposit(String username, double amount) {
        return createDepositRequest(username, amount);
    }

    private DepositRequestDto mapDepositRequest(ResultSet rs) throws SQLException {
        Timestamp requestedAt = rs.getTimestamp("requested_at");
        Timestamp reviewedAt = rs.getTimestamp("reviewed_at");
        return new DepositRequestDto(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getDouble("amount"),
                rs.getString("status"),
                requestedAt == null ? null : requestedAt.toLocalDateTime().toString(),
                rs.getString("reviewed_by"),
                reviewedAt == null ? null : reviewedAt.toLocalDateTime().toString(),
                rs.getString("rejection_reason")
        );
    }
}
