package com.auction.exception;

public class AuctionClosedException extends AuctionException {
    public AuctionClosedException(String message) {
        super(message);
    }

    public AuctionClosedException(String message, Throwable cause) {
        super(message, cause);
    }
}
