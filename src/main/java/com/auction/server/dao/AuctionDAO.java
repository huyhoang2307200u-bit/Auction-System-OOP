package com.auction.server.dao;

import com.auction.common.AuctionDTO;

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
            System.out.println("[AuctionDAO] Database error: " + e.getMessage());
        }

        return auctions;
    }
}