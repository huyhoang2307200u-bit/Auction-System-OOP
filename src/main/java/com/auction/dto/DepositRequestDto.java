package com.auction.dto;

public class DepositRequestDto {
    private int id;
    private String username;
    private double amount;
    private String status;
    private String requestedAt;
    private String reviewedBy;
    private String reviewedAt;
    private String rejectionReason;

    public DepositRequestDto() {
    }

    public DepositRequestDto(int id, String username, double amount, String status,
                             String requestedAt, String reviewedBy, String reviewedAt, String rejectionReason) {
        this.id = id;
        this.username = username;
        this.amount = amount;
        this.status = status;
        this.requestedAt = requestedAt;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = reviewedAt;
        this.rejectionReason = rejectionReason;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(String requestedAt) {
        this.requestedAt = requestedAt;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(String reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    @Override
    public String toString() {
        return "DepositRequestDto{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", amount=" + amount +
                ", status='" + status + '\'' +
                ", requestedAt='" + requestedAt + '\'' +
                '}';
    }
}
