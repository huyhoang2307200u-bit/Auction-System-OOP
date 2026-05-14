package com.auction.model;

import java.time.LocalDateTime;

public class ElectronicItem extends Item {

    public ElectronicItem(String id, String name, String description, double startingPrice, double currentPrice) {
        super(id, name, description, startingPrice, currentPrice);
        this.endTime = LocalDateTime.now().plusDays(7);
    }

    // Thực hiện tính đa hình: Định nghĩa loại mặt hàng cụ thể
    @Override
    public String getItemType() {
        return "Đồ điện tử";
    }
}