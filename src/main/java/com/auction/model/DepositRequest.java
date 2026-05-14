package com.auction.model;

import java.time.LocalDateTime;

public class DepositRequest extends Entity {
    private final String username;
    private final String displayName;
    private final double amount;
    private DepositRequestStatus status;
    private final LocalDateTime requestedAt;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private String rejectionReason;

    public DepositRequest(String username, String displayName, double amount) {
        super();
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username không được để trống.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Số tiền nạp phải lớn hơn 0.");
        }
        this.username = username.trim();
        this.displayName = displayName == null || displayName.isBlank() ? this.username : displayName.trim();
        this.amount = amount;
        this.status = DepositRequestStatus.PENDING;
        this.requestedAt = LocalDateTime.now();
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getAmount() {
        return amount;
    }

    public DepositRequestStatus getStatus() {
        return status;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public boolean isPending() {
        return status == DepositRequestStatus.PENDING;
    }

    public void approve(String adminUsername) {
        ensurePending();
        this.status = DepositRequestStatus.APPROVED;
        this.reviewedBy = adminUsername;
        this.reviewedAt = LocalDateTime.now();
        this.rejectionReason = null;
    }

    public void reject(String adminUsername, String reason) {
        ensurePending();
        this.status = DepositRequestStatus.REJECTED;
        this.reviewedBy = adminUsername;
        this.reviewedAt = LocalDateTime.now();
        this.rejectionReason = reason == null || reason.isBlank() ? "Không có lý do cụ thể." : reason.trim();
    }

    private void ensurePending() {
        if (!isPending()) {
            throw new IllegalStateException("Yêu cầu nạp tiền đã được xử lý trước đó.");
        }
    }
}
