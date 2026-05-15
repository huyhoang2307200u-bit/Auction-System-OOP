package com.auction.controller;

import com.auction.model.BidTransaction;
import com.auction.service.TransactionManager;
import com.auction.util.MoneyUtil;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class TransactionHistoryController {

    @FXML private TableView<BidTransaction> historyTable;
    @FXML private TableColumn<BidTransaction, Integer> colId;
    @FXML private TableColumn<BidTransaction, String> colBidder;
    @FXML private TableColumn<BidTransaction, String> colItem;
    @FXML private TableColumn<BidTransaction, String> colAmount;
    @FXML private TableColumn<BidTransaction, String> colTime;

    @FXML
    public void initialize() {
        // Sử dụng Property để bọc dữ liệu POJO, an toàn cho JSON/Socket
        if (colId != null) {
            colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getNumericId()).asObject());
        }
        colBidder.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBidderName()));
        colItem.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getItemName()));
        colAmount.setCellValueFactory(data -> new SimpleStringProperty(MoneyUtil.formatVnd(data.getValue().getBidAmountValue())));
        colTime.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormattedTime()));

        // Nạp dữ liệu từ Manager của dev
        refreshHistory();
    }

    private void refreshHistory() {
        try {
            historyTable.setItems(TransactionManager.getInstance().getTransactionList());
            System.out.println(">>> Hệ thống: Đã tải dữ liệu lịch sử giao dịch.");
        } catch (Exception e) {
            System.err.println(">>> Lỗi khi làm mới lịch sử: " + e.getMessage());
        }
    }
}