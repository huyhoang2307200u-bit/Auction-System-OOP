package com.auction.service;

<<<<<<< HEAD
import com.auction.model.Auction;
import com.auction.model.Bidder;
import java.util.ArrayList;
import java.util.List;

public class AuctionManager {
    private static AuctionManager instance;
    private List<Auction> auctions = new ArrayList<>(); // Quản lý danh sách các phiên

    private AuctionManager() {}
=======
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
>>>>>>> 3fe42484d323085aa7f2422769a1b94c0309a976

    public static synchronized AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        return instance;
    }

<<<<<<< HEAD
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
=======
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
>>>>>>> 3fe42484d323085aa7f2422769a1b94c0309a976
}