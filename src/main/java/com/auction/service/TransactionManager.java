package com.auction.service;

import com.auction.model.BidTransaction;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TransactionManager {
    private static TransactionManager instance;

    private final ObservableList<BidTransaction> transactionList = FXCollections.observableArrayList();

    private TransactionManager() {
    }

    public static synchronized TransactionManager getInstance() {
        if (instance == null) {
            instance = new TransactionManager();
        }
        return instance;
    }

    public void addTransaction(BidTransaction trans) {
        if (trans == null) {
            return;
        }
        transactionList.add(trans);
    }

    public ObservableList<BidTransaction> getTransactionList() {
        return transactionList;
    }

    public List<BidTransaction> snapshotTransactions() {
        return new ArrayList<>(transactionList);
    }

    public void restoreTransactions(List<BidTransaction> savedTransactions) {
        transactionList.clear();
        if (savedTransactions != null) {
            transactionList.addAll(savedTransactions);
        }
    }

    public void resetForTesting() {
        transactionList.clear();
    }
}
