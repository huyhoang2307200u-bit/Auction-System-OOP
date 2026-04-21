package com.auction.service;

import com.auction.model.Item;
import com.auction.model.User;
import java.util.ArrayList;
import java.util.List;

public class AuctionManager {
    // BƯỚC 1: Tạo biến static để giữ đối tượng duy nhất (Singleton)
    private static AuctionManager instance;
    private List<Item> items = new ArrayList<>();

    // BƯỚC 2: Để private constructor để không ai bên ngoài tự tạo mới được
    private AuctionManager() {}

    // BƯỚC 3: Hàm lấy đối tượng duy nhất (Nhóm yêu cầu)
    public static synchronized AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        return instance;
    }

    public List<Item> getAvailableItems() {
        return items;
    }
    // Thêm hàm này để kiểm tra tài khoản
    public boolean checkLogin(String username, String password) {
        // Tạm thời để admin/123, sau này nhóm sẽ thay bằng database
        return "admin".equals(username) && "123".equals(password);
    }

    // BƯỚC 4: Hàm đặt giá - Logic cực kỳ quan trọng
    public synchronized void placeBid(String itemId, double amount, User bidder) throws Exception {
        for (Item item : items) {
            if (item.getId()) {
                // KIỂM TRA LOGIC: Nếu giá đặt thấp hơn hoặc bằng giá hiện tại -> Báo lỗi
                if (amount <= item.getCurrentPrice()) {
                    throw new Exception("Giá đặt phải lớn hơn " + item.getCurrentPrice());
                }
                
                // Nếu hợp lệ thì cập nhật giá mới
                item.setCurrentPrice(amount);
                
                // THÔNG BÁO CHO GUI (Observer)
                // Chỗ này bạn sẽ gọi hàm notify để màn hình của các bạn khác tự nhảy số
                System.out.println("Sản phẩm " + item.getName() + " vừa có giá mới: " + amount);
                return;
            }
        }
    }
}