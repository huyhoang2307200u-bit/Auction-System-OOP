package com.auction.service;

import com.auction.dao.ItemDAO;
import com.auction.dao.TransactionDAO; // Thêm DAO mới
import com.auction.model.Item;
import com.auction.model.User;
import com.auction.model.Bidder;
import com.auction.model.BidTransaction;
import java.time.LocalDateTime;

public class BidService {

    // Khởi tạo các thành phần truy xuất dữ liệu
    private final ItemDAO itemDAO = new ItemDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    /**
     * Xử lý logic đặt giá và lưu vết giao dịch
     */
    public boolean placeBid(Item item, User user, double amount) {
        // 1. Xác thực trạng thái hoạt động của phiên đấu giá
        if (!item.isAuctionActive()) {
            System.out.println(">>> Thông báo: Phiên đấu giá đã đóng.");
            return false;
        }

        // 2. Kiểm tra tính hợp lệ của giá đặt mới (phải cao hơn giá hiện hành)
        if (amount <= item.getCurrentPrice()) {
            System.out.println(">>> Thông báo: Giá đặt phải cao hơn giá hiện tại.");
            return false;
        }

        // 3. Tiến hành cập nhật đồng bộ để tránh xung đột dữ liệu khi nhiều người cùng đặt giá
        synchronized(item) {
            try {
                // A. CẬP NHẬT TRẠNG THÁI SẢN PHẨM TRÊN CƠ SỞ DỮ LIỆU
                itemDAO.updatePrice(item.getId(), amount);

                // B. CẬP NHẬT THÔNG TIN TRÊN BỘ NHỚ TẠM (RAM) ĐỂ HIỂN THỊ GIAO DIỆN
                item.setCurrentPrice(amount);
                item.setHighestBidderName(user.getName());

                // C. LƯU LẠI LỊCH SỬ GIAO DỊCH VÀO DATABASE (Vĩnh viễn)
                // Lưu ID người dùng, ID sản phẩm và số tiền đặt
                transactionDAO.saveTransaction(user.getId(), Integer.parseInt(item.getId()), amount);

                // D. GHI LOG CHO TransactionManager (Để hỗ trợ các tính năng liệt kê nhanh)
                if (user instanceof Bidder) {
                    Bidder bidderForLog = (Bidder) user;
                    BidTransaction newTrans = new BidTransaction(
                            (int) (System.currentTimeMillis() / 1000),
                            bidderForLog,
                            item,
                            amount,
                            LocalDateTime.now()
                    );
                    TransactionManager.getInstance().addTransaction(newTrans);
                }

                System.out.println(">>> Giao dịch thành công: Sản phẩm ID " + item.getId() + " đạt mức giá " + amount);
                return true;

            } catch (Exception e) {
                System.err.println(">>> Lỗi hệ thống khi xử lý đặt giá: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
    }
}