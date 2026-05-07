package com.auction.server.service;

import com.auction.common.AuctionDTO;
import com.auction.common.BidResult;
import com.auction.common.Response;
import com.auction.server.dao.AuctionDAO;

import java.util.List;

public class AuctionService {
    private final AuctionDAO auctionDAO;

    public AuctionService() {
        this.auctionDAO = new AuctionDAO();
    }

    public Response getAuctions() {
        List<AuctionDTO> auctions = auctionDAO.getAllAuctions();

        if (auctions == null || auctions.isEmpty()) {
            return new Response(false, "Không tìm thấy phiên đấu giá nào.", null);
        }

        return new Response(true, "Lấy danh sách phiên đấu giá thành công.", auctions);
    }

    public Response getAuctionDetail(Integer auctionId) {
        if (auctionId == null) {
            return new Response(false, "Thiếu mã phiên đấu giá.", null);
        }

        AuctionDTO auction = auctionDAO.getAuctionById(auctionId);

        if (auction == null) {
            return new Response(false, "Không tìm thấy phiên đấu giá có mã: " + auctionId, null);
        }

        return new Response(true, "Lấy chi tiết phiên đấu giá thành công.", auction);
    }

    public Response placeBid(Integer auctionId, String username, Double amount) {
        if (auctionId == null) {
            return new Response(false, "Thiếu mã phiên đấu giá.", null);
        }

        if (username == null || username.trim().isEmpty()) {
            return new Response(false, "Thiếu tên người dùng.", null);
        }

        if (amount == null) {
            return new Response(false, "Thiếu số tiền đặt giá.", null);
        }

        BidResult bidResult = auctionDAO.placeBid(auctionId, username, amount);

        return new Response(
                bidResult.isSuccess(),
                bidResult.getMessage(),
                bidResult.getCurrentPrice()
        );
    }
}