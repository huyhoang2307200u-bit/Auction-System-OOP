package com.auction.util;

import java.sql.*;

public class DatabaseHelper {
    private static final String URL = "jdbc:sqlite:auction.db";

    // Thiết lập kết nối và khởi tạo cấu trúc dữ liệu khi lớp được nạp
    static {
        initDatabase();
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    /**
     * Khởi tạo các bảng cần thiết và nạp dữ liệu mặc định cho hệ thống
     */
    public static void initDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Quản lý danh sách người dùng và phân quyền
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE, " +
                    "password TEXT, " +
                    "name TEXT, " +
                    "role TEXT)");

            // 2. Thông tin chi tiết các sản phẩm đang đấu giá
            stmt.execute("CREATE TABLE IF NOT EXISTS items (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT, " +
                    "current_price REAL, " +
                    "end_time TEXT, " +
                    "is_active INTEGER DEFAULT 1)");

            // 3. Lưu trữ lịch sử tất cả các lượt đặt giá (Transaction Logs)
            stmt.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER, " +
                    "item_id INTEGER, " +
                    "amount REAL, " +
                    "time TEXT, " +
                    "FOREIGN KEY(user_id) REFERENCES users(user_id), " +
                    "FOREIGN KEY(item_id) REFERENCES items(id))");

            // Khởi tạo tài khoản quản trị mặc định cho hệ thống
            stmt.execute("INSERT OR IGNORE INTO users (username, password, name, role) " +
                    "VALUES ('admin', 'admin123', 'Quản trị viên', 'ADMIN')");

            // Kiểm tra và nạp dữ liệu ban đầu cho bảng sản phẩm nếu chưa có dữ liệu
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM items");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO items (name, current_price, end_time, is_active) " +
                        "VALUES ('Laptop Dell XPS 15', 2500.0, '2026-12-31', 1)");
                stmt.execute("INSERT INTO items (name, current_price, end_time, is_active) " +
                        "VALUES ('iPhone 15 Pro Max', 1200.0, '2026-12-31', 1)");
                stmt.execute("INSERT INTO items (name, current_price, end_time, is_active) " +
                        "VALUES ('AirPod Pro 2', 500.0, '2026-12-31', 1)");

                System.out.println(">>> Hệ thống: Khởi tạo database và dữ liệu mẫu thành công.");
            }

        } catch (SQLException e) {
            System.err.println(">>> Lỗi nghiêm trọng khi khởi tạo cơ sở dữ liệu: " + e.getMessage());
        }
    }
}