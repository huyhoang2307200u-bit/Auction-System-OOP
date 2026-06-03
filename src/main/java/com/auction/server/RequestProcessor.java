package com.auction.server;

import com.auction.common.Request;
import com.auction.common.RequestType;
import com.auction.common.Response;
import com.auction.dto.LoginResultDto;
import com.auction.server.controller.AuctionController;
import com.auction.server.controller.AuthController;
import com.auction.server.controller.WalletController;
import com.auction.server.controller.NotificationController;
import com.auction.server.dao.UserDAO;
import com.auction.server.security.AuthenticatedUser;

public class RequestProcessor {
    private final AuthController authController;
    private final AuctionController auctionController;
    private final WalletController walletController;
    private final NotificationController notificationController;
    private final UserDAO userDAO;
    private AuthenticatedUser currentUser;

    public RequestProcessor() {
        this.authController = new AuthController();
        this.auctionController = new AuctionController();
        this.walletController = new WalletController();
        this.notificationController = new NotificationController();
        this.userDAO = new UserDAO();
    }

    public Response process(Request request) {
        if (request == null) {
            return new Response(false, "Yêu cầu không được null.", null);
        }

        if (request.getType() == null) {
            return new Response(false, "Loại yêu cầu không được để trống.", null);
        }

        RequestType type = request.getType();

        switch (type) {
            case PING:
                return new Response(true, "Server đã phản hồi PING.", "Server đang hoạt động.");

            case MESSAGE:
                return new Response(true, "Server đã nhận tin nhắn.", request.getMessage());

            case LOGIN:
                return loginAndRememberSession(request);

            case REGISTER:
                return authController.register(request);

            case GET_AUCTIONS:
                return auctionController.getAuctions();

            case GET_AUCTION_DETAIL:
                return auctionController.getAuctionDetail(request);

            case CREATE_AUCTION:
                return auctionController.createAuction(resolveCurrentUser(request), request);

            case DELETE_AUCTION:
                return auctionController.deleteAuction(resolveCurrentUser(request), request);

            case APPROVE_AUCTION:
                return auctionController.approveAuction(resolveCurrentUser(request), request);

            case REJECT_AUCTION:
                return auctionController.rejectAuction(resolveCurrentUser(request), request);

            case FINISH_AUCTION:
                return auctionController.finishAuction(resolveCurrentUser(request), request);

            case REGISTER_AUTO_BID:
                return auctionController.registerAutoBid(resolveCurrentUser(request), request);

            case PLACE_BID:
                return auctionController.placeBid(resolveCurrentUser(request), request);

            case DEPOSIT_MONEY: {
                Response response = walletController.deposit(resolveCurrentUser(request), request);
                if (response.isSuccess()) {
                    RealtimeClientRegistry.broadcast(new Response(true, "REALTIME_DEPOSIT_REQUEST_CREATED", request.getUsername()));
                }
                return response;
            }

            case GET_BALANCE:
                return walletController.getBalance(resolveCurrentUser(request));

            case GET_PENDING_DEPOSIT_REQUESTS:
                return walletController.getPendingDepositRequests(resolveCurrentUser(request));

            case APPROVE_DEPOSIT: {
                Response response = walletController.approveDeposit(resolveCurrentUser(request), request);
                if (response.isSuccess()) {
                    RealtimeClientRegistry.broadcast(new Response(true, "REALTIME_WALLET_UPDATE", request.getDepositRequestId()));
                }
                return response;
            }

            case REJECT_DEPOSIT: {
                Response response = walletController.rejectDeposit(resolveCurrentUser(request), request);
                if (response.isSuccess()) {
                    RealtimeClientRegistry.broadcast(new Response(true, "REALTIME_WALLET_UPDATE", request.getDepositRequestId()));
                }
                return response;
            }


            case GET_UNREAD_NOTIFICATIONS:
                return notificationController.getUnreadNotifications(resolveCurrentUser(request));

            case MARK_NOTIFICATION_READ:
                return notificationController.markNotificationRead(resolveCurrentUser(request), request);

            case EXIT:
                currentUser = null;
                return new Response(true, "Tạm biệt! Đang ngắt kết nối với server.", null);

            case GET_TRANSACTION_HISTORY:
                return auctionController.getTransactionHistory();
            default:
                return new Response(false, "Loại yêu cầu chưa được hỗ trợ: " + type, null);
        }
    }

    private Response loginAndRememberSession(Request request) {
        Response response = authController.login(request);
        if (response.isSuccess() && response.getData() instanceof LoginResultDto result) {
            currentUser = new AuthenticatedUser(
                    Integer.parseInt(result.getUserId()),
                    result.getUsername(),
                    result.getRole().name()
            );
        }
        return response;
    }

    private AuthenticatedUser resolveCurrentUser(Request request) {
        // Bỏ token theo yêu cầu: server dùng session của socket sau LOGIN.
        // Nếu gọi trực tiếp không qua socket/session, server vẫn có thể resolve từ username,
        // nhưng role luôn lấy từ database, không tin role client gửi lên.
        if (currentUser != null) {
            request.setUsername(currentUser.getUsername());
            request.setRole(currentUser.getRole());
            return currentUser;
        }

        String username = request.getUsername();
        if (username == null || username.isBlank()) {
            return null;
        }

        Integer userId = userDAO.getUserIdByUsername(username);
        String role = userDAO.getUserRole(username);
        if (userId == null || role == null) {
            return null;
        }

        request.setRole(role);
        return new AuthenticatedUser(userId, username.trim(), role);
    }
}
