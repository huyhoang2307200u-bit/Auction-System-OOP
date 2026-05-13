package com.auction.model;

import java.math.BigDecimal;

public class Electronics extends Item {
    private static final long serialVersionUID = 1L;

    private String brand;
    private int warrantyMonths;

    public Electronics() {
        super();
    }

    public Electronics(String sellerId, String title, String description, BigDecimal startingPrice,
            String brand, int warrantyMonths) {
        super(sellerId, title, description, startingPrice, ItemCategory.ELECTRONICS);
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
    }

    @Override
    public String printInfo() {
        return getTitle() + " - " + brand + ", warranty " + warrantyMonths + " months";
    }

    public String getBrand() {
        return brand;
    }

    public int getWarrantyMonths() {
        return warrantyMonths;
    }
}
