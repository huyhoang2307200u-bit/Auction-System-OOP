package com.auction.service;

import com.auction.model.Item;
import com.auction.model.User;

/**
 * BidService: Chịu trách nhiệm xử lý logic đấu giá.
 * Đây là tầng trung gian để tách biệt logic đấu giá khỏi giao diện.
 */
public class BidService {

    /**
     * Thực hiện đặt giá đấu cho một sản phẩm.
     * @param item Sản phẩm cần đấu giá
     * @param bidder Người dùng đang đặt giá
     * @param bidAmount Số tiền đặt giá
     * @return true nếu đặt giá thành công, false nếu giá không hợp lệ
     */
    public boolean placeBid(Item item, User bidder, double bidAmount) {
        // 1. Kiểm tra tính hợp lệ của giá đấu
        if (bidAmount <= item.getCurrentPrice()) {
            // Trong thực tế,sẽ trả về thông báo lỗi chi tiết hơn hoặc throw Exception
            return false;
        }

        // 2. Cập nhật giá mới cho sản phẩm
        item.setCurrentPrice(bidAmount);

        // 3. Ghi lại người vừa đấu giá thành công (cần thêm thuộc tính này vào class Item)
        // item.setLastBidder(bidder);

        return true;
    }
}