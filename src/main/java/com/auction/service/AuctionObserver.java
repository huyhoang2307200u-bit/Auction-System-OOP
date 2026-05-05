package com.auction.service;

public interface AuctionObserver {
<<<<<<< HEAD
    // Hàm này sẽ được gọi mỗi khi có ai đó đặt giá thành công
    void update(int auctionId, double newPrice, String highestBidderName);
=======
    // Hàm này sẽ được gọi khi giá sản phẩm thay đổi
    void onPriceChanged(String itemId, double newPrice);
>>>>>>> 3fe42484d323085aa7f2422769a1b94c0309a976
}