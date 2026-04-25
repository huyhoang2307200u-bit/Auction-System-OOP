package com.auction.service;

import com.auction.model.BidTransaction;
import com.auction.model.Bidder;
import com.auction.model.Auction;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionService {
    // Danh sách lưu trữ các giao dịch đã thực hiện
    private List<BidTransaction> transactionHistory = new ArrayList<>();
    private int idCounter = 1;

    // Hàm thực hiện ghi lại một giao dịch mới
    public void recordTransaction(Bidder bidder, Auction auction, double amount) {
        BidTransaction newTransaction = new BidTransaction(
                idCounter++,
                bidder,
                auction,
                amount,
                LocalDateTime.now()
        );

        transactionHistory.add(newTransaction);
        System.out.println("Giao dịch #" + newTransaction.getId() + " đã được lưu.");
    }

    public List<BidTransaction> getTransactionHistory() {
        return transactionHistory;
    }
}