package com.auction.common;

public class AuctionDTO {
    private int id;
    private String itemName;
    private String description;
    private double currentPrice;
    private String status;

    public AuctionDTO() {
    }

    public AuctionDTO(int id, String itemName, String description, double currentPrice, String status) {
        this.id = id;
        this.itemName = itemName;
        this.description = description;
        this.currentPrice = currentPrice;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "AuctionDTO{" +
                "id=" + id +
                ", itemName='" + itemName + '\'' +
                ", description='" + description + '\'' +
                ", currentPrice=" + currentPrice +
                ", status='" + status + '\'' +
                '}';
    }
}