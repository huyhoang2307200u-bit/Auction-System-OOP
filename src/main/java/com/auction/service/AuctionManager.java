package com.auction.service;

import com.auction.model.Admin;
import com.auction.model.Bidder;
import com.auction.model.Item;
import com.auction.model.User;
import com.auction.model.ItemFactory; // Import Factory để tạo mẫu dữ liệu

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.time.LocalDateTime;

public class AuctionManager {
    private static AuctionManager instance;

    private final List<Item> items = new ArrayList<>();
    private final List<AuctionObserver> observers = new ArrayList<>();

    // Sử dụng ConcurrentHashMap để lưu Lock riêng cho từng Item ID
    private final Map<String, ReentrantLock> itemLocks = new ConcurrentHashMap<>();

    // Thread pool để quét kết thúc đấu giá tự động (Yêu cầu 3.1.4)
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private AuctionManager() {
        // Sử dụng ItemFactory để tạo dữ liệu mẫu (Đúng chuẩn Design Pattern 3.6)
        Item item1 = ItemFactory.createItem("ART", "SP01", "Đồng hồ cổ", 500.0);
        item1.setEndTime(LocalDateTime.now().plusMinutes(5));
        items.add(item1);

        Item item2 = ItemFactory.createItem("ELECTRONICS", "SP02", "Laptop Gaming", 1200.0);
        item2.setEndTime(LocalDateTime.now().plusMinutes(10));
        items.add(item2);

        // Chạy nhiệm vụ quét thời gian mỗi giây một lần
        scheduler.scheduleAtFixedRate(this::checkAndEndAuctions, 0, 1, TimeUnit.SECONDS);
    }

    public static synchronized AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        return instance;
    }

    public void addObserver(AuctionObserver observer) {
        observers.add(observer);
    }

    private void notifyObservers(String itemId, double newPrice) {
        for (AuctionObserver observer : observers) {
            observer.onPriceChanged(itemId, newPrice);
        }
    }

    public List<Item> getAvailableItems() {
        return items;
    }

    public void addNewItem(Item item) {
        items.add(item);
        notifyObservers(item.getId(), item.getCurrentPrice());
    }

    // --- XỬ LÝ ĐẤU GIÁ ĐỒNG THỜI (CONCURRENCY - MỤC 3.2.2) ---
    public void placeBid(String itemId, double amount, User bidder) throws Exception {
        // Lấy hoặc tạo mới Lock cho Item này[cite: 1]
        ReentrantLock lock = itemLocks.computeIfAbsent(itemId, k -> new ReentrantLock());

        lock.lock(); // Ngăn chặn các Thread khác sửa đổi giá cùng lúc[cite: 1]
        try {
            Item item = findItemById(itemId);
            if (item == null) throw new Exception("Không tìm thấy sản phẩm!");

            // Kiểm tra các ràng buộc nghiệp vụ (Yêu cầu 3.1.5)[cite: 1]
            if (!item.isAuctionActive()) {
                throw new Exception("Phiên đấu giá đã kết thúc!");
            }
            if (amount <= item.getCurrentPrice()) {
                throw new Exception("Giá đặt " + amount + " phải lớn hơn giá hiện tại " + item.getCurrentPrice());
            }

            // Cập nhật thông tin dẫn đầu an toàn[cite: 1]
            item.setCurrentPrice(amount);
            item.setHighestBidderName(bidder.getName());

            // Thông báo cập nhật giao diện ngay lập tức (Realtime Update 3.2.4)[cite: 1]
            notifyObservers(itemId, amount);

        } finally {
            lock.unlock(); // Luôn giải phóng khóa trong khối finally[cite: 1]
        }
    }

    // Hàm phụ tìm Item nhanh
    private Item findItemById(String id) {
        return items.stream().filter(i -> i.getId().equals(id)).findFirst().orElse(null);
    }

    // --- TỰ ĐỘNG KẾT THÚC (MỤC 3.1.4) ---[cite: 1]
    public void checkAndEndAuctions() {
        for (Item item : items) {
            if (item.isAuctionActive() && item.getEndTime() != null && LocalDateTime.now().isAfter(item.getEndTime())) {
                item.setAuctionActive(false);
                notifyObservers(item.getId(), item.getCurrentPrice());
                System.out.println("Hệ thống: Tự động đóng phiên " + item.getId());
            }
        }
    }

    public User authenticate(String username, String password) {
        List<User> userList = new ArrayList<>();
        userList.add(new Admin(1, "Quản trị viên", "admin@gmail.com", "123", "ADMIN"));
        userList.add(new Bidder(2, "Người đấu giá", "user@gmail.com", "123", "USER"));

        for (User u : userList) {
            if (u.getEmail().equals(username) && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null;
    }
}