package com.auction.model;

import com.auction.util.MoneyUtil;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public abstract class Item extends Entity {
    private static final long serialVersionUID = 1L;

    private String sellerId;
    private String title;
    private String description;
    private BigDecimal startingPrice;
    private ItemCategory category;
    private String imageDataUrl;

    // Các thuộc tính dưới đây được thêm để tương thích với code GUI cũ
    // đang gọi getCurrentPrice(), isAuctionActive(), getEndTime(),...
    private BigDecimal currentPrice;
    private String highestBidderName;
    private boolean auctionActive;
    private LocalDateTime endTime;

    protected Item() {
        super();
        this.imageDataUrl = "";
        this.startingPrice = BigDecimal.ZERO;
        this.currentPrice = BigDecimal.ZERO;
        this.highestBidderName = "";
        this.auctionActive = true;
    }

    protected Item(String sellerId, String title, String description,
                   BigDecimal startingPrice, ItemCategory category) {
        super();
        this.sellerId = sellerId;
        this.title = title;
        this.description = description;
        this.startingPrice = MoneyUtil.normalize(startingPrice);
        this.currentPrice = this.startingPrice;
        this.category = category;
        this.imageDataUrl = "";
        this.highestBidderName = "";
        this.auctionActive = true;
    }

    public abstract String printInfo();

    public String getSellerId() {
        return sellerId;
    }

    public String getTitle() {
        return title;
    }

    // Tên cũ mà Controller đang dùng
    public String getName() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getStartingPrice() {
        return startingPrice;
    }

    public ItemCategory getCategory() {
        return category;
    }

    public String getImageDataUrl() {
        return imageDataUrl;
    }

    public void setImageDataUrl(String imageDataUrl) {
        this.imageDataUrl = imageDataUrl == null ? "" : imageDataUrl;
    }

    public double getCurrentPrice() {
        return MoneyUtil.toDouble(currentPrice);
    }

    public BigDecimal getCurrentPriceValue() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = MoneyUtil.fromDouble(currentPrice);
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = MoneyUtil.normalize(currentPrice);
    }

    public String getHighestBidderName() {
        return highestBidderName;
    }

    public void setHighestBidderName(String highestBidderName) {
        this.highestBidderName = highestBidderName == null ? "" : highestBidderName;
    }

    public boolean isAuctionActive() {
        return auctionActive;
    }

    public void setAuctionActive(boolean auctionActive) {
        this.auctionActive = auctionActive;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return auctionActive ? "ĐANG MỞ" : "ĐÃ KẾT THÚC";
    }
}
