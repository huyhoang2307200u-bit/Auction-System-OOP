package com.auction.controller;

import com.auction.model.Item;
import com.auction.model.User;
import com.auction.service.AuctionManager;
import com.auction.service.AuctionObserver;
import com.auction.service.BidService;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class AuctionListController implements AuctionObserver {

    @FXML private TableView<Item> itemTable;
    @FXML private TableColumn<Item, String> colId;
    @FXML private TableColumn<Item, String> colName;
    @FXML private TableColumn<Item, Double> colCurrentPrice;
    @FXML private TableColumn<Item, String> colStatus;

    @FXML private TextField bidAmountField;
    @FXML private Button btnEndAuction;
    @FXML private Button btnBid;
    @FXML private Label userInfoLabel;

    private final BidService bidService = new BidService();
    private User currentUser;

    // Sử dụng ObservableList để quản lý dữ liệu bảng mượt mà
    private ObservableList<Item> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configureTable();
        AuctionManager.getInstance().addObserver(this);

        // Bật/tắt input dựa trên trạng thái chọn sản phẩm (Trải nghiệm người dùng tốt hơn)
        itemTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            updateInputStates(newSelection);
        });
    }

    private void configureTable() {
        // Đổi màu dòng nếu phiên đấu giá đã kết thúc
        itemTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (!item.isAuctionActive()) {
                    setStyle("-fx-background-color: #bdc3c7;"); // Màu xám
                } else {
                    setStyle("");
                }
            }
        });

        // Bọc dữ liệu POJO chuẩn của dev thành JavaFX Property để hiển thị
        colId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colCurrentPrice.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getCurrentPrice()).asObject());
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
    }

    public void initData(User user) {
        this.currentUser = user;
        userInfoLabel.setText("Xin chào: " + user.getName() + " | Quyền: " + user.getRole());

        // Kiểm tra quyền an toàn dựa trên hàm isAdmin() của Entity dev
        boolean isAdmin = user.isAdmin();
        if (btnEndAuction != null) {
            btnEndAuction.setVisible(isAdmin);
            btnEndAuction.setManaged(isAdmin);
        }

        loadData();
    }

    private void loadData() {
        List<Item> items = AuctionManager.getInstance().getAvailableItems();
        masterData.setAll(items);
        itemTable.setItems(masterData);
        itemTable.refresh();
    }

    // Logic khóa nút nếu sản phẩm đã đóng
    private void updateInputStates(Item item) {
        if (item != null) {
            boolean isActive = item.isAuctionActive();
            if (btnBid != null) btnBid.setDisable(!isActive);
            if (bidAmountField != null) {
                bidAmountField.setDisable(!isActive);
                bidAmountField.setPromptText(isActive ? "Nhập giá..." : "Đã kết thúc");
                if (!isActive) bidAmountField.clear();
            }
        }
    }

    @FXML
    public void handleBidAction() {
        Item selectedItem = itemTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showNotification("Thông báo", "Vui lòng chọn sản phẩm!");
            return;
        }

        try {
            double amount = Double.parseDouble(bidAmountField.getText().trim());
            if (bidService.placeBid(selectedItem, currentUser, amount)) {
                loadData();
                showNotification("Thành công", "Đã đặt giá thành công!");
                bidAmountField.clear();
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
        loadData(); // Làm mới bảng ngay lập tức
        showNotification("Thành công", "Đã kết thúc phiên cho: " + selectedItem.getName());
    }

    @Override
    public void onPriceChanged(String itemId, double newPrice) {
        // Platform.runLater giúp an toàn khi cập nhật UI từ Thread khác (Socket/Observer)
        Platform.runLater(() -> {
            loadData();
            Item selected = itemTable.getSelectionModel().getSelectedItem();
            updateInputStates(selected);
        });
    }

    @FXML
    private void handleOpenAddItem() {
        try {
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL); // Khóa màn hình chính khi đang thêm SP
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/AddItem.fxml"))));
            stage.setTitle("Thêm sản phẩm mới");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showNotification("Lỗi", "Không thể mở màn hình thêm sản phẩm!");
        }
    }

    @FXML
    private void handleOpenHistory(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TransactionHistory.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Lịch sử giao dịch");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showNotification("Lỗi", "Không thể mở lịch sử giao dịch!");
        }
    }

    @FXML
    private void handleOpenNewWindow() {
        try {
            // Mở lại màn hình Đăng nhập trên một cửa sổ (Stage) hoàn toàn mới
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage newStage = new javafx.stage.Stage();
            newStage.setTitle("Giả lập người dùng (Client mới)");
            newStage.setScene(new javafx.scene.Scene(root));
            newStage.show();

            System.out.println(">>> Hệ thống: Đã mở thêm một cửa sổ Client giả lập.");
        } catch (Exception e) {
            e.printStackTrace();
            showNotification("Lỗi", "Không thể mở cửa sổ mới: " + e.getMessage());
        }
    }

    private void showNotification(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}