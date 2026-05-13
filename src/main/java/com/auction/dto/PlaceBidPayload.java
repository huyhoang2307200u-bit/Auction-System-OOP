package com.auction.dto;

import java.math.BigDecimal;

public class PlaceBidPayload {
    private String auctionId;
    private BigDecimal amount;

    public PlaceBidPayload() {
    }

    public PlaceBidPayload(String auctionId, BigDecimal amount) {
        this.auctionId = auctionId;
        this.amount = amount;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}