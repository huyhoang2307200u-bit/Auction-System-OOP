package com.auction.server.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = System.getenv().getOrDefault(
            "AUCTION_DB_URL",
            "jdbc:mysql://localhost:3306/auction_system?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true"
    );
    private static final String USER = System.getenv().getOrDefault("AUCTION_DB_USER", "root");
    private static final String PASSWORD = System.getenv().getOrDefault("AUCTION_DB_PASSWORD", "123456789");

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("[DB] MySQL Driver loaded successfully.");
        } catch (ClassNotFoundException e) {
            System.out.println("[DB] Failed to load MySQL Driver: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
