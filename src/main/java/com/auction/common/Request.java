package com.auction.common;

public class Request {
    private String action;
    private String message;

    public Request() {
    }

    public Request(String action, String message) {
        this.action = action;
        this.message = message;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Request{" +
                "action='" + action + '\'' +
                ", message='" + message + '\'' +
                '}';
    }

}
