package com.auction.dto;

import java.math.BigDecimal;

public class AutoBidPayload {
    private String auctionId;
    private BigDecimal maxBid;
    private BigDecimal increment;

    public AutoBidPayload() {
    }

    public AutoBidPayload(String auctionId, BigDecimal maxBid, BigDecimal increment) {
        this.auctionId = auctionId;
        this.maxBid = maxBid;
        this.increment = increment;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public BigDecimal getMaxBid() {
        return maxBid;
    }

    public void setMaxBid(BigDecimal maxBid) {
        this.maxBid = maxBid;
    }

    public BigDecimal getIncrement() {
        return increment;
    }

    public void setIncrement(BigDecimal increment) {
        this.increment = increment;
    }
}
