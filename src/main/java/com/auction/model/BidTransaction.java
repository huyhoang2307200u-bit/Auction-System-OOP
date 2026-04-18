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
}