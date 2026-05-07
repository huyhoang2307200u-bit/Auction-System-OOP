package com.auction.server.controller;

import com.auction.common.AuctionDTO;
import com.auction.common.BidResult;
import com.auction.common.Request;
import com.auction.common.Response;
import com.auction.server.service.AuctionService;

import java.util.List;

public class AuctionController {
    private final AuctionService auctionService;

    public AuctionController() {
        this.auctionService = new AuctionService();
    }

    public Response getAuctions() {
        List<AuctionDTO> auctions = auctionService.getAllAuctions();

        if (auctions == null || auctions.isEmpty()) {
            return new Response(false, "Không tìm thấy phiên đấu giá nào.", null);
        }

        return new Response(true, "Lấy danh sách phiên đấu giá thành công.", auctions);
    }

    public Response placeBid(Request request) {
        if (request.getAuctionId() == null) {
            return new Response(false, "Thiếu mã phiên đấu giá.", null);
        }

        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return new Response(false, "Thiếu tên người dùng.", null);
        }

        if (request.getAmount() == null) {
            return new Response(false, "Thiếu số tiền đặt giá.", null);
        }

        if (request.getAmount() <= 0) {
            return new Response(false, "Số tiền đặt giá phải lớn hơn 0.", null);
        }

        BidResult result = auctionService.placeBid(
                request.getAuctionId(),
                request.getUsername(),
                request.getAmount()
        );

        return new Response(result.isSuccess(), result.getMessage(), result.getCurrentPrice());
    }
}