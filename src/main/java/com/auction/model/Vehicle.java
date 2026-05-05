package com.example.auction.model;

import java.math.BigDecimal;

public class Vehicle extends Item {
    private static final long serialVersionUID = 1L;

    private String manufacturer;
    private int year;

    public Vehicle() {
        super();
    }

    public Vehicle(String sellerId, String title, String description, BigDecimal startingPrice,
            String manufacturer, int year) {
        super(sellerId, title, description, startingPrice, ItemCategory.VEHICLE);
        this.manufacturer = manufacturer;
        this.year = year;
    }

    @Override
    public String printInfo() {
        return getTitle() + " - " + manufacturer + " " + year;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public int getYear() {
        return year;
    }
}
