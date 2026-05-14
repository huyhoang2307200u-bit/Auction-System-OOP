package com.auction.model;

import com.auction.util.MoneyUtil;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import javafx.beans.property.*;

/**
 * Lớp Item: Đại diện cho sản phẩm đấu giá.
 * Kết hợp logic định danh, tính toán tài chính và thuộc tính hiển thị JavaFX.
 */
public abstract class Item extends Entity {
    private static final long serialVersionUID = 1L;

    // --- Thuộc tính cơ bản ---
    private String sellerId;
    private String title;
    private String description;
    private ItemCategory category;
    private String imageDataUrl;

    // --- Thuộc tính trạng thái đấu giá ---
    private BigDecimal startingPrice;
    private BigDecimal currentPrice;
    private String highestBidderName;
    private boolean auctionActive;
    private LocalDateTime endTime;

    /**
     * Constructor mặc định cho các mục đích khởi tạo nhanh.
     */
    protected Item() {
        super(); // Tự động sinh ID từ IdGenerator qua lớp Entity
        this.imageDataUrl = "";
        this.startingPrice = BigDecimal.ZERO;
        this.currentPrice = BigDecimal.ZERO;
        this.highestBidderName = "Chưa có";
        this.auctionActive = true;
    }

    /**
     * Constructor đầy đủ để tạo một sản phẩm mới.
     */
    protected Item(String sellerId, String title, String description,
                   BigDecimal startingPrice, ItemCategory category) {
        super();
        this.sellerId = sellerId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.imageDataUrl = "";

        // Chuẩn hóa tiền tệ ngay từ đầu bằng MoneyUtil
        this.startingPrice = MoneyUtil.normalize(startingPrice);
        this.currentPrice = this.startingPrice;

        this.highestBidderName = "Chưa có";
        this.auctionActive = true;
    }

    /**
     * Phương thức trừu tượng để in thông tin chi tiết (Đa hình).
     */
    public abstract String printInfo();

    // --- Getter và Setter thông thường ---

    public String getSellerId() { return sellerId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    // Phương thức bridge tương thích với code GUI cũ
    public String getName() { return title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ItemCategory getCategory() { return category; }
    public void setCategory(ItemCategory category) { this.category = category; }

    public String getImageDataUrl() { return imageDataUrl; }
    public void setImageDataUrl(String url) { this.imageDataUrl = (url == null) ? "" : url; }

    public BigDecimal getStartingPrice() { return startingPrice; }

    public BigDecimal getCurrentPriceValue() { return currentPrice; }

    // Chuyển đổi sang double khi cần hiển thị lên giao diện đơn giản
    public double getCurrentPrice() {
        return MoneyUtil.toDouble(currentPrice);
    }

    public void setCurrentPrice(BigDecimal price) {
        this.currentPrice = MoneyUtil.normalize(price);
    }

    public String getHighestBidderName() { return highestBidderName; }
    public void setHighestBidderName(String name) {
        this.highestBidderName = (name == null || name.isEmpty()) ? "Chưa có" : name;
    }

    public boolean isAuctionActive() { return auctionActive; }
    public void setAuctionActive(boolean active) { this.auctionActive = active; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getStatus() {
        return auctionActive ? "ĐANG MỞ" : "ĐÃ KẾT THÚC";
    }

    // --- CÁC PHƯƠNG THỨC JAVAFX PROPERTY (Dùng để Binding dữ liệu lên Bảng) ---

    public StringProperty idProperty() {
        return new SimpleStringProperty(getId());
    }

    public StringProperty nameProperty() {
        return new SimpleStringProperty(getTitle());
    }

    public DoubleProperty currentPriceProperty() {
        return new SimpleDoubleProperty(getCurrentPrice());
    }

    public StringProperty statusProperty() {
        return new SimpleStringProperty(getStatus());
    }

    public StringProperty highestBidderProperty() {
        return new SimpleStringProperty(getHighestBidderName());
    }
}