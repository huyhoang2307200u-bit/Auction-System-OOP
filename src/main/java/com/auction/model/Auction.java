package com.auction.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Auction extends Entity {
    private static final long serialVersionUID = 1L;

    private Item item;
    private String sellerId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private AuctionStatus status;
    private BigDecimal currentPrice;
    private String winnerBidderId;
    private List<BidTransaction> bidHistory;
    private int antiSnipingThresholdSeconds = 30;
    private int antiSnipingExtensionSeconds = 60;
    private transient ReentrantLock lock = new ReentrantLock(true);

    public Auction() {
        super();
        this.bidHistory = new ArrayList<>();
    }

    public Auction(Item item, String sellerId, LocalDateTime startTime, LocalDateTime endTime) {
        super();
        this.item = item;
        this.sellerId = sellerId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = AuctionStatus.OPEN;
        this.currentPrice = item.getStartingPrice();
        this.bidHistory = new ArrayList<>();
    }

    public void refreshStatus(LocalDateTime now) {
        if (status == AuctionStatus.CANCELED || status == AuctionStatus.PAID) {
            return;
        }
        if (!now.isBefore(endTime)) {
            status = AuctionStatus.FINISHED;
            return;
        }
        if (!now.isBefore(startTime) && status == AuctionStatus.OPEN) {
            status = AuctionStatus.RUNNING;
        }
    }

    public boolean canAcceptBid(LocalDateTime now) {
        refreshStatus(now);
        return status == AuctionStatus.RUNNING;
    }

    public void applyBid(BidTransaction bid) {
        currentPrice = bid.getAmount();
        winnerBidderId = bid.getBidderId();
        bidHistory.add(bid);
    }

    public void extendEndTimeBySeconds(int seconds) {
        endTime = endTime.plusSeconds(seconds);
    }

    public ReentrantLock getLock() {
        return lock;
    }

    public Item getItem() {
        return item;
    }

    public String getSellerId() {
        return sellerId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public String getWinnerBidderId() {
        return winnerBidderId;
    }

    public List<BidTransaction> getBidHistory() {
        return Collections.unmodifiableList(bidHistory);
    }

    public int getAntiSnipingThresholdSeconds() {
        return antiSnipingThresholdSeconds;
    }

    public void setAntiSnipingThresholdSeconds(int antiSnipingThresholdSeconds) {
        this.antiSnipingThresholdSeconds = antiSnipingThresholdSeconds;
    }

    public int getAntiSnipingExtensionSeconds() {
        return antiSnipingExtensionSeconds;
    }

    public void setAntiSnipingExtensionSeconds(int antiSnipingExtensionSeconds) {
        this.antiSnipingExtensionSeconds = antiSnipingExtensionSeconds;
    }

    public void cancel() {
        status = AuctionStatus.CANCELED;
    }

    private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        inputStream.defaultReadObject();
        lock = new ReentrantLock(true);
        if (bidHistory == null) {
            bidHistory = new ArrayList<>();
        }
    }
}
