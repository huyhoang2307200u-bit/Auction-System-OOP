package com.auction.service;

import com.auction.exception.AuctionClosedException;
import com.auction.exception.EntityNotFoundException;
import com.auction.exception.InvalidBidException;
import com.auction.model.Admin;
import com.auction.model.Auction;
import com.auction.model.AutoBidConfig;
import com.auction.model.BidTransaction;
import com.auction.model.Bidder;
import com.auction.model.Item;
import com.auction.model.ItemFactory;
import com.auction.model.User;
import com.auction.util.MoneyUtil;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Singleton quản lý phiên đấu giá phía client/demo.
 *
 * <p>Lớp này gom các nghiệp vụ bắt buộc quan trọng để có thể kiểm thử độc lập:
 * kiểm tra giá đấu hợp lệ, tránh race condition bằng lock theo sản phẩm, ghi lịch sử bid,
 * auto-bidding, anti-sniping và Observer để cập nhật realtime cho JavaFX.</p>
 */
public class AuctionManager {
    private static AuctionManager instance;

    private final List<Item> items = new CopyOnWriteArrayList<>();
    private final List<Auction> auctions = new CopyOnWriteArrayList<>();
    private final List<AuctionObserver> observers = new CopyOnWriteArrayList<>();
    private final Map<String, ReentrantLock> itemLocks = new ConcurrentHashMap<>();
    private final Map<String, List<BidTransaction>> bidHistoryByItem = new ConcurrentHashMap<>();
    private final Map<String, List<AutoBidConfig>> autoBidsByItem = new ConcurrentHashMap<>();
    private final Map<String, User> runtimeUsersById = new ConcurrentHashMap<>();
    private final AtomicLong autoBidPriority = new AtomicLong(0);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "auction-expiration-checker");
        thread.setDaemon(true);
        return thread;
    });

    private final int antiSnipingThresholdSeconds = 30;
    private final int antiSnipingExtensionSeconds = 60;

    private AuctionManager() {
        seedDemoData();
        scheduler.scheduleAtFixedRate(this::checkAndEndAuctionsSafely, 0, 1, TimeUnit.SECONDS);
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
        checkAndEndAuctions();
        return new ArrayList<>(items);
    }

    public List<BidTransaction> getBidHistory(String itemId) {
        return new ArrayList<>(bidHistoryByItem.getOrDefault(itemId, List.of()));
    }

    public List<AutoBidConfig> getAutoBidConfigs(String itemId) {
        return new ArrayList<>(autoBidsByItem.getOrDefault(itemId, List.of()));
    }

    public void addNewItem(Item item) {
        if (item == null) {
            return;
        }
        if (item.getEndTime() == null) {
            item.setEndTime(LocalDateTime.now().plusMinutes(5));
        }
        item.setAuctionActive(true);
        items.add(item);
        bidHistoryByItem.putIfAbsent(item.getId(), new CopyOnWriteArrayList<>());
        notifyObservers(item.getId(), item.getCurrentPrice());
    }

    public void createAuction(Auction auction) {
        if (auction == null || auctions.contains(auction)) {
            return;
        }
        auctions.add(auction);
        if (auction.getItem() != null && !items.contains(auction.getItem())) {
            addNewItem(auction.getItem());
        }
        System.out.println("Đã tạo phiên đấu giá cho sản phẩm: " + auction.getItem().getName());
    }

    public void placeBid(String itemId, double amount, User bidder) throws Exception {
        if (bidder == null) {
            throw new InvalidBidException("Người đặt giá không hợp lệ.");
        }

        runtimeUsersById.putIfAbsent(bidder.getId(), bidder);

        ReentrantLock lock = itemLocks.computeIfAbsent(itemId, ignored -> new ReentrantLock(true));
        lock.lock();
        try {
            Item item = requireItem(itemId);
            validateAuctionCanBid(item);

            BigDecimal bidAmount = MoneyUtil.fromDouble(amount);
            BigDecimal currentPrice = item.getCurrentPriceValue();
            if (bidAmount.compareTo(currentPrice) <= 0) {
                throw new InvalidBidException("Giá đặt " + bidAmount + " phải lớn hơn giá hiện tại " + currentPrice + ".");
            }

            applyBidLocked(item, bidder.getId(), bidder.getName(), bidAmount, false);
            runAutoBiddingLocked(item);
        } finally {
            lock.unlock();
        }
    }

    // Hàm tương thích với code cũ dùng int auctionId
    public void placeBid(int auctionId, double amount, Bidder bidder) throws Exception {
        placeBid(String.valueOf(auctionId), amount, bidder);
    }

    public void registerAutoBid(String itemId, User bidder, double maxBid, double increment) throws Exception {
        if (bidder == null) {
            throw new InvalidBidException("Người đăng ký auto-bid không hợp lệ.");
        }

        runtimeUsersById.putIfAbsent(bidder.getId(), bidder);

        BigDecimal max = MoneyUtil.fromDouble(maxBid);
        BigDecimal step = MoneyUtil.fromDouble(increment);
        if (max.compareTo(BigDecimal.ZERO) <= 0 || step.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidBidException("Max bid và bước giá phải lớn hơn 0.");
        }

        ReentrantLock lock = itemLocks.computeIfAbsent(itemId, ignored -> new ReentrantLock(true));
        lock.lock();
        try {
            Item item = requireItem(itemId);
            validateAuctionCanBid(item);
            if (max.compareTo(item.getCurrentPriceValue()) <= 0) {
                throw new InvalidBidException("Max bid phải lớn hơn giá hiện tại.");
            }
            if (!bidder.canAfford(max)) {
                throw new InvalidBidException("Số dư không đủ để bật auto-bid với maxBid " + max + ". Vui lòng nạp thêm tiền.");
            }

            List<AutoBidConfig> configs = autoBidsByItem.computeIfAbsent(itemId, ignored -> new CopyOnWriteArrayList<>());
            for (AutoBidConfig config : configs) {
                if (config.getBidderId().equals(bidder.getId()) && config.isActive()) {
                    config.deactivate();
                }
            }

            AutoBidConfig config = new AutoBidConfig(
                    itemId,
                    bidder.getId(),
                    max,
                    step,
                    LocalDateTime.now(),
                    autoBidPriority.incrementAndGet()
            );
            configs.add(config);
            runAutoBiddingLocked(item);
        } finally {
            lock.unlock();
        }
    }

    public void finishAuction(String itemId) throws Exception {
        ReentrantLock lock = itemLocks.computeIfAbsent(itemId, ignored -> new ReentrantLock(true));
        lock.lock();
        try {
            Item item = requireItem(itemId);
            item.setAuctionActive(false);
            notifyObservers(item.getId(), item.getCurrentPrice());
        } finally {
            lock.unlock();
        }
    }

    public void checkAndEndAuctions() {
        LocalDateTime now = LocalDateTime.now();
        for (Item item : items) {
            ReentrantLock lock = itemLocks.computeIfAbsent(item.getId(), ignored -> new ReentrantLock(true));
            lock.lock();
            try {
                if (item.isAuctionActive() && item.getEndTime() != null && !now.isBefore(item.getEndTime())) {
                    item.setAuctionActive(false);
                    notifyObservers(item.getId(), item.getCurrentPrice());
                    System.out.println("Hệ thống: Tự động đóng phiên " + item.getId());
                }
            } finally {
                lock.unlock();
            }
        }
    }

    public User authenticate(String username, String password) {
        List<User> userList = new ArrayList<>();
        userList.add(new Admin(1, "Quản trị viên", "admin@gmail.com", "123", "ADMIN"));
        userList.add(new Bidder(2, "Người đấu giá", "user@gmail.com", "123", "BIDDER"));

        for (User user : userList) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    public void resetForTesting() {
        items.clear();
        auctions.clear();
        itemLocks.clear();
        bidHistoryByItem.clear();
        autoBidsByItem.clear();
        runtimeUsersById.clear();
        autoBidPriority.set(0);
    }

    public void seedDemoData() {
        if (!items.isEmpty()) {
            return;
        }
        Item item1 = ItemFactory.createItem("ART", "seller", "Đồng hồ cổ", 500.0);
        item1.setEndTime(LocalDateTime.now().plusMinutes(5));
        addNewItem(item1);

        Item item2 = ItemFactory.createItem("ELECTRONICS", "seller", "Laptop Gaming", 1200.0);
        item2.setEndTime(LocalDateTime.now().plusMinutes(10));
        addNewItem(item2);

        Item item3 = ItemFactory.createItem("VEHICLE", "seller", "Xe máy Vespa cổ", 1500.0);
        item3.setEndTime(LocalDateTime.now().plusMinutes(8));
        addNewItem(item3);
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }

    private void runAutoBiddingLocked(Item item) {
        int safetyCounter = 0;
        while (safetyCounter++ < 100) {
            AutoBidConfig winner = findBestEligibleAutoBid(item);
            if (winner == null) {
                return;
            }

            BigDecimal current = item.getCurrentPriceValue();
            BigDecimal nextAmount = current.add(winner.getIncrement());
            if (nextAmount.compareTo(winner.getMaxBid()) > 0) {
                nextAmount = winner.getMaxBid();
            }
            if (nextAmount.compareTo(current) <= 0) {
                winner.deactivate();
                continue;
            }

            try {
                applyBidLocked(item, winner.getBidderId(), "AutoBid-" + winner.getBidderId(), nextAmount, true);
            } catch (InvalidBidException e) {
                // Auto-bid không còn đủ số dư hoặc không hợp lệ thì tắt cấu hình đó,
                // các auto-bid khác vẫn tiếp tục được xét.
                winner.deactivate();
                continue;
            }
            if (nextAmount.compareTo(winner.getMaxBid()) >= 0) {
                winner.deactivate();
            }
        }
    }

    private AutoBidConfig findBestEligibleAutoBid(Item item) {
        String currentWinnerId = getCurrentWinnerId(item);
        PriorityQueue<AutoBidConfig> queue = new PriorityQueue<>(
                Comparator.comparing(AutoBidConfig::getMaxBid).reversed()
                        .thenComparingLong(AutoBidConfig::getPriorityOrder)
        );
        for (AutoBidConfig config : autoBidsByItem.getOrDefault(item.getId(), List.of())) {
            if (config.isActive()
                    && !config.getBidderId().equals(currentWinnerId)
                    && config.getMaxBid().compareTo(item.getCurrentPriceValue()) > 0) {
                queue.offer(config);
            }
        }
        return queue.poll();
    }

    private String getCurrentWinnerId(Item item) {
        List<BidTransaction> history = bidHistoryByItem.getOrDefault(item.getId(), List.of());
        if (history.isEmpty()) {
            return "";
        }
        return history.get(history.size() - 1).getBidderId();
    }

    private void applyBidLocked(Item item, String bidderId, String bidderName, BigDecimal amount, boolean autoGenerated) throws InvalidBidException {
        BigDecimal normalizedAmount = MoneyUtil.normalize(amount);
        reserveBidFundsLocked(item, bidderId, normalizedAmount);
        item.setCurrentPrice(normalizedAmount);
        item.setHighestBidderName(bidderName);
        extendIfAntiSniping(item);

        BidTransaction transaction = new BidTransaction(
                item.getId(),
                bidderId,
                bidderName,
                item.getName(),
                normalizedAmount,
                LocalDateTime.now(),
                autoGenerated
        );
        bidHistoryByItem.computeIfAbsent(item.getId(), ignored -> new CopyOnWriteArrayList<>()).add(transaction);
        TransactionManager.getInstance().addTransaction(transaction);
        notifyObservers(item.getId(), normalizedAmount.doubleValue());
    }


    /**
     * Cơ chế ví tiền demo: mỗi bid đang dẫn đầu được giữ tiền ngay.
     * Khi có người khác vượt giá, hệ thống hoàn lại số tiền đang giữ cho người dẫn đầu cũ
     * rồi giữ số tiền của người dẫn đầu mới. Nhờ vậy bidder phải nạp đủ tiền trước khi đặt giá.
     */
    private void reserveBidFundsLocked(Item item, String newBidderId, BigDecimal newAmount) throws InvalidBidException {
        User newBidder = runtimeUsersById.get(newBidderId);
        if (newBidder == null) {
            newBidder = AuthService.findUserById(newBidderId);
        }
        if (newBidder == null) {
            throw new InvalidBidException("Không tìm thấy ví tiền của người đặt giá.");
        }

        List<BidTransaction> history = bidHistoryByItem.getOrDefault(item.getId(), List.of());
        String previousBidderId = history.isEmpty() ? null : history.get(history.size() - 1).getBidderId();
        BigDecimal previousHeldAmount = history.isEmpty()
                ? BigDecimal.ZERO
                : MoneyUtil.normalize(history.get(history.size() - 1).getBidAmountValue());

        if (previousBidderId != null && previousBidderId.equals(newBidderId)) {
            // Người đang dẫn đầu tự tăng giá: hoàn lại khoản giữ cũ rồi giữ khoản mới.
            newBidder.credit(previousHeldAmount);
            try {
                newBidder.debit(newAmount);
            } catch (InvalidBidException e) {
                newBidder.debit(previousHeldAmount);
                throw e;
            }
            return;
        }

        if (!newBidder.canAfford(newAmount)) {
            throw new InvalidBidException("Số dư không đủ để đặt giá " + newAmount
                    + ". Số dư hiện tại: " + newBidder.getBalanceValue() + ". Vui lòng nạp thêm tiền.");
        }

        if (previousBidderId != null && previousHeldAmount.compareTo(BigDecimal.ZERO) > 0) {
            User previousBidder = runtimeUsersById.get(previousBidderId);
            if (previousBidder == null) {
                previousBidder = AuthService.findUserById(previousBidderId);
            }
            if (previousBidder != null) {
                previousBidder.credit(previousHeldAmount);
            }
        }

        newBidder.debit(newAmount);
    }

    private void extendIfAntiSniping(Item item) {
        if (item.getEndTime() == null) {
            return;
        }
        long secondsLeft = Duration.between(LocalDateTime.now(), item.getEndTime()).getSeconds();
        if (secondsLeft >= 0 && secondsLeft <= antiSnipingThresholdSeconds) {
            item.setEndTime(item.getEndTime().plusSeconds(antiSnipingExtensionSeconds));
        }
    }

    private void validateAuctionCanBid(Item item) throws AuctionClosedException {
        if (item == null) {
            throw new AuctionClosedException("Sản phẩm không hợp lệ.");
        }
        if (!item.isAuctionActive()) {
            throw new AuctionClosedException("Phiên đấu giá đã kết thúc.");
        }
        if (item.getEndTime() != null && !LocalDateTime.now().isBefore(item.getEndTime())) {
            item.setAuctionActive(false);
            throw new AuctionClosedException("Phiên đấu giá đã hết thời gian.");
        }
    }

    private Item requireItem(String id) throws EntityNotFoundException {
        return items.stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sản phẩm/phiên đấu giá: " + id));
    }

    private void checkAndEndAuctionsSafely() {
        try {
            checkAndEndAuctions();
        } catch (Exception e) {
            System.err.println("Lỗi khi tự động đóng phiên: " + e.getMessage());
        }
    }
}
