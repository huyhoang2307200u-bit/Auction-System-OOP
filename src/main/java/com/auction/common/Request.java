package com.auction.common;

public class Request {
    private RequestType type;

    private String message;

    private String username;
    private String password;
    private String displayName;
    private String role;

    private Integer auctionId;
    private Integer depositRequestId;
    private Integer notificationId;
    private Double amount;

    private String itemName;
    private String description;
    private String category;
    private Integer durationMinutes;
    private Double maxBid;
    private Double increment;
    private String rejectionReason;
    private String imageDataUrl;

    public Request() {
    }

    public RequestType getType() {
        return type;
    }

    public void setType(RequestType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(Integer auctionId) {
        this.auctionId = auctionId;
    }

    public Integer getDepositRequestId() {
        return depositRequestId;
    }

    public void setDepositRequestId(Integer depositRequestId) {
        this.depositRequestId = depositRequestId;
    }

    public Integer getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Integer notificationId) {
        this.notificationId = notificationId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Double getMaxBid() {
        return maxBid;
    }

    public void setMaxBid(Double maxBid) {
        this.maxBid = maxBid;
    }

    public Double getIncrement() {
        return increment;
    }

    public void setIncrement(Double increment) {
        this.increment = increment;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getImageDataUrl() {
        return imageDataUrl;
    }

    public void setImageDataUrl(String imageDataUrl) {
        this.imageDataUrl = imageDataUrl;
    }

    @Override
    public String toString() {
        return "Request{" +
                "type=" + type +
                ", message='" + message + '\'' +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", auctionId=" + auctionId +
                ", depositRequestId=" + depositRequestId +
                ", notificationId=" + notificationId +
                ", amount=" + amount +
                ", itemName='" + itemName + '\'' +
                '}';
    }
}
