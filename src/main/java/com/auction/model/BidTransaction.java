package com.auction.model;

import java.time.LocalDateTime;

public class BidTransaction {
    private int id;
    private Bidder bidder;
    private Auction auction;
    private double bidAmount;
    private LocalDateTime bidTime;

    public BidTransaction() {
    }

    public BidTransaction(int id, Bidder bidder, Auction auction, double bidAmount, LocalDateTime bidTime) {
        this.id = id;
        this.bidder = bidder;
        this.auction = auction;
        this.bidAmount = bidAmount;
        this.bidTime = bidTime;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Bidder getBidder() {
        return bidder;
    }

    public void setBidder(Bidder bidder) {
        this.bidder = bidder;
    }

    public Auction getAuction() {
        return auction;
    }

    public void setAuction(Auction auction) {
        this.auction = auction;
    }

    public double getBidAmount() {
        return bidAmount;
    }

    public void setBidAmount(double bidAmount) {
        this.bidAmount = bidAmount;
    }

    public LocalDateTime getBidTime() {
        return bidTime;
    }

    public void setBidTime(LocalDateTime bidTime) {
        this.bidTime = bidTime;
    }
    @Override
    public String toString() {
        return "BidTransaction{" +
                "id=" + id +
                ", bidder=" + bidder +
                ", auction=" + auction +
                ", bidAmount=" + bidAmount +
                ", bidTime=" + bidTime +
                '}';
    }
}