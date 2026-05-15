package com.auction.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.auction.exception.AuthorizationException;
import com.auction.model.Admin;
import com.auction.model.Bidder;
import com.auction.model.DepositRequest;
import com.auction.model.DepositRequestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WalletManagerTest {
    private WalletManager walletManager;
    private Bidder bidder;
    private Admin admin;

    @BeforeEach
    void setUp() {
        walletManager = WalletManager.getInstance();
        walletManager.resetForTesting();
        bidder = new Bidder("wallet_user_" + System.nanoTime(), "Wallet User", "", "123");
        admin = new Admin("admin_wallet", "Admin Wallet", "", "123");
        AuthService.register(bidder);
    }

    @Test
    void depositRequestDoesNotIncreaseBalanceBeforeAdminApproval() {
        DepositRequest request = walletManager.requestDeposit(bidder, 300.0);

        assertEquals(DepositRequestStatus.PENDING, request.getStatus());
        assertEquals(0.0, bidder.getBalance(), 0.001);
        assertEquals(1, walletManager.getPendingRequests().size());
    }

    @Test
    void adminApprovalCreditsUserBalance() {
        DepositRequest request = walletManager.requestDeposit(bidder, 300.0);

        walletManager.approveDeposit(request.getId(), admin);

        assertEquals(DepositRequestStatus.APPROVED, request.getStatus());
        assertEquals(300.0, bidder.getBalance(), 0.001);
        assertTrue(walletManager.getPendingRequests().isEmpty());
    }

    @Test
    void nonAdminCannotApproveDepositRequest() {
        DepositRequest request = walletManager.requestDeposit(bidder, 300.0);

        assertThrows(AuthorizationException.class, () -> walletManager.approveDeposit(request.getId(), bidder));
    }

    @Test
    void rejectedRequestDoesNotCreditBalance() {
        DepositRequest request = walletManager.requestDeposit(bidder, 300.0);

        walletManager.rejectDeposit(request.getId(), admin, "Biên lai không hợp lệ");

        assertEquals(DepositRequestStatus.REJECTED, request.getStatus());
        assertEquals(0.0, bidder.getBalance(), 0.001);
    }
}
