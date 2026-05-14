package com.auction.server.service;

import com.auction.common.Response;
import com.auction.dto.DepositRequestDto;
import com.auction.server.dao.UserDAO;
import com.auction.server.dao.WalletDAO;
import com.auction.server.security.AuthenticatedUser;
import java.util.List;

public class WalletService {
    private final WalletDAO walletDAO;
    private final UserDAO userDAO;

    public WalletService() {
        this.walletDAO = new WalletDAO();
        this.userDAO = new UserDAO();
    }

    public Response getBalance(String username) {
        if (username == null || username.isBlank()) {
            return new Response(false, "Bạn cần đăng nhập để xem số dư.", null);
        }
        if (!userDAO.userExists(username)) {
            return new Response(false, "Không tìm thấy người dùng.", null);
        }
        return new Response(true, "Lấy số dư thành công.", walletDAO.getBalance(username));
    }

    /**
     * User gửi yêu cầu nạp tiền. Số dư chưa tăng cho đến khi Admin duyệt.
     */
    public Response requestDeposit(String username, Double amount) {
        if (username == null || username.isBlank()) {
            return new Response(false, "Bạn cần đăng nhập để gửi yêu cầu nạp tiền.", null);
        }
        if (amount == null || amount <= 0) {
            return new Response(false, "Số tiền nạp phải lớn hơn 0.", null);
        }
        if (!userDAO.userExists(username)) {
            return new Response(false, "Không tìm thấy người dùng.", null);
        }
        if (!userDAO.isBidder(username)) {
            return new Response(false, "Chỉ Bidder được gửi yêu cầu nạp tiền vào ví.", null);
        }
        boolean success = walletDAO.createDepositRequest(username.trim(), amount);
        return new Response(success,
                success
                        ? "Đã gửi yêu cầu nạp tiền. Số dư chỉ tăng sau khi Admin duyệt."
                        : "Không thể tạo yêu cầu nạp tiền.",
                null);
    }

    /**
     * Backward-compatible method name: deposit now means request deposit, not credit immediately.
     */
    public Response deposit(String username, Double amount) {
        return requestDeposit(username, amount);
    }

    public Response getPendingDepositRequests(AuthenticatedUser admin) {
        if (!isAdmin(admin)) {
            return new Response(false, "Chỉ Admin mới được xem yêu cầu nạp tiền chờ duyệt.", null);
        }
        List<DepositRequestDto> requests = walletDAO.getPendingDepositRequests();
        return new Response(true, "Lấy danh sách yêu cầu nạp tiền chờ duyệt thành công.", requests);
    }

    public Response approveDeposit(AuthenticatedUser admin, Integer requestId) {
        if (!isAdmin(admin)) {
            return new Response(false, "Chỉ Admin mới được duyệt yêu cầu nạp tiền.", null);
        }
        if (requestId == null || requestId <= 0) {
            return new Response(false, "Mã yêu cầu nạp tiền không hợp lệ.", null);
        }
        boolean success = walletDAO.approveDepositRequest(requestId, admin.getUsername());
        return new Response(success,
                success ? "Đã duyệt yêu cầu nạp tiền và cộng số dư cho người dùng."
                        : "Duyệt thất bại. Yêu cầu có thể không tồn tại hoặc đã được xử lý.",
                null);
    }

    public Response rejectDeposit(AuthenticatedUser admin, Integer requestId, String reason) {
        if (!isAdmin(admin)) {
            return new Response(false, "Chỉ Admin mới được từ chối yêu cầu nạp tiền.", null);
        }
        if (requestId == null || requestId <= 0) {
            return new Response(false, "Mã yêu cầu nạp tiền không hợp lệ.", null);
        }
        boolean success = walletDAO.rejectDepositRequest(requestId, admin.getUsername(), reason);
        return new Response(success,
                success ? "Đã từ chối yêu cầu nạp tiền."
                        : "Từ chối thất bại. Yêu cầu có thể không tồn tại hoặc đã được xử lý.",
                null);
    }

    private boolean isAdmin(AuthenticatedUser user) {
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }
}
