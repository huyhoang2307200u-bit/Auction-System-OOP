package com.auction.dto;

public class NotificationDto {
    private int id;
    private String recipientUsername;
    private String title;
    private String message;
    private boolean read;
    private String createdAt;

    public NotificationDto() {
    }

    public NotificationDto(int id, String recipientUsername, String title, String message, boolean read, String createdAt) {
        this.id = id;
        this.recipientUsername = recipientUsername;
        this.title = title;
        this.message = message;
        this.read = read;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getRecipientUsername() { return recipientUsername; }
    public void setRecipientUsername(String recipientUsername) { this.recipientUsername = recipientUsername; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
