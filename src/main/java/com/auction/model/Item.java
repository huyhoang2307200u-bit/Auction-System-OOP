package com.auction.model;

public class Item {
    private String id;
    private String name;
    private String description;
    private double startingPrice;
    private double currentPrice;
    // THÊM THUỘC TÍNH NÀY
    private User lastBidder;

    public Item() {}

    public Item(String id, String name, String description, double startingPrice, double currentPrice) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentPrice = currentPrice;
        this.lastBidder = null; // Khởi tạo ban đầu là chưa có ai
    }

    // --- Giữ nguyên các Getter/Setter cũ của bạn ---
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

    // --- THÊM Getter và Setter cho lastBidder ---
    public User getLastBidder() { return lastBidder; }
    public void setLastBidder(User lastBidder) { this.lastBidder = lastBidder; }
    private boolean isAuctionActive = true; // Mặc định là đang đấu

    // Getter và Setter
    public boolean isAuctionActive() { return isAuctionActive; }
    public void setAuctionActive(boolean active) { isAuctionActive = active; }
}