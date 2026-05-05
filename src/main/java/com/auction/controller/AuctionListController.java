package com.auction.controller;

import com.auction.service.AuctionObserver;
import com.auction.model.Item;
import com.auction.model.User;
import com.auction.model.Admin; // Đã thêm import lớp Admin
import com.auction.service.AuctionManager;
import com.auction.service.BidService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class AuctionListController implements AuctionObserver {

    @FXML private TableView<Item> itemTable;
    @FXML private TextField bidAmountField;
    @FXML private Button btnEndAuction;
    @FXML private Label userInfoLabel;
    @FXML private TableColumn<Item, String> colId;
    @FXML private TableColumn<Item, String> colName;
    @FXML private TableColumn<Item, Double> colCurrentPrice;
    @FXML private TableColumn<Item, String> colStatus;

    private final BidService bidService = new BidService();
    private User currentUser;

    @FXML
    public void initialize() {
        // 1. Cấu hình bảng: Đổi màu dòng (Nhiệm vụ 2)
        itemTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (!item.isAuctionActive()) {
                    setStyle("-fx-background-color: #bdc3c7;"); // Màu xám cho phiên đã đóng
                } else {
                    setStyle("");
                }
            }
        });

        // 2. Kết nối cột
        colId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colCurrentPrice.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getCurrentPrice()).asObject());
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

        loadData();
        AuctionManager.getInstance().addObserver(this);

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> itemTable.refresh()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void loadData() {
        List<Item> items = AuctionManager.getInstance().getAvailableItems();
        itemTable.setItems(FXCollections.observableArrayList(items));
    }

    @FXML
    public void handleBidAction() {
        Item selectedItem = itemTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showNotification("Thông báo", "Vui lòng chọn sản phẩm!");
            return;
        }

        try {
            double amount = Double.parseDouble(bidAmountField.getText());
            if (bidService.placeBid(selectedItem, currentUser, amount)) {
                itemTable.refresh();
                showNotification("Thành công", "Đã đặt giá thành công!");
            } else {
                showNotification("Lỗi", "Giá đặt không hợp lệ hoặc phiên đã đóng!");
            }
        } catch (NumberFormatException e) {
            showNotification("Lỗi", "Vui lòng nhập số tiền hợp lệ!");
        } catch (Exception e) {
            showNotification("Lỗi", e.getMessage());
        }
    }

    @FXML
    public void handleEndAuction(ActionEvent event) {
        Item selectedItem = itemTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;

        selectedItem.setAuctionActive(false);
        itemTable.refresh();
        showNotification("Thành công", "Đã kết thúc phiên cho: " + selectedItem.getName());
    }

    public void initData(User user) {
        this.currentUser = user;
        userInfoLabel.setText("Xin chào: " + user.getName() + " | Quyền: " + user.getRole());

        // Kiểm tra logic dựa trên kiểu đối tượng thực tế thay vì chuỗi Role
        // Điều này đảm bảo dù Admin có kế thừa từ Bidder thì hệ thống vẫn nhận diện đúng
        boolean isAdmin = (user instanceof Admin);

        if (btnEndAuction != null) {
            btnEndAuction.setVisible(isAdmin);
            btnEndAuction.setManaged(isAdmin);
        }
    }

    @Override
    public void onPriceChanged(String itemId, double newPrice) {
        Platform.runLater(() -> {
            // Load lại toàn bộ danh sách từ Manager để đảm bảo có món mới
            List<Item> updatedItems = AuctionManager.getInstance().getAvailableItems();
            itemTable.setItems(FXCollections.observableArrayList(updatedItems));
            itemTable.refresh();
        });
    }

    private void showNotification(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleOpenAddItem() throws Exception {
        Stage stage = new Stage();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/AddItem.fxml"))));
        stage.show();
    }

    @FXML
    private void handleOpenHistory(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TransactionHistory.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Lịch sử giao dịch");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showNotification("Lỗi", "Không thể mở lịch sử giao dịch!");
        }
    }
}