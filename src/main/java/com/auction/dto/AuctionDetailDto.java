package com.auction.dto;

import com.auction.model.AuctionStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuctionDetailDto {
    private String auctionId;
    private ItemDto item;
    private String sellerId;
    private String sellerName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private AuctionStatus status;
    private BigDecimal currentPrice;
    private String winnerBidderId;
    private String winnerName;
    private List<BidDto> bidHistory = new ArrayList<>();

    public AuctionDetailDto() {
    }

    public AuctionDetailDto(String auctionId, ItemDto item, String sellerId, String sellerName,
                            LocalDateTime startTime, LocalDateTime endTime, AuctionStatus status,
                            BigDecimal currentPrice, String winnerBidderId, String winnerName,
                            List<BidDto> bidHistory) {
        this.auctionId = auctionId;
        this.item = item;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.currentPrice = currentPrice;
        this.winnerBidderId = winnerBidderId;
        this.winnerName = winnerName;
        this.bidHistory = bidHistory;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public ItemDto getItem() {
        return item;
    }

    public void setItem(ItemDto item) {
        this.item = item;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
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

    public AuctionStatus getStatus() {
        return status;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public String getWinnerBidderId() {
        return winnerBidderId;
    }

    public void setWinnerBidderId(String winnerBidderId) {
        this.winnerBidderId = winnerBidderId;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public void setWinnerName(String winnerName) {
        this.winnerName = winnerName;
    }

    public List<BidDto> getBidHistory() {
        return bidHistory;
    }

    public void setBidHistory(List<BidDto> bidHistory) {
        this.bidHistory = bidHistory;
    }
}
