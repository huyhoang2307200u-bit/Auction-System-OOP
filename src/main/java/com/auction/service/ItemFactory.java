package com.auction.service;

import com.auction.model.Item;

public class ItemFactory {
    // Hàm tạo sản phẩm dựa trên loại (Nhóm yêu cầu)
    public static Item createItem(String type, String name, double price) {
        // Sau này bạn có các lớp như ElectronicsItem, ArtItem kế thừa từ Item
        // Hiện tại bạn có thể trả về một logic mẫu hoặc null để tránh lỗi code
        if (type.equalsIgnoreCase("Electronics")) {
            // return new ElectronicsItem(name, price); 
        }
        return null; 
    }
}