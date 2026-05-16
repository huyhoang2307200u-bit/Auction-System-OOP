package com.auction.controller;

import com.auction.client.ServerApiClient;
import com.auction.dto.BidDto;
import com.auction.model.BidTransaction;
import com.auction.util.MoneyUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalDateTime;
import java.util.List;

public class TransactionHistoryController {

    @FXML private TableView<BidTransaction> historyTable;
    @FXML private TableColumn<BidTransaction, Integer> colId;
    @FXML private TableColumn<BidTransaction, String> colBidder;
    @FXML private TableColumn<BidTransaction, String> colItem;
    @FXML private TableColumn<BidTransaction, String> colAmount;
    @FXML private TableColumn<BidTransaction, String> colTime;

    @FXML
    public void initialize() {
        if (colId != null) {
            colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getNumericId()).asObject());
        }
        colBidder.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBidderName()));
        colItem.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getItemName()));
        colAmount.setCellValueFactory(data -> new SimpleStringProperty(MoneyUtil.formatVnd(data.getValue().getBidAmountValue())));
        colTime.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormattedTime()));

        refreshHistory();
    }

    private void refreshHistory() {
        try {
            // Lấy dữ liệu thật từ database thông qua Socket API
            List<BidDto> dtoList = ServerApiClient.getInstance().getTransactionHistory();
            ObservableList<BidTransaction> list = FXCollections.observableArrayList();

            for (BidDto dto : dtoList) {
                int id = 0;
                try { id = Integer.parseInt(dto.getBidId()); } catch (Exception ignored) {}

                LocalDateTime time = null;
                try {
                    if (dto.getTimestamp() != null) {
                        time = LocalDateTime.parse(dto.getTimestamp());
                    }
                } catch (Exception ignored) {}

                // Kế thừa tạm để map dữ liệu chuẩn
                BidTransaction bt = new BidTransaction(id, null, null, dto.getAmount().doubleValue(), time) {
                    @Override public String getBidderName() { return dto.getBidderName(); }
                    @Override public String getItemName() { return dto.getItemName(); }
                };
                list.add(bt);
            }
            // Gán lên bảng
            Platform.runLater(() -> historyTable.setItems(list));
        } catch (Exception e) {
            System.err.println(">>> Lỗi khi làm mới lịch sử: " + e.getMessage());
        }
    }
}