package com.auction.service;

import com.auction.model.Auction;
import com.auction.model.Bidder;
import java.util.ArrayList;
import java.util.List;

public class AuctionManager {
    private static AuctionManager instance;
    private List<Auction> auctions = new ArrayList<>(); // Quản lý danh sách các phiên

    private AuctionManager() {}

    public static synchronized AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        return instance;
    }

    // BƯỚC 4: Logic đặt giá - Đã tích hợp Observer và Kiểm tra hợp lệ
    public synchronized void placeBid(int auctionId, double amount, Bidder bidder) throws Exception {
        for (Auction auction : auctions) {
            if (auction.getId() == auctionId) {
                
                // 1. KIỂM TRA TRẠNG THÁI: Phiên phải đang hoạt động
                if (!auction.isActive()) {
                    throw new Exception("Phiên đấu giá này hiện đã kết thúc!");
                }

                // 2. KIỂM TRA BƯỚC GIÁ: Giá mới >= Giá hiện tại + Bước giá tối thiểu
                double minRequired = auction.getHighestBid() + auction.getMinIncrement();
                if (amount < minRequired) {
                    throw new Exception("Giá đặt phải lớn hơn hoặc bằng: " + minRequired);
                }

                // 3. CẬP NHẬT DỮ LIỆU: Nếu hợp lệ thì ghi nhận người dẫn đầu mới
                auction.setHighestBid(amount);
                auction.setHighestBidder(bidder);

                // 4. KÍCH HOẠT OBSERVER: Đây là phần quan trọng nhất của Người A
                // Hàm này sẽ báo cho toàn bộ các màn hình UI tự động cập nhật số mới
                auction.notifyObservers();
                
                System.out.println("Đặt giá thành công cho phiên: " + auctionId);
                return;
            }
        }
        throw new Exception("Không tìm thấy phiên đấu giá này!");
    }
    public void createAuction(Auction auction) {
    // Logic: Kiểm tra nếu item chưa có trong phiên nào khác thì mới cho tạo
    if (auction != null && !auctions.contains(auction)) {
        auctions.add(auction);
        System.out.println("Đã tạo phiên đấu giá cho sản phẩm: " + auction.getItem().getName());
    }
}
}