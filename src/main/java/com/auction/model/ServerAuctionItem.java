package com.auction.model;

import com.auction.common.AuctionDTO;
import com.auction.util.MoneyUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Adapter để JavaFX TableView cũ vẫn hiển thị được dữ liệu lấy từ server/MySQL.
 */
public class ServerAuctionItem extends Electronics {
    private static final long serialVersionUID = 1L;

    private final int auctionId;
    private final String serverStatus;
    private final String sellerUsername;
    private final String winnerUsername;
    private final LocalDateTime serverEndTime;

    public ServerAuctionItem(AuctionDTO dto) {
        super(
                dto.getSellerUsername() == null ? "" : dto.getSellerUsername(),
                dto.getItemName(),
                dto.getDescription() == null ? "" : dto.getDescription(),
                MoneyUtil.fromDouble(dto.getCurrentPrice()),
                "Server",
                0
        );
        this.auctionId = dto.getId();
        this.serverStatus = dto.getStatus() == null ? "UNKNOWN" : dto.getStatus();
        this.sellerUsername = dto.getSellerUsername();
        this.winnerUsername = dto.getWinnerUsername();
        this.serverEndTime = parseEndTime(dto.getEndTime());
        setCurrentPrice(BigDecimal.valueOf(dto.getCurrentPrice()));
        setHighestBidderName(dto.getWinnerUsername() == null || dto.getWinnerUsername().isBlank()
                ? "Chưa có"
                : dto.getWinnerUsername());
        setImageDataUrl(dto.getImageUrl() == null ? "" : dto.getImageUrl());
    }

    @Override
    public String getId() {
        return String.valueOf(auctionId);
    }

    public int getAuctionId() {
        return auctionId;
    }

    public String getServerStatus() {
        return serverStatus;
    }

    public String getSellerUsername() {
        return sellerUsername;
    }

    public String getWinnerUsername() {
        return winnerUsername;
    }

    @Override
    public boolean isAuctionActive() {
        return "OPEN".equalsIgnoreCase(serverStatus) || "RUNNING".equalsIgnoreCase(serverStatus);
    }

    @Override
    public String getStatus() {
        return switch (serverStatus.toUpperCase()) {
            case "PENDING_APPROVAL" -> "CHỜ DUYỆT";
            case "OPEN" -> "ĐANG MỞ";
            case "RUNNING" -> "ĐANG ĐẤU GIÁ";
            case "FINISHED" -> "ĐÃ KẾT THÚC";
            case "REJECTED" -> "BỊ TỪ CHỐI";
            case "PAID" -> "ĐÃ THANH TOÁN";
            case "CANCELED" -> "ĐÃ HỦY";
            default -> serverStatus;
        };
    }

    @Override
    public LocalDateTime getEndTime() {
        return serverEndTime;
    }

    private LocalDateTime parseEndTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value);
        } catch (Exception e) {
            return null;
        }
    }
}
