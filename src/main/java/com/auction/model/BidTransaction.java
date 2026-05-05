package com.auction.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BidTransaction {
    private int id;
    private Bidder bidder;
    // Thay Auction bằng Item để khớp với các class khác của bạn
    private Item auctionItem;
    private double bidAmount;
    private LocalDateTime bidTime;

    public BidTransaction() {}

    public BidTransaction(int id, Bidder bidder, Item auctionItem, double bidAmount, LocalDateTime bidTime) {
        this.id = id;
        this.bidder = bidder;
        this.auctionItem = auctionItem;
        this.bidAmount = bidAmount;
        this.bidTime = bidTime;
    }

    // --- Getters và Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Bidder getBidder() { return bidder; }
    public void setBidder(Bidder bidder) { this.bidder = bidder; }

    public Item getAuctionItem() { return auctionItem; }
    public void setAuctionItem(Item auctionItem) { this.auctionItem = auctionItem; }

    public double getBidAmount() { return bidAmount; }
    public void setBidAmount(double bidAmount) { this.bidAmount = bidAmount; }

    public LocalDateTime getBidTime() { return bidTime; }
    public void setBidTime(LocalDateTime bidTime) { this.bidTime = bidTime; }

    // --- HÀM HỖ TRỢ HIỂN THỊ UI ---
    // Hàm này giúp TableView hiển thị thời gian đẹp hơn
    public String getFormattedTime() {
        return bidTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    // Hàm lấy tên người đấu giá nhanh
    public String getBidderName() {
        return bidder != null ? bidder.getName() : "N/A";
    }

    // Hàm lấy tên sản phẩm nhanh
    public String getItemName() {
        return auctionItem != null ? auctionItem.getName() : "N/A";
    }

    @Override
    public String toString() {
        return "BidTransaction{" + "id=" + id + ", bidder=" + bidder.getName() +
                ", item=" + auctionItem.getName() + ", amount=" + bidAmount + '}';
    }
}