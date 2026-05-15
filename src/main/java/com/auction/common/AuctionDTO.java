package com.auction.common;

public class AuctionDTO {
    private int id;
    private String itemName;
    private String description;
    private double currentPrice;
    private String status;
    private String sellerUsername;
    private String winnerUsername;
    private String endTime;

    public AuctionDTO() {
    }

    public AuctionDTO(int id, String itemName, String description, double currentPrice, String status) {
        this(id, itemName, description, currentPrice, status, null, null, null);
    }

    public AuctionDTO(int id, String itemName, String description, double currentPrice, String status,
                      String sellerUsername, String winnerUsername, String endTime) {
        this.id = id;
        this.itemName = itemName;
        this.description = description;
        this.currentPrice = currentPrice;
        this.status = status;
        this.sellerUsername = sellerUsername;
        this.winnerUsername = winnerUsername;
        this.endTime = endTime;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSellerUsername() { return sellerUsername; }
    public void setSellerUsername(String sellerUsername) { this.sellerUsername = sellerUsername; }

    public String getWinnerUsername() { return winnerUsername; }
    public void setWinnerUsername(String winnerUsername) { this.winnerUsername = winnerUsername; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    @Override
    public String toString() {
        return "AuctionDTO{" +
                "id=" + id +
                ", itemName='" + itemName + '\'' +
                ", currentPrice=" + currentPrice +
                ", status='" + status + '\'' +
                ", sellerUsername='" + sellerUsername + '\'' +
                ", winnerUsername='" + winnerUsername + '\'' +
                '}';
    }
}
