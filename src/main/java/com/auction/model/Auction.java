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
}