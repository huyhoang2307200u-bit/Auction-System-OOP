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
            return new Response(false, "No auctions found.", null);
        }

        return new Response(true, "Auctions fetched successfully.", auctions);
    }

    public Response placeBid(Request request) {
        if (request.getAuctionId() == null) {
            return new Response(false, "auctionId is required.", null);
        }

        if (request.getUsername() == null || request.getUsername().isBlank()) {
            return new Response(false, "username is required.", null);
        }

        if (request.getAmount() == null) {
            return new Response(false, "amount is required.", null);
        }

        BidResult result = auctionService.placeBid(
                request.getAuctionId(),
                request.getUsername(),
                request.getAmount()
        );

        return new Response(result.isSuccess(), result.getMessage(), result.getCurrentPrice());
    }
}