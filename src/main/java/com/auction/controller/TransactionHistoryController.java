package com.auction.controller;

import com.auction.dao.TransactionDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleStringProperty;

public class TransactionHistoryController {

    @FXML private TableView<String[]> historyTable;

    // Khai báo thêm colId để khớp với FXML của bạn
    @FXML private TableColumn<String[], String> colId;
    @FXML private TableColumn<String[], String> colBidder;
    @FXML private TableColumn<String[], String> colItem;
    @FXML private TableColumn<String[], String> colAmount;
    @FXML private TableColumn<String[], String> colTime;

    private final TransactionDAO transactionDAO = new TransactionDAO();

    @FXML
    public void initialize() {
        // Kiểm tra an toàn
        if (historyTable == null || colBidder == null) {
            System.err.println(">>> Lỗi: Không tìm thấy các cột. Kiểm tra fx:id trong FXML!");
            return;
        }

        // CẬP NHẬT INDEX: Phải khớp với thứ tự mảng String[] trong TransactionDAO.getFullHistory()
        // [0]: ID, [1]: Username, [2]: ItemName, [3]: Amount, [4]: Formatted Time

        if (colId != null) {
            colId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));
        }

        colBidder.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));
        colItem.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[2]));
        colAmount.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[3]));
        colTime.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[4]));

        // Tải dữ liệu từ DB
        refreshHistory();
    }

    private void refreshHistory() {
        try {
            // Lấy dữ liệu mới từ DAO
            ObservableList<String[]> data = FXCollections.observableArrayList(transactionDAO.getFullHistory());
            historyTable.setItems(data);
            System.out.println(">>> Hệ thống: Đã hiển thị " + data.size() + " bản ghi giao dịch.");
        } catch (Exception e) {
            System.err.println(">>> Lỗi khi làm mới lịch sử: " + e.getMessage());
        }
    }
}