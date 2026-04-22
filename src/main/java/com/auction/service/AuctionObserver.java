package com.auction.service;

public interface AuctionObserver {
    // Hàm này sẽ được gọi mỗi khi có ai đó đặt giá thành công
    void update(int auctionId, double newPrice, String highestBidderName);
}