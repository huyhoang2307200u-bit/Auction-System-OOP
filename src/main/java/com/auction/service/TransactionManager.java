package com.auction.service;

import com.auction.model.BidTransaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TransactionManager {
    // 1. Singleton pattern: Đảm bảo chỉ có duy nhất 1 "két sắt" quản lý giao dịch
    private static TransactionManager instance;

    // 2. ObservableList: Giúp TableView tự động cập nhật khi bạn thêm mới giao dịch
    private final ObservableList<BidTransaction> transactionList = FXCollections.observableArrayList();

    private TransactionManager() {
        // Constructor private để không ai có thể khởi tạo mới từ ngoài
    }

    public static TransactionManager getInstance() {
        if (instance == null) {
            instance = new TransactionManager();
        }
        return instance;
    }

    // 3. Hàm thêm giao dịch mới vào "két sắt"
    public void addTransaction(BidTransaction trans) {
        transactionList.add(trans);
    }

    // 4. Hàm lấy danh sách để hiển thị lên giao diện
    public ObservableList<BidTransaction> getTransactionList() {
        return transactionList;
    }
}