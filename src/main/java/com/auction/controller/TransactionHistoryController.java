package com.auction.controller;

import com.auction.model.BidTransaction;
import com.auction.service.TransactionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class TransactionHistoryController {
    @FXML private TableView<BidTransaction> historyTable;
    @FXML private TableColumn<BidTransaction, Integer> colId;
    @FXML private TableColumn<BidTransaction, String> colBidder;
    @FXML private TableColumn<BidTransaction, String> colItem;
    @FXML private TableColumn<BidTransaction, Double> colAmount;
    @FXML private TableColumn<BidTransaction, String> colTime;

    @FXML
    public void initialize() {
        // Ánh xạ dữ liệu từ model vào bảng
        colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        colBidder.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBidderName()));
        colItem.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getItemName()));
        colAmount.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getBidAmount()).asObject());
        colTime.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormattedTime()));

        // Gán dữ liệu từ Manager vào bảng
        historyTable.setItems(TransactionManager.getInstance().getTransactionList());
    }
}