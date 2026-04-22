package com.auction.common;

public class Request {
    private String action;
    private String message;
    private String username;
    private String password;

    public Request() {
    }

    public Request(String action, String message) {
        this.action = action;
        this.message = message;
    }

    public Request(String action, String username, String password) {
        this.action = action;
        this.username = username;
        this.password = password;
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

    @Override
    public String toString() {
        return "Request{" +
                "action='" + action + '\'' +
                ", message='" + message + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}