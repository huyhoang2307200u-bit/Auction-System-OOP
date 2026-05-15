package com.auction.server.service;

import com.auction.common.AuctionDTO;
import com.auction.common.BidResult;
import com.auction.common.Response;
import com.auction.model.Role;
import com.auction.server.dao.AuctionDAO;
import java.util.List;

public class AuctionService {
    private static final String STATUS_PENDING_APPROVAL = "PENDING_APPROVAL";
    private static final String STATUS_OPEN = "OPEN";

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

    public Response createAuction(String sellerUsername, String role, String itemName, String description, Double startPrice) {
        if (!Role.SELLER.name().equalsIgnoreCase(role) && !Role.ADMIN.name().equalsIgnoreCase(role)) {
            return new Response(false, "Chỉ Seller hoặc Admin được tạo sản phẩm đấu giá.", null);
        }
        if (itemName == null || itemName.isBlank()) {
            return new Response(false, "Tên sản phẩm không được để trống.", null);
        }
        if (startPrice == null || startPrice <= 0) {
            return new Response(false, "Giá khởi điểm phải lớn hơn 0.", null);
        }

        // Seller tạo sản phẩm thì phải chờ Admin kiểm duyệt.
        // Admin tạo trực tiếp có thể mở phiên ngay.
        String initialStatus = Role.ADMIN.name().equalsIgnoreCase(role) ? STATUS_OPEN : STATUS_PENDING_APPROVAL;
        boolean created = auctionDAO.createAuction(
                sellerUsername,
                itemName.trim(),
                description == null ? "" : description.trim(),
                startPrice,
                initialStatus
        );

        String successMessage = Role.ADMIN.name().equalsIgnoreCase(role)
                ? "Admin đã tạo phiên đấu giá và mở ngay."
                : "Seller đã tạo sản phẩm. Sản phẩm đang chờ Admin kiểm duyệt.";
        return new Response(created, created ? successMessage : "Tạo phiên đấu giá thất bại.", null);
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
        return new Response(true,
                "Server đã nhận cấu hình auto-bid. Bản GUI demo xử lý auto-bid realtime trong AuctionManager.",
                null);
    }

    public Response placeBid(Integer auctionId, String username, String role, Double amount) {
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
        return new Response(bidResult.isSuccess(), bidResult.getMessage(), bidResult.getCurrentPrice());
    }
}
