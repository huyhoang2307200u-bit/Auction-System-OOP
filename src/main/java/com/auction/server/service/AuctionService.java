package com.auction.server.service;

import com.auction.common.AuctionDTO;
import com.auction.common.BidResult;
import com.auction.server.dao.AuctionDAO;

import java.util.List;

public class AuctionService {
    private final AuctionDAO auctionDAO;

    public AuctionService() {
        this.auctionDAO = new AuctionDAO();
    }

    public List<AuctionDTO> getAllAuctions() {
        return auctionDAO.getAllAuctions();
    }

    public BidResult placeBid(int auctionId, String username, double amount) {
        return auctionDAO.placeBid(auctionId, username, amount);
    }
}