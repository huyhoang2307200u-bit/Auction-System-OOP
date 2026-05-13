package com.auction.server.controller;

import com.auction.common.Request;
import com.auction.common.Response;
import com.auction.server.service.AuctionService;

public class AuctionController {
    private final AuctionService auctionService;

    public AuctionController() {
        this.auctionService = new AuctionService();
    }

    public Response getAuctions() {
        return auctionService.getAuctions();
    }

    public Response getAuctionDetail(Request request) {
        if (request.getAuctionId() == null) {
            return new Response(false, "Thiếu mã phiên đấu giá.", null);
        }

        return auctionService.getAuctionDetail(request.getAuctionId());
    }

    public Response placeBid(Request request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return new Response(false, "Thiếu tên người dùng.", null);
        }

        if (request.getAuctionId() == null) {
            return new Response(false, "Thiếu mã phiên đấu giá.", null);
        }

        if (request.getAmount() == null) {
            return new Response(false, "Thiếu số tiền đặt giá.", null);
        }

        if (request.getAmount() <= 0) {
            return new Response(false, "Số tiền đặt giá phải lớn hơn 0.", null);
        }

        return auctionService.placeBid(
                request.getAuctionId(),
                request.getUsername(),
                request.getAmount()
        );
    }
}