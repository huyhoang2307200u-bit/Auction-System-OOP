package com.auction.model;

public class Item {
    private int id;
    private String name;
    private String description;
    private double startingPrice;
    private double currentPrice;

    public Item() {
    }

    public Item(int id, String name, String description, double startingPrice, double currentPrice) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentPrice = currentPrice;
    }
}