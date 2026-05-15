package com.auction.server.controller;

import com.auction.common.Request;
import com.auction.common.Response;
import com.auction.server.security.AuthenticatedUser;
import com.auction.server.service.WalletService;

public class WalletController {
    private final WalletService walletService;

    public WalletController() {
        this.walletService = new WalletService();
    }

    public Response getBalance(AuthenticatedUser user) {
        if (user == null) {
            return new Response(false, "Bạn cần đăng nhập để xem số dư.", null);
        }
        return walletService.getBalance(user.getUsername());
    }

    public Response deposit(AuthenticatedUser user, Request request) {
        if (user == null) {
            return new Response(false, "Bạn cần đăng nhập để gửi yêu cầu nạp tiền.", null);
        }
        return walletService.requestDeposit(user.getUsername(), request.getAmount());
    }

    public Response getPendingDepositRequests(AuthenticatedUser user) {
        return walletService.getPendingDepositRequests(user);
    }

    public Response approveDeposit(AuthenticatedUser user, Request request) {
        return walletService.approveDeposit(user, request.getDepositRequestId());
    }

    public Response rejectDeposit(AuthenticatedUser user, Request request) {
        return walletService.rejectDeposit(user, request.getDepositRequestId(), request.getRejectionReason());
    }
}
