package com.auction.server.service;

import com.auction.common.AuctionDTO;
import com.auction.common.BidResult;
import com.auction.common.Response;
import com.auction.model.Role;
import com.auction.server.RealtimeClientRegistry;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.NotificationDAO;
import com.auction.util.MoneyUtil;

import java.util.List;

public class AuctionService {
    private static final String STATUS_PENDING_APPROVAL = "PENDING_APPROVAL";
    private static final String STATUS_OPEN = "OPEN";

    private final AuctionDAO auctionDAO;
    private final NotificationDAO notificationDAO;

    public AuctionService() {
        this.auctionDAO = new AuctionDAO();
        this.notificationDAO = new NotificationDAO();
    }

    public Response getAuctions() {
        finishExpiredAuctionsAndNotify();
        List<AuctionDTO> auctions = auctionDAO.getAllAuctions();
        return new Response(true, "Lấy danh sách phiên đấu giá thành công.", auctions);
    }

    public Response getAuctionDetail(Integer auctionId) {
        finishExpiredAuctionsAndNotify();
        if (auctionId == null) {
            return new Response(false, "Thiếu mã phiên đấu giá.", null);
        }

        AuctionDTO auction = auctionDAO.getAuctionById(auctionId);
        if (auction == null) {
            return new Response(false, "Không tìm thấy phiên đấu giá có mã: " + auctionId, null);
        }
        return new Response(true, "Lấy chi tiết phiên đấu giá thành công.", auction);
    }

    public Response createAuction(String sellerUsername, String role, String itemName, String description, String category, Double startPrice, Integer durationMinutes, String imageDataUrl) {
        if (!Role.SELLER.name().equalsIgnoreCase(role) && !Role.ADMIN.name().equalsIgnoreCase(role)) {
            return new Response(false, "Chỉ Seller hoặc Admin được tạo sản phẩm đấu giá.", null);
        }
        if (itemName == null || itemName.isBlank()) {
            return new Response(false, "Tên sản phẩm không được để trống.", null);
        }
        if (startPrice == null || startPrice <= 0) {
            return new Response(false, "Giá khởi điểm phải lớn hơn 0.", null);
        }

        String initialStatus = Role.ADMIN.name().equalsIgnoreCase(role) ? STATUS_OPEN : STATUS_PENDING_APPROVAL;
        boolean created = auctionDAO.createAuction(
                sellerUsername,
                itemName.trim(),
                description == null ? "" : description.trim(),
                category,
                startPrice,
                durationMinutes,
                initialStatus,
                imageDataUrl
        );

        if (created && Role.SELLER.name().equalsIgnoreCase(role)) {
            RealtimeClientRegistry.broadcast(new Response(true, "REALTIME_AUCTION_UPDATE", "Có sản phẩm mới chờ duyệt."));
        }

        String successMessage = Role.ADMIN.name().equalsIgnoreCase(role)
                ? "Admin đã tạo phiên đấu giá và mở ngay."
                : "Seller đã tạo sản phẩm. Sản phẩm đang chờ Admin kiểm duyệt.";
        return new Response(created, created ? successMessage : "Tạo phiên đấu giá thất bại.", null);
    }

    public Response finishAuction(String role, Integer auctionId) {
        if (!Role.ADMIN.name().equalsIgnoreCase(role)) {
            return new Response(false, "Chỉ Admin được kết thúc phiên đấu giá.", null);
        }
        if (auctionId == null) {
            return new Response(false, "Thiếu mã phiên đấu giá.", null);
        }

        AuctionDTO beforeFinish = auctionDAO.getAuctionById(auctionId);
        boolean finished = auctionDAO.finishAuction(auctionId);
        if (finished) {
            AuctionDTO finishedAuction = auctionDAO.getAuctionById(auctionId);
            if (finishedAuction == null) {
                finishedAuction = beforeFinish;
            }
            createFinishNotifications(finishedAuction);
            RealtimeClientRegistry.broadcast(new Response(true, "REALTIME_AUCTION_FINISHED", finishedAuction));
        }
        return new Response(finished,
                finished ? "Đã kết thúc phiên đấu giá và gửi thông báo cho người liên quan."
                        : "Không thể kết thúc. Phiên không tồn tại hoặc không còn mở.",
                null);
    }

    public int finishExpiredAuctionsAndNotify() {
        List<AuctionDTO> expiredAuctions = auctionDAO.finishExpiredAuctions();
        for (AuctionDTO auction : expiredAuctions) {
            createFinishNotifications(auction);
            RealtimeClientRegistry.broadcast(new Response(true, "REALTIME_AUCTION_FINISHED", auction));
        }
        return expiredAuctions.size();
    }

    public Response deleteAuction(String role, Integer auctionId) {
        if (!Role.ADMIN.name().equalsIgnoreCase(role)) {
            return new Response(false, "Chỉ Admin được xóa/hủy phiên đấu giá.", null);
        }
        if (auctionId == null) {
            return new Response(false, "Thiếu mã phiên đấu giá.", null);
        }
        boolean deleted = auctionDAO.deleteAuction(auctionId);
        return new Response(deleted, deleted ? "Đã xóa phiên đấu giá." : "Không thể xóa phiên đấu giá.", null);
    }

    public Response approveAuction(String role, String adminUsername, Integer auctionId) {
        if (!Role.ADMIN.name().equalsIgnoreCase(role)) {
            return new Response(false, "Chỉ Admin được duyệt sản phẩm.", null);
        }
        if (auctionId == null) {
            return new Response(false, "Thiếu mã phiên đấu giá cần duyệt.", null);
        }
        boolean approved = auctionDAO.approveAuction(auctionId, adminUsername);
        return new Response(approved,
                approved ? "Đã duyệt sản phẩm. Phiên đấu giá được mở." : "Không thể duyệt. Phiên không tồn tại hoặc không ở trạng thái chờ duyệt.",
                null);
    }

    public Response rejectAuction(String role, String adminUsername, Integer auctionId, String reason) {
        if (!Role.ADMIN.name().equalsIgnoreCase(role)) {
            return new Response(false, "Chỉ Admin được từ chối sản phẩm.", null);
        }
        if (auctionId == null) {
            return new Response(false, "Thiếu mã phiên đấu giá cần từ chối.", null);
        }
        String rejectionReason = reason == null || reason.isBlank() ? "Không đạt yêu cầu kiểm duyệt." : reason.trim();
        boolean rejected = auctionDAO.rejectAuction(auctionId, adminUsername, rejectionReason);
        return new Response(rejected,
                rejected ? "Đã từ chối sản phẩm." : "Không thể từ chối. Phiên không tồn tại hoặc không ở trạng thái chờ duyệt.",
                null);
    }

    public Response registerAutoBid(Integer auctionId, String username, String role, Double maxBid, Double increment) {
        if (!Role.BIDDER.name().equalsIgnoreCase(role)) {
            return new Response(false, "Chỉ Bidder được bật auto-bid.", null);
        }
        if (auctionId == null || username == null || username.trim().isEmpty()) {
            return new Response(false, "Thiếu thông tin người dùng hoặc phiên đấu giá.", null);
        }
        if (maxBid == null || maxBid <= 0 || increment == null || increment <= 0) {
            return new Response(false, "maxBid và increment phải lớn hơn 0.", null);
        }
        boolean registered = auctionDAO.registerAutoBid(auctionId, username, maxBid, increment);
        if (registered) {
            RealtimeClientRegistry.broadcast(new Response(true, "REALTIME_AUCTION_UPDATE", "Có cấu hình Auto-Bid mới."));
            return new Response(true, "Đã lưu cấu hình auto-bid và hệ thống sẽ tự động đặt giá.", null);
        } else {
            return new Response(false, "Không thể lưu cấu hình auto-bid. Vui lòng thử lại.", null);
        }
    }

    public Response placeBid(Integer auctionId, String username, String role, Double amount) {
        finishExpiredAuctionsAndNotify();
        if (!Role.BIDDER.name().equalsIgnoreCase(role)) {
            return new Response(false, "Chỉ Bidder được đặt giá.", null);
        }
        if (auctionId == null) {
            return new Response(false, "Thiếu mã phiên đấu giá.", null);
        }
        if (username == null || username.trim().isEmpty()) {
            return new Response(false, "Thiếu tên người dùng.", null);
        }
        if (amount == null || amount <= 0) {
            return new Response(false, "Số tiền đặt giá phải lớn hơn 0.", null);
        }

        BidResult bidResult = auctionDAO.placeBid(auctionId, username, amount);
        if (bidResult.isSuccess() && bidResult.isTimeExtended()) {
            RealtimeClientRegistry.broadcast(new Response(true, "REALTIME_AUCTION_UPDATE", "Gia hạn thời gian đấu giá do Anti-Sniping."));
        }
        return new Response(bidResult.isSuccess(), bidResult.getMessage(), bidResult.getCurrentPrice());
    }

    private void createFinishNotifications(AuctionDTO auction) {
        if (auction == null) {
            return;
        }

        String itemName = auction.getItemName() == null ? "sản phẩm" : auction.getItemName();
        String finalPrice = MoneyUtil.formatVnd(auction.getCurrentPrice());
        String winner = auction.getWinnerUsername();
        String seller = auction.getSellerUsername();

        if (winner != null && !winner.isBlank()) {
            notificationDAO.createNotification(
                    winner,
                    "Bạn đã thắng phiên đấu giá",
                    "Chúc mừng! Bạn đã đấu giá thành công sản phẩm \"" + itemName
                            + "\" với giá cuối cùng " + finalPrice + "."
            );
        }

        if (seller != null && !seller.isBlank()) {
            String sellerMessage = winner == null || winner.isBlank()
                    ? "Phiên đấu giá sản phẩm \"" + itemName + "\" đã kết thúc nhưng chưa có người thắng."
                    : "Phiên đấu giá sản phẩm \"" + itemName + "\" đã kết thúc. Người thắng: "
                    + winner + ". Giá cuối: " + finalPrice + ".";
            notificationDAO.createNotification(
                    seller,
                    "Phiên đấu giá đã kết thúc",
                    sellerMessage
            );
        }
    }
    // Thêm hàm này vào AuctionService
    public List<com.auction.dto.BidDto> getTransactionHistory() {
        return auctionDAO.getTransactionHistory();
    }
}
