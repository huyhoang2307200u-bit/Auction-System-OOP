package com.auction.dto;

public class AuctionIdPayload {
    private String auctionId;

    public AuctionIdPayload() {
    }

    public AuctionIdPayload(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }
}
