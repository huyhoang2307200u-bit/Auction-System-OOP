package com.auction.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Notification extends Entity {
    private static final long serialVersionUID = 1L;

    private String recipientUsername;
    private String title;
    private String message;
    private LocalDateTime createdAtLocal;
    private boolean read;

    public Notification(String recipientUsername, String title, String message) {
        super();
        if (recipientUsername == null || recipientUsername.isBlank()) {
            throw new IllegalArgumentException("Người nhận thông báo không hợp lệ.");
        }
        this.recipientUsername = recipientUsername.trim();
        this.title = title == null || title.isBlank() ? "Thông báo" : title.trim();
        this.message = message == null ? "" : message.trim();
        this.createdAtLocal = LocalDateTime.now();
        this.read = false;
    }

    public String getRecipientUsername() {
        return recipientUsername;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAtLocal() {
        return createdAtLocal;
    }

    public boolean isRead() {
        return read;
    }

    public void markRead() {
        this.read = true;
    }

    public String getFormattedCreatedAt() {
        LocalDateTime safeTime = createdAtLocal == null ? LocalDateTime.now() : createdAtLocal;
        return safeTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }
}
