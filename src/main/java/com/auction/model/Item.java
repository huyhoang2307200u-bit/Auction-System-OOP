package com.auction.model;

import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;

import java.time.LocalDateTime;

public abstract class Item {
    protected String id;
    protected String name;
    protected String description;
    protected double startingPrice;
    protected double currentPrice;
    protected LocalDateTime endTime;
    protected User lastBidder;
    protected boolean isAuctionActive = true;
    protected String highestBidderName = "Chưa có";

    // Constructor chung giữ nguyên tham số của bạn
    public Item(String id, String name, String description, double startingPrice, double currentPrice) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentPrice = currentPrice;
        this.isAuctionActive = true;
    }

    public javafx.beans.property.StringProperty idProperty() {
        return new javafx.beans.property.SimpleStringProperty(id);
    }

    public javafx.beans.property.StringProperty nameProperty() {
        return new javafx.beans.property.SimpleStringProperty(name);
    }

    public javafx.beans.property.DoubleProperty currentPriceProperty() {
        return new javafx.beans.property.SimpleDoubleProperty(currentPrice);
    }

    public javafx.beans.property.StringProperty statusProperty() {
        return new javafx.beans.property.SimpleStringProperty(getStatus());
    }

    public javafx.beans.property.StringProperty highestBidderProperty() {
        return new javafx.beans.property.SimpleStringProperty(highestBidderName);
    }



    // --- KẾT THÚC PHẦN BỔ SUNG ---

    public abstract String getItemType();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getStartingPrice() { return startingPrice; }
    public void setStartingPrice(double startingPrice) { this.startingPrice = startingPrice; }
    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public User getLastBidder() { return lastBidder; }
    public void setLastBidder(User lastBidder) { this.lastBidder = lastBidder; }
    public boolean isAuctionActive() { return isAuctionActive; }
    public void setAuctionActive(boolean active) { this.isAuctionActive = active; }
    public String getStatus() { return isAuctionActive ? "Đang đấu giá" : "Đã kết thúc"; }
    public String getHighestBidderName() { return highestBidderName; }
    public void setHighestBidderName(String name) { this.highestBidderName = name; }


}