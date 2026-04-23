package com.auction.server.controller;

import com.auction.common.AuctionDTO;
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
}