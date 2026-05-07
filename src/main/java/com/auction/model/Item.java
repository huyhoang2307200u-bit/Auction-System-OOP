package com.auction.model;

import java.math.BigDecimal;

public abstract class Item extends Entity {
    private static final long serialVersionUID = 1L;

    private String sellerId;
    private String title;
    private String description;
    private BigDecimal startingPrice;
    private ItemCategory category;
    private String imageDataUrl;

    protected Item() {
        super();
    }

    protected Item(String sellerId, String title, String description,
            BigDecimal startingPrice, ItemCategory category) {
        super();
        this.sellerId = sellerId;
        this.title = title;
        this.description = description;
        this.startingPrice = startingPrice;
        this.category = category;
        this.imageDataUrl = "";
    }

    public abstract String printInfo();

    public String getSellerId() {
        return sellerId;
    }

    public String getTitle() {
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
}
