package com.auction.model;

public class Electronics extends Item {
    public Electronics(String id, String name, double price) {
        // Gán mô tả mặc định cho đồ điện tử
        super(id, name, "Sản phẩm thiết bị điện tử", price, price);
    }

    // Ghi đè phương thức getItemType để trả về đúng loại
    @Override
    public String getItemType() {
        return "Điện tử";
    }
}