package com.auction.service;

import com.auction.model.Item;
import com.auction.model.User;
import com.auction.model.Bidder;
import com.auction.model.BidTransaction;
import java.time.LocalDateTime;

public class BidService {

    public boolean placeBid(Item item, User user, double amount) {
        // 1. Kiểm tra phiên đã kết thúc chưa
        if (!item.isAuctionActive()) return false;

        // 2. Kiểm tra giá đặt phải cao hơn giá hiện tại
        if (amount <= item.getCurrentPrice()) return false;

        // 3. Cập nhật giá và ghi nhật ký
        synchronized(item) {
            item.setCurrentPrice(amount);
            item.setHighestBidderName(user.getName());

            try {
                // CHỈNH SỬA TẠI ĐÂY:
                // Nếu user không phải Bidder (như Admin), chúng ta vẫn thực hiện ghi log
                // nhưng xử lý ép kiểu cẩn thận.

                Bidder bidderForLog = null;
                if (user instanceof Bidder) {
                    bidderForLog = (Bidder) user;
                }

                // Tạo giao dịch mới
                // Nếu dự án của bạn lớp BidTransaction cho phép Bidder bị null (trong trường hợp Admin đấu giá)
                // Hoặc bạn chỉ cần ghi log khi người đó là Bidder thật sự.
                if (bidderForLog != null) {
                    BidTransaction newTrans = new BidTransaction(
                            (int) (System.currentTimeMillis() / 1000),
                            bidderForLog,
                            item,
                            amount,
                            LocalDateTime.now()
                    );
                    TransactionManager.getInstance().addTransaction(newTrans);
                    System.out.println(" Đã ghi log giao dịch!");
                } else {
                    // Nếu là Admin, bạn có thể tạo một đối tượng Bidder mặc định
                    // mà không cần truyền tham số nếu Constructor 2 tham số chưa tồn tại
                    System.out.println("User không phải Bidder, không ghi log vào bảng lịch sử người dùng.");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}