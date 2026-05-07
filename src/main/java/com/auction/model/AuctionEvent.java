package com.auction.model;
import com.auction.dto.AuctionDetailDto;
import java.time.LocalDateTime;

public class AuctionEvent {
    private AuctionEventType type;
    private String auctionId;
    private String message;
    private BidTransaction bid;
    private AuctionDetailDto detail;
    private LocalDateTime timestamp;

    public AuctionEvent() {
    }

    public AuctionEvent(AuctionEventType type, String auctionId, String message,
                        BidTransaction bid, AuctionDetailDto detail, LocalDateTime timestamp) {
        this.type = type;
        this.auctionId = auctionId;
        this.message = message;
        this.bid = bid;
        this.detail = detail;
        this.timestamp = timestamp;
    }

    public AuctionEventType getType() {
        return type;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getMessage() {
        return message;
    }

    public BidTransaction getBid() {
        return bid;
    }

    public AuctionDetailDto getDetail() {
        return detail;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}

