package com.auction.service;

import com.auction.dao.ItemDAO;
import com.auction.dao.UserDAO;
import com.auction.model.Item;
import com.auction.model.User;
import com.auction.util.DatabaseHelper;
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
    private final Map<String, ReentrantLock> itemLocks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final ItemDAO itemDAO = new ItemDAO();
    private final UserDAO userDAO = new UserDAO();

    private AuctionManager() {
        // Load toàn bộ sản phẩm từ Database lên RAM khi khởi động
        refreshItemsFromDB();

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

    // ĐỔI TÊN HÀM NÀY: Để hết lỗi đỏ ở AuctionListController
    public void notifyPriceChanged(String itemId, double newPrice) {
        for (AuctionObserver observer : observers) {
            observer.onPriceChanged(itemId, newPrice);
        }
    }

    public void refreshItemsFromDB() {
        items.clear();
        items.addAll(itemDAO.getAllItems());
    }

    public List<Item> getAvailableItems() {
        return items;
    }

    // --- ĐĂNG NHẬP THẬT TỪ DATABASE ---
    public User authenticate(String username, String password) {
        // Gọi UserDAO để kiểm tra trong SQLite
        return userDAO.login(username, password);
    }

    // --- XỬ LÝ ĐẤU GIÁ ĐỒNG THỜI ---
    public void placeBid(String itemId, double amount, User bidder) throws Exception {
        ReentrantLock lock = itemLocks.computeIfAbsent(itemId, k -> new ReentrantLock());
        lock.lock();
        try {
            Item item = findItemById(itemId);
            if (item == null) throw new Exception("Không tìm thấy sản phẩm!");

            if (!item.isAuctionActive()) throw new Exception("Phiên đã kết thúc!");
            if (amount <= item.getCurrentPrice()) throw new Exception("Giá đặt phải lớn hơn giá hiện tại!");

            // 1. Cập nhật RAM
            item.setCurrentPrice(amount);

            // 2. Cập nhật Database (Dùng ItemDAO để lưu bền vững)
            itemDAO.updatePrice(itemId, amount);

            // 3. Thông báo Realtime
            notifyPriceChanged(itemId, amount);

        } finally {
            lock.unlock();
        }
    }

    private Item findItemById(String id) {
        return items.stream().filter(i -> i.getId().equals(id)).findFirst().orElse(null);
    }

    // --- TỰ ĐỘNG KẾT THÚC ---
    public void checkAndEndAuctions() {
        LocalDateTime now = LocalDateTime.now();
        for (Item item : items) {
            if (item.isAuctionActive() && item.getEndTime() != null && now.isAfter(item.getEndTime())) {
                item.setAuctionActive(false);
                // Lưu trạng thái đóng vào DB
                itemDAO.updateStatus(item.getId(), false);
                // Cập nhật giao diện
                notifyPriceChanged(item.getId(), item.getCurrentPrice());
                System.out.println("Hệ thống: Tự động đóng phiên " + item.getId());
            }
        }
    }
}