package com.auction.service;

import com.auction.model.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionService {
    private final List<BidTransaction> transactionHistory = new ArrayList<>();
    private int idCounter = 1;

    public void recordTransaction(Bidder bidder, Item item, double amount) {
        BidTransaction newTransaction = new BidTransaction(
                idCounter++,
                bidder,
                item,
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