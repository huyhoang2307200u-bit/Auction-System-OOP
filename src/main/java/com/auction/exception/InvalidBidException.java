package com.auction.exception;

public class InvalidBidException extends AuctionException {
    public InvalidBidException(String message) {
        super(message);
    }

    public InvalidBidException(String message, Throwable cause) {
        super(message, cause);
    }
}
