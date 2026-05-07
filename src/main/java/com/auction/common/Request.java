package com.auction.common;

public class Request {
    private RequestType type;
    private String message;
    private String username;
    private String password;
    private Integer auctionId;
    private Double amount;

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

    public Integer getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(Integer auctionId) {
        this.auctionId = auctionId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "Request{" +
                "type=" + type +
                ", message='" + message + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", auctionId=" + auctionId +
                ", amount=" + amount +
                '}';
    }
}