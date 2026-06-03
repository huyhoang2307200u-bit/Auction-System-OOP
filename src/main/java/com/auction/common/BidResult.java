package com.auction.common;

public class BidResult {
    private boolean success;
    private String message;
    private Double currentPrice;

    private boolean timeExtended;

    public BidResult() {
    }

    public BidResult(boolean success, String message, Double currentPrice) {
        this.success = success;
        this.message = message;
        this.currentPrice = currentPrice;
        this.timeExtended = false;
    }

    public BidResult(boolean success, String message, Double currentPrice, boolean timeExtended) {
        this.success = success;
        this.message = message;
        this.currentPrice = currentPrice;
        this.timeExtended = timeExtended;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(Double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public boolean isTimeExtended() {
        return timeExtended;
    }

    public void setTimeExtended(boolean timeExtended) {
        this.timeExtended = timeExtended;
    }

    @Override
    public String toString() {
        return "BidResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", currentPrice=" + currentPrice +
                ", timeExtended=" + timeExtended +
                '}';
    }
}