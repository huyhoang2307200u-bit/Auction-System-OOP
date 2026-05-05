package com.auction.dto;

import com.auction.model.AuctionStatus;
import com.auction.model.ItemCategory;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AuctionSummaryDto {
    private String auctionId;
    private String title;
    private ItemCategory category;
    private BigDecimal currentPrice;
    private AuctionStatus status;
    private LocalDateTime endTime;
    private String sellerName;
    private String winnerName;
    private String imageDataUrl;

    public AuctionSummaryDto() {
    }

    public AuctionSummaryDto(String auctionId, String title, ItemCategory category,
                             BigDecimal currentPrice, AuctionStatus status, LocalDateTime endTime,
                             String sellerName, String winnerName, String imageDataUrl) {
        this.auctionId = auctionId;
        this.title = title;
        this.category = category;
        this.currentPrice = currentPrice;
        this.status = status;
        this.endTime = endTime;
        this.sellerName = sellerName;
        this.winnerName = winnerName;
        this.imageDataUrl = imageDataUrl;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ItemCategory getCategory() {
        return category;
    }

    public void setCategory(ItemCategory category) {
        this.category = category;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public void setWinnerName(String winnerName) {
        this.winnerName = winnerName;
    }

    public String getImageDataUrl() {
        return imageDataUrl;
    }

    public void setImageDataUrl(String imageDataUrl) {
        this.imageDataUrl = imageDataUrl;
    }
}
