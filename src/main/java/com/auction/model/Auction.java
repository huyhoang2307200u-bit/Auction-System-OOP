package com.auction.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.auction.service.AuctionObserver;

public class Auction {
    private int id;
    private Item item;
    private Bidder highestBidder;
    private double highestBid;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean active;
    private double minIncrement;
    private java.util.List<com.auction.service.AuctionObserver> observers = new java.util.ArrayList<>();
    public Auction() {
    }

    public Auction(int id, Item item, Bidder highestBidder, double highestBid,
                   LocalDateTime startTime, LocalDateTime endTime, boolean active) {
        this.id = id;
        this.item = item;
        this.highestBidder = highestBidder;
        this.highestBid = highestBid;
        this.startTime = startTime;
        this.endTime = endTime;
        this.active = active;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Bidder getHighestBidder() {
        return highestBidder;
    }

    public void setHighestBidder(Bidder highestBidder) {
        this.highestBidder = highestBidder;
    }

    public double getHighestBid() {
        return highestBid;
    }

    public void setHighestBid(double highestBid) {
        this.highestBid = highestBid;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
    @Override
    public String toString() {
        return "Auction{" +
                "id=" + id +
                ", item=" + item +
                ", highestBidder=" + highestBidder +
                ", highestBid=" + highestBid +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", active=" + active +
                '}';
    }

    // Phương thức để các Controller/UI đăng ký lắng nghe
    public void addObserver(com.auction.service.AuctionObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    // Phương thức thông báo cho tất cả khi có giá cao nhất mới
    public void notifyObservers() {
        for (com.auction.service.AuctionObserver observer : observers) {
            // Truyền ID, Giá mới và tên người đặt giá cao nhất cho UI cập nhật
            observer.update(this.id, this.highestBid, 
                (highestBidder != null ? highestBidder.getUsername() : "Chưa có"));
        }
    }

    // Getter và Setter cho bước giá (minIncrement)
    public double getMinIncrement() { return minIncrement; }
    public void setMinIncrement(double minIncrement) { this.minIncrement = minIncrement; }
}