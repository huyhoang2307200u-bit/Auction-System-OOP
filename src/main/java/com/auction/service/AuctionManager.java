package com.auction.service;

import com.auction.model.Admin;
import com.auction.model.Auction;
import com.auction.model.Bidder;
import com.auction.model.Item;
import com.auction.model.ItemFactory;
import com.auction.model.User;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class AuctionManager {
    private static AuctionManager instance;

    private final List<Item> items = new CopyOnWriteArrayList<>();
    private final List<Auction> auctions = new CopyOnWriteArrayList<>();
    private final List<AuctionObserver> observers = new CopyOnWriteArrayList<>();
    private final Map<String, ReentrantLock> itemLocks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private AuctionManager() {
        Item item1 = ItemFactory.createItem("ART", "SP01", "Đồng hồ cổ", 500.0);
        item1.setEndTime(LocalDateTime.now().plusMinutes(5));
        items.add(item1);

        Item item2 = ItemFactory.createItem("ELECTRONICS", "SP02", "Laptop Gaming", 1200.0);
        item2.setEndTime(LocalDateTime.now().plusMinutes(10));
        items.add(item2);

        scheduler.scheduleAtFixedRate(this::checkAndEndAuctions, 0, 1, TimeUnit.SECONDS);
    }

    public static synchronized AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        return instance;
    }

    public void addObserver(AuctionObserver observer) {
        if (observer != null) {
            observers.add(observer);
        }
    }

    public void removeObserver(AuctionObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers(String itemId, double newPrice) {
        for (AuctionObserver observer : observers) {
            observer.onPriceChanged(itemId, newPrice);
        }
    }

    public List<Item> getAvailableItems() {
        return new ArrayList<>(items);
    }

    public void addNewItem(Item item) {
        if (item == null) {
            return;
        }
        items.add(item);
        notifyObservers(item.getId(), item.getCurrentPrice());
    }

    public void createAuction(Auction auction) {
        if (auction == null || auctions.contains(auction)) {
            return;
        }
        auctions.add(auction);
        if (auction.getItem() != null && !items.contains(auction.getItem())) {
            items.add(auction.getItem());
        }
        System.out.println("Đã tạo phiên đấu giá cho sản phẩm: " + auction.getItem().getName());
    }

    public void placeBid(String itemId, double amount, User bidder) throws Exception {
        ReentrantLock lock = itemLocks.computeIfAbsent(itemId, ignored -> new ReentrantLock(true));

        lock.lock();
        try {
            Item item = findItemById(itemId);
            if (item == null) {
                throw new Exception("Không tìm thấy sản phẩm!");
            }
            if (!item.isAuctionActive()) {
                throw new Exception("Phiên đấu giá đã kết thúc!");
            }
            if (amount <= item.getCurrentPrice()) {
                throw new Exception("Giá đặt " + amount + " phải lớn hơn giá hiện tại " + item.getCurrentPrice());
            }

            item.setCurrentPrice(amount);
            item.setHighestBidderName(bidder == null ? "Không rõ" : bidder.getName());
            notifyObservers(itemId, amount);
        } finally {
            lock.unlock();
        }
    }

    // Hàm tương thích với code cũ dùng int auctionId
    public void placeBid(int auctionId, double amount, Bidder bidder) throws Exception {
        placeBid(String.valueOf(auctionId), amount, bidder);
    }

    private Item findItemById(String id) {
        return items.stream()
                .filter(item -> item.getId().equals(id) || item.getSellerId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void checkAndEndAuctions() {
        LocalDateTime now = LocalDateTime.now();
        for (Item item : items) {
            if (item.isAuctionActive() && item.getEndTime() != null && now.isAfter(item.getEndTime())) {
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

        for (User user : userList) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}
