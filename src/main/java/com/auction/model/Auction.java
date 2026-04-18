package com.auction.model;

import java.time.LocalDateTime;

public class Auction {
    private int id;
    private Item item;
    private Bidder highestBidder;
    private double highestBid;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean active;

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

}