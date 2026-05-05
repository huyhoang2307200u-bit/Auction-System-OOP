package com.auction.model;

public class ItemFactory {
    public static Item createItem(String type, String id, String name, double price) {
        if (type == null) return null;

        switch (type.toUpperCase()) {
            case "ELECTRONICS":
                return new Electronics(id, name, price);
            case "ART":
                return new Art(id, name, price);
            default:
                // Mặc định trả về Electronics nếu không xác định được loại
                return new Electronics(id, name, price);
        }
    }
}