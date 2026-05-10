package com.auction.service;

import com.auction.dao.ItemDAO;
import com.auction.dao.UserDAO;
import com.auction.model.Item;
import com.auction.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.time.LocalDateTime;

public class AuctionManager {
    private static AuctionManager instance;
    private final List<Item> items = new ArrayList<>();
    private final List<AuctionObserver> observers = new CopyOnWriteArrayList<>(); // Dùng bản này để an toàn đa luồng
    private final Map<String, ReentrantLock> itemLocks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final ItemDAO itemDAO = new ItemDAO();
    private final UserDAO userDAO = new UserDAO();

    private AuctionManager() {
        // Load toàn bộ sản phẩm từ Database lên RAM khi khởi động
        refreshItemsFromDB();

        // Chạy nhiệm vụ quét thời gian mỗi giây một lần để kết thúc phiên tự động
        scheduler.scheduleAtFixedRate(this::checkAndEndAuctions, 0, 1, TimeUnit.SECONDS);
    }

    public static synchronized AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        return instance;
    }

    public void addObserver(AuctionObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Phát tín hiệu Real-time cho các cửa sổ đang mở
     */
    public void notifyPriceChanged(String itemId, double newPrice) {
        for (AuctionObserver observer : observers) {
            observer.onPriceChanged(itemId, newPrice);
        }
    }

    /**
     * Đồng bộ dữ liệu từ Database vào RAM
     */
    public void refreshItemsFromDB() {
        items.clear();
        items.addAll(itemDAO.getAllItems());
    }

    public List<Item> getAvailableItems() {
        return items;
    }

    // --- XÁC THỰC NGƯỜI DÙNG ---
    public User authenticate(String username, String password) {
        return userDAO.login(username, password);
    }

    /**
     * Thông báo khi có sản phẩm mới được thêm vào
     */
    public void notifyItemAdded() {
        refreshItemsFromDB();
        notifyPriceChanged("NEW_ITEM", 0);
    }

    // --- XỬ LÝ ĐẤU GIÁ ĐỒNG THỜI (REAL-TIME CORE) ---
    public void placeBid(String itemId, double amount, User bidder) throws Exception {
        ReentrantLock lock = itemLocks.computeIfAbsent(itemId, k -> new ReentrantLock());
        lock.lock();
        try {
            Item item = findItemById(itemId);
            if (item == null) throw new Exception("Không tìm thấy sản phẩm!");

            if (!item.isAuctionActive()) throw new Exception("Phiên đã kết thúc!");
            if (amount <= item.getCurrentPrice()) throw new Exception("Giá đặt phải lớn hơn giá hiện tại!");

            // --- QUY TRÌNH REAL-TIME CHUẨN ---

            // 1. Lưu vào Database trước (Đảm bảo dữ liệu bền vững)
            if (itemDAO.updatePrice(itemId, amount)) {

                // 2. Cập nhật vào đối tượng trong RAM
                item.setCurrentPrice(amount);

                // 3. Làm tươi danh sách trong RAM để các cửa sổ khác lấy được bản mới nhất
                refreshItemsFromDB();

                // 4. Phát tín hiệu Real-time cho các máy khách
                notifyPriceChanged(itemId, amount);

                System.out.println("Hệ thống: Đã cập nhật giá mới " + amount + " cho SP " + itemId);
            } else {
                throw new Exception("Lỗi: Không thể cập nhật giá vào Database!");
            }

        } finally {
            lock.unlock();
        }
    }

    private Item findItemById(String id) {
        return items.stream().filter(i -> i.getId().equals(id)).findFirst().orElse(null);
    }

    // --- TỰ ĐỘNG KẾT THÚC PHIÊN ---
    public void checkAndEndAuctions() {
        LocalDateTime now = LocalDateTime.now();
        boolean hasChange = false;

        for (Item item : items) {
            if (item.isAuctionActive() && item.getEndTime() != null && now.isAfter(item.getEndTime())) {
                item.setAuctionActive(false);
                // Lưu trạng thái đóng vào DB
                itemDAO.updateStatus(item.getId(), false);
                hasChange = true;
                System.out.println("Hệ thống: Tự động đóng phiên " + item.getId());
            }
        }

        if (hasChange) {
            refreshItemsFromDB();
            notifyPriceChanged("AUCTION_CLOSED", 0);
        }
    }
}