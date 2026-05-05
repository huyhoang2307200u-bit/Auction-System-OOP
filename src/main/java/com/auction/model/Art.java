package com.auction.model;

public class Art extends Item {

    public Art(String id, String name, double price) {
        // Gán mô tả mặc định cho tác phẩm nghệ thuật
        super(id, name, "Tác phẩm nghệ thuật sưu tầm", price, price);
    }

    // Ghi đè phương thức getItemType[cite: 1]
    @Override
    public String getItemType() {
        return "Nghệ thuật";
    }
}