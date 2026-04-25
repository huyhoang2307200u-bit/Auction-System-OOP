package com.auction.controller;

import com.auction.model.Item;
import com.auction.model.User;
import com.auction.service.AuctionManager;
import com.auction.service.BidService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import java.util.List;
import javafx.event.ActionEvent;

public class AuctionListController {

    @FXML
    private TableView<Item> itemTable;

    @FXML
    private TextField bidAmountField;

    private final BidService bidService = new BidService();
    // Tạo tạm một user để demo, sau này có thể lấy từ màn hình Login
    private final User currentUser = new User("user1", "Người dùng A");

    @FXML
    public void initialize() {
        loadData();
    }

    private void loadData() {
        List<Item> items = AuctionManager.getInstance().getAvailableItems();
        ObservableList<Item> observableItems = FXCollections.observableArrayList(items);
        itemTable.setItems(observableItems);
    }

    @FXML
    public void handleBidAction() {
        Item selectedItem = itemTable.getSelectionModel().getSelectedItem();

        if (selectedItem == null) {
            showNotification("Thông báo", "Vui lòng chọn sản phẩm muốn đấu giá!");
            return;
        }

        try {
            double amount = Double.parseDouble(bidAmountField.getText());

            if (bidService.placeBid(selectedItem, currentUser, amount)) {
                itemTable.refresh();
                showNotification("Thành công", "Đã đặt giá thành công cho: " + selectedItem.getName());
            } else {
                showNotification("Lỗi", "Giá đặt phải lớn hơn giá hiện tại!");
            }
        } catch (NumberFormatException e) {
            showNotification("Lỗi", "Vui lòng nhập số tiền hợp lệ!");
        }
    }
    @FXML
    private void handleEndAuction(ActionEvent event) {
        Item selectedItem = itemTable.getSelectionModel().getSelectedItem();

        if (selectedItem == null) {
            showNotification("Thông báo", "Vui lòng chọn sản phẩm để kết thúc!");
            return;
        }

        // Đánh dấu sản phẩm đã kết thúc
        selectedItem.setAuctionActive(false);
        itemTable.refresh();
        showNotification("Thành công", "Đã kết thúc phiên cho: " + selectedItem.getName());
    }

    private void showNotification(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}