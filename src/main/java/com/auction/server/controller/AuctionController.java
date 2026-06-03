package com.auction.server.controller;

import com.auction.common.Request;
import com.auction.common.Response;
import com.auction.server.security.AuthenticatedUser;
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

    public Response createAuction(AuthenticatedUser user, Request request) {
        if (user == null) {
            return new Response(false, "Bạn cần đăng nhập để tạo phiên đấu giá.", null);
        }
        return auctionService.createAuction(
                user.getUsername(),
                user.getRole(),
                request.getItemName(),
                request.getDescription(),
                request.getCategory(),
                request.getAmount(),
                request.getDurationMinutes(),
                request.getImageDataUrl()
        );
    }
    public Response finishAuction(AuthenticatedUser user, Request request) {
        if (user == null) {
            return new Response(false, "Bạn cần đăng nhập để kết thúc phiên.", null);
        }
        return auctionService.finishAuction(user.getRole(), request.getAuctionId());
    }


    public Response deleteAuction(AuthenticatedUser user, Request request) {
        if (user == null) {
            return new Response(false, "Bạn cần đăng nhập để xóa phiên đấu giá.", null);
        }
        return auctionService.deleteAuction(user.getRole(), request.getAuctionId());
    }


    public Response approveAuction(AuthenticatedUser user, Request request) {
        if (user == null) {
            return new Response(false, "Bạn cần đăng nhập để duyệt sản phẩm.", null);
        }
        return auctionService.approveAuction(user.getRole(), user.getUsername(), request.getAuctionId());
    }

    public Response rejectAuction(AuthenticatedUser user, Request request) {
        if (user == null) {
            return new Response(false, "Bạn cần đăng nhập để từ chối sản phẩm.", null);
        }
        return auctionService.rejectAuction(
                user.getRole(),
                user.getUsername(),
                request.getAuctionId(),
                request.getRejectionReason() == null ? request.getMessage() : request.getRejectionReason()
        );
    }

    public Response registerAutoBid(AuthenticatedUser user, Request request) {
        if (user == null) {
            return new Response(false, "Bạn cần đăng nhập để bật auto-bid.", null);
        }
        if (request.getAuctionId() == null
                || request.getMaxBid() == null || request.getIncrement() == null) {
            return new Response(false, "Thiếu thông tin auto-bid.", null);
        }
        return auctionService.registerAutoBid(
                request.getAuctionId(),
                user.getUsername(),
                user.getRole(),
                request.getMaxBid(),
                request.getIncrement()
        );
    }

    public Response placeBid(AuthenticatedUser user, Request request) {
        if (user == null) {
            return new Response(false, "Bạn cần đăng nhập để đặt giá.", null);
        }
        if (request.getAuctionId() == null) {
            return new Response(false, "Thiếu mã phiên đấu giá.", null);
        }
        if (request.getAmount() == null || request.getAmount() <= 0) {
            return new Response(false, "Số tiền đặt giá phải lớn hơn 0.", null);
        }
        return auctionService.placeBid(request.getAuctionId(), user.getUsername(), user.getRole(), request.getAmount());
    }
    // Thêm hàm này vào AuctionController
    public Response getTransactionHistory() {
        return new Response(true, "Thành công", auctionService.getTransactionHistory());
    }
}
