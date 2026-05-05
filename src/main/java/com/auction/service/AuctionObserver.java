package com.auction.service;

public interface AuctionObserver {
    // Hàm này sẽ được gọi khi giá sản phẩm thay đổi
    void onPriceChanged(String itemId, double newPrice);
}