package com.auction.model;

import java.time.LocalDateTime;

// Chuyển thành abstract class để áp dụng tính trừu tượng
public abstract class Item {
    protected String id;
    protected String name;
    protected String description;
    protected double startingPrice;
    protected double currentPrice;
    protected LocalDateTime endTime;
    protected User lastBidder;
    protected boolean isAuctionActive = true;
    protected String highestBidderName = "Chưa có";

    // Constructor chung cho các lớp con gọi qua super()
    public Item(String id, String name, String description, double startingPrice, double currentPrice) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentPrice = currentPrice;
        this.isAuctionActive = true;
    }

    // Phương thức trừu tượng để thực hiện tính đa hình
    public abstract String getItemType();

    // Các Getter và Setter giữ nguyên để đảm bảo tính đóng gói (Encapsulation)[cite: 1]
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getStartingPrice() { return startingPrice; }
    public void setStartingPrice(double startingPrice) { this.startingPrice = startingPrice; }
    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public User getLastBidder() { return lastBidder; }
    public void setLastBidder(User lastBidder) { this.lastBidder = lastBidder; }
    public boolean isAuctionActive() { return isAuctionActive; }
    public void setAuctionActive(boolean active) { this.isAuctionActive = active; }
    public String getStatus() { return isAuctionActive ? "Đang đấu giá" : "Đã kết thúc"; }
    public String getHighestBidderName() { return highestBidderName; }
    public void setHighestBidderName(String name) { this.highestBidderName = name; }
}