package com.auction.model;

import java.math.BigDecimal;

public class Art extends Item {
    private static final long serialVersionUID = 1L;

    private String artist;
    private String material;

    public Art() {
        super();
    }

    public Art(String sellerId, String title, String description, BigDecimal startingPrice,
            String artist, String material) {
        super(sellerId, title, description, startingPrice, ItemCategory.ART);
        this.artist = artist;
        this.material = material;
    }

    @Override
    public String printInfo() {
        return getTitle() + " - artwork by " + artist + " on " + material;
    }

    public String getArtist() {
        return artist;
    }

    public String getMaterial() {
        return material;
    }
}
