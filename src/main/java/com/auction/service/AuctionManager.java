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
import java.util.HashMap;
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
 * Singleton quản lý phiên đấu giá phía JavaFX demo/local.
 *
 * <p>Lớp này xử lý nghiệp vụ đấu giá, ví tạm giữ, auto-bidding, anti-sniping,
 * realtime observer, thông báo kết thúc phiên và lưu trạng thái cục bộ.</p>
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
    private boolean bootstrapping;

    private AuctionManager() {
        bootstrapping = true;
        seedDemoData();
        bootstrapping = false;
        scheduler.scheduleAtFixedRate(this::checkAndEndAuctionsSafely, 0, 1, TimeUnit.SECONDS);
    }

    public static synchronized AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        return instance;
    }

    public void addObserver(AuctionObserver observer) {
        if (observer != null && !observers.contains(observer)) {
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
        if (!bootstrapping) {
            notifyObservers(item.getId(), item.getCurrentPrice());
        }
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
                throw new InvalidBidException("Giá đặt " + MoneyUtil.formatVnd(bidAmount)
                        + " phải lớn hơn giá hiện tại " + MoneyUtil.formatVnd(currentPrice) + ".");
            }

            applyBidLocked(item, bidder.getId(), bidder.getName(), bidAmount, false);
            runAutoBiddingLocked(item);
        } finally {
            lock.unlock();
        }
    }

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
                throw new InvalidBidException("Số dư không đủ để bật auto-bid với maxBid "
                        + MoneyUtil.formatVnd(max) + ". Vui lòng nạp thêm tiền.");
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
            if (item.isAuctionActive()) {
                closeAuctionLocked(item, "Phiên đấu giá đã được Admin kết thúc.");
            }
            notifyObservers(item.getId(), item.getCurrentPrice());
        } finally {
            lock.unlock();
        }
    }

    public void checkAndEndAuctions() {
        LocalDateTime now = LocalDateTime.now();
        boolean changed = false;
        for (Item item : items) {
            ReentrantLock lock = itemLocks.computeIfAbsent(item.getId(), ignored -> new ReentrantLock(true));
            lock.lock();
            try {
                if (item.isAuctionActive() && item.getEndTime() != null && !now.isBefore(item.getEndTime())) {
                    closeAuctionLocked(item, "Phiên đấu giá đã tự động kết thúc vì hết thời gian.");
                    notifyObservers(item.getId(), item.getCurrentPrice());
                    changed = true;
                    System.out.println("Hệ thống: Tự động đóng phiên " + item.getId());
                }
            } finally {
                lock.unlock();
            }
        }
        if (changed) {
        }
    }

    public User authenticate(String username, String password) {
        return AuthService.login(username, password);
    }

    public void resetForTesting() {
        items.clear();
        auctions.clear();
        itemLocks.clear();
        bidHistoryByItem.clear();
        autoBidsByItem.clear();
        runtimeUsersById.clear();
        autoBidPriority.set(0);
        TransactionManager.getInstance().resetForTesting();
        NotificationManager.getInstance().resetForTesting();
    }

    public void seedDemoData() {
        if (!items.isEmpty()) {
            return;
        }
        User seller = AuthService.findUserByUsername("seller");
        String sellerId = seller == null ? "seller" : seller.getId();

        Item item1 = ItemFactory.createItem("ART", sellerId, "Đồng hồ cổ", 500.0);
        item1.setEndTime(LocalDateTime.now().plusMinutes(5));
        addNewItem(item1);

        Item item2 = ItemFactory.createItem("ELECTRONICS", sellerId, "Laptop Gaming", 1200.0);
        item2.setEndTime(LocalDateTime.now().plusMinutes(10));
        addNewItem(item2);

        Item item3 = ItemFactory.createItem("VEHICLE", sellerId, "Xe máy Vespa cổ", 1500.0);
        item3.setEndTime(LocalDateTime.now().plusMinutes(8));
        addNewItem(item3);
    }

    public List<Item> snapshotItems() {
        return new ArrayList<>(items);
    }

    public Map<String, List<BidTransaction>> snapshotBidHistory() {
        Map<String, List<BidTransaction>> snapshot = new HashMap<>();
        for (Map.Entry<String, List<BidTransaction>> entry : bidHistoryByItem.entrySet()) {
            snapshot.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return snapshot;
    }

    public Map<String, List<AutoBidConfig>> snapshotAutoBids() {
        Map<String, List<AutoBidConfig>> snapshot = new HashMap<>();
        for (Map.Entry<String, List<AutoBidConfig>> entry : autoBidsByItem.entrySet()) {
            snapshot.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return snapshot;
    }

    public void restoreState(List<Item> savedItems,
                             Map<String, List<BidTransaction>> savedBidHistory,
                             Map<String, List<AutoBidConfig>> savedAutoBids) {
        items.clear();
        auctions.clear();
        itemLocks.clear();
        bidHistoryByItem.clear();
        autoBidsByItem.clear();
        runtimeUsersById.clear();
        for (User user : AuthService.snapshotUsers()) {
            runtimeUsersById.put(user.getId(), user);
        }

        if (savedItems != null) {
            items.addAll(savedItems);
        }
        if (items.isEmpty()) {
            seedDemoData();
        }
        for (Item item : items) {
            bidHistoryByItem.putIfAbsent(item.getId(), new CopyOnWriteArrayList<>());
        }

        if (savedBidHistory != null) {
            for (Map.Entry<String, List<BidTransaction>> entry : savedBidHistory.entrySet()) {
                bidHistoryByItem.put(entry.getKey(), new CopyOnWriteArrayList<>(entry.getValue()));
            }
        }
        if (savedAutoBids != null) {
            long maxPriority = 0;
            for (Map.Entry<String, List<AutoBidConfig>> entry : savedAutoBids.entrySet()) {
                autoBidsByItem.put(entry.getKey(), new CopyOnWriteArrayList<>(entry.getValue()));
                for (AutoBidConfig config : entry.getValue()) {
                    maxPriority = Math.max(maxPriority, config.getPriorityOrder());
                }
            }
            autoBidPriority.set(maxPriority);
        }
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

    private void applyBidLocked(Item item, String bidderId, String bidderName, BigDecimal amount, boolean autoGenerated)
            throws InvalidBidException {
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
     * rồi giữ số tiền của người dẫn đầu mới.
     */
    private void reserveBidFundsLocked(Item item, String newBidderId, BigDecimal newAmount) throws InvalidBidException {
        User newBidder = runtimeUsersById.get(newBidderId);
        if (newBidder == null) {
            newBidder = AuthService.findUserById(newBidderId);
        }
        if (newBidder == null) {
            throw new InvalidBidException("Không tìm thấy ví tiền của người đặt giá.");
        }
        runtimeUsersById.putIfAbsent(newBidder.getId(), newBidder);

        List<BidTransaction> history = bidHistoryByItem.getOrDefault(item.getId(), List.of());
        String previousBidderId = history.isEmpty() ? null : history.get(history.size() - 1).getBidderId();
        BigDecimal previousHeldAmount = history.isEmpty()
                ? BigDecimal.ZERO
                : MoneyUtil.normalize(history.get(history.size() - 1).getBidAmountValue());

        if (previousBidderId != null && previousBidderId.equals(newBidderId)) {
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
            throw new InvalidBidException("Số dư không đủ để đặt giá " + MoneyUtil.formatVnd(newAmount)
                    + ". Số dư hiện tại: " + MoneyUtil.formatVnd(newBidder.getBalanceValue())
                    + ". Vui lòng nạp thêm tiền.");
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
            closeAuctionLocked(item, "Phiên đấu giá đã tự động kết thúc vì hết thời gian.");
            notifyObservers(item.getId(), item.getCurrentPrice());
            throw new AuctionClosedException("Phiên đấu giá đã hết thời gian.");
        }
    }

    private Item requireItem(String id) throws EntityNotFoundException {
        return items.stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sản phẩm/phiên đấu giá: " + id));
    }

    private void closeAuctionLocked(Item item, String reason) {
        if (item == null || !item.isAuctionActive()) {
            return;
        }
        item.setAuctionActive(false);
        sendAuctionFinishedNotifications(item, reason);
    }

    private void sendAuctionFinishedNotifications(Item item, String reason) {
        List<BidTransaction> history = bidHistoryByItem.getOrDefault(item.getId(), List.of());
        BidTransaction winningBid = history.isEmpty() ? null : history.get(history.size() - 1);
        String finalPrice = MoneyUtil.formatVnd(item.getCurrentPriceValue());

        User seller = AuthService.findUserById(item.getSellerId());
        if (seller == null) {
            seller = AuthService.findUserByUsername(item.getSellerId());
        }
        String sellerUsername = seller == null ? null : seller.getUsername();

        if (winningBid != null) {
            User winner = AuthService.findUserById(winningBid.getBidderId());
            if (winner == null) {
                winner = AuthService.findUserByUsername(winningBid.getBidderName());
            }
            if (winner != null) {
                NotificationManager.getInstance().addNotification(
                        winner.getUsername(),
                        "Bạn đã thắng phiên đấu giá",
                        "Chúc mừng! Bạn đã đấu giá thành công sản phẩm \"" + item.getName()
                                + "\" với giá " + finalPrice + ". " + reason
                );
            }

            if (sellerUsername != null) {
                NotificationManager.getInstance().addNotification(
                        sellerUsername,
                        "Phiên đấu giá đã kết thúc",
                        "Sản phẩm \"" + item.getName() + "\" đã kết thúc. Người thắng: "
                                + winningBid.getBidderName() + ". Giá cuối: " + finalPrice + ". " + reason
                );
            }
        } else if (sellerUsername != null) {
            NotificationManager.getInstance().addNotification(
                    sellerUsername,
                    "Phiên đấu giá đã kết thúc",
                    "Sản phẩm \"" + item.getName() + "\" đã kết thúc nhưng chưa có người đặt giá. " + reason
            );
        }
    }

    private void checkAndEndAuctionsSafely() {
        try {
            checkAndEndAuctions();
        } catch (Exception e) {
            System.err.println("Lỗi khi tự động đóng phiên: " + e.getMessage());
        }
    }
}
