package com.example.auction.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AutoBidConfig extends Entity {
    private static final long serialVersionUID = 1L;

    private String auctionId;
    private String bidderId;
    private BigDecimal maxBid;
    private BigDecimal increment;
    private LocalDateTime registeredAt;
    private long priorityOrder;
    private boolean active = true;

    public AutoBidConfig() {
        super();
    }

    public AutoBidConfig(String auctionId, String bidderId, BigDecimal maxBid,
            BigDecimal increment, LocalDateTime registeredAt, long priorityOrder) {
        super();
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.maxBid = maxBid;
        this.increment = increment;
        this.registeredAt = registeredAt;
        this.priorityOrder = priorityOrder;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getBidderId() {
        return bidderId;
    }

    public BigDecimal getMaxBid() {
        return maxBid;
    }

    public BigDecimal getIncrement() {
        return increment;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public long getPriorityOrder() {
        return priorityOrder;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        active = false;
    }
}
