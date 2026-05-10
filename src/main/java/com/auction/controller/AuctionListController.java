package com.auction.controller;

import com.auction.service.AuctionClientService;
import com.auction.service.AuctionObserver;
import com.auction.model.Item;
import com.auction.model.User;
import com.auction.model.Admin;
import com.auction.service.AuctionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.List;
import javafx.event.ActionEvent;
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

    private User currentUser;
    private final ObservableList<Item> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. Cấu hình bảng màu sắc
        itemTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (!item.isAuctionActive()) {
                    setStyle("-fx-background-color: #bdc3c7;");
                } else {
                    setStyle("");
                }
            }
        });

        // 2. Map dữ liệu vào cột
        colId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colCurrentPrice.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getCurrentPrice()).asObject());
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

        itemTable.setItems(masterData);

        // 3. ĐĂNG KÝ OBSERVER (Quan trọng cho Real-time)
        AuctionManager.getInstance().addObserver(this);

        loadData();
    }

    private void loadData() {
        // Ép Manager nạp lại dữ liệu từ Database (thông qua DAO mà Manager đang giữ)
        AuctionManager.getInstance().refreshItemsFromDB();
        List<Item> items = AuctionManager.getInstance().getAvailableItems();
        masterData.setAll(items);
        itemTable.refresh();
    }

    @FXML
    public void handleBidAction() {
        Item selectedItem = itemTable.getSelectionModel().getSelectedItem();
        String amountText = bidAmountField.getText().trim();

        if (selectedItem == null || amountText.isEmpty()) {
            showNotification("Thông báo", "Vui lòng chọn sản phẩm và nhập giá!");
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);

            // Gửi lệnh sang Server (Middle-tier kiểm duyệt)
            AuctionClientService clientService = new AuctionClientService();
            // Định dạng %.0f để tránh số mũ E17 gây lỗi Server
            String request = "BID|" + selectedItem.getId() + "|" + String.format("%.0f", amount);
            String response = clientService.sendRequest(request);

            if (response.startsWith("SUCCESS")) {
                showNotification("Thành công", response.split("\\|")[1]);

                // --- BƯỚC HOÀN HẢO: KÍCH HOẠT REAL-TIME ---
                // Sau khi Server duyệt và ghi DB xong, ta báo cho Manager phát tín hiệu
                // Điều này làm tất cả các cửa sổ đang mở tự gọi hàm onPriceChanged
                AuctionManager.getInstance().notifyPriceChanged(selectedItem.getId(), amount);

                bidAmountField.clear();
            } else if (response.startsWith("REJECT")) {
                showNotification("Bị từ chối", "Lý do: " + response.split("\\|")[1]);
            } else {
                showNotification("Lỗi Server", response);
            }

        } catch (NumberFormatException e) {
            showNotification("Lỗi", "Giá đặt phải là số!");
        }
    }

    @Override
    public void onPriceChanged(String itemId, double newPrice) {
        // Platform.runLater giúp cập nhật giao diện từ luồng phụ an toàn
        Platform.runLater(() -> {
            System.out.println(">>> Cập nhật UI cho ID: " + itemId + " -> " + newPrice);
            loadData(); // Tải lại toàn bộ bảng để đồng bộ với Database Server
        });
    }

    @FXML
    private void handleOpenNewWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AuctionList.fxml"));
            Parent root = loader.load();
            AuctionListController controller = loader.getController();

            // Giả lập user khác để test
            User guestUser = new User("123", "User_Test", "USER");
            controller.initData(guestUser);

            Stage stage = new Stage();
            stage.setTitle("Cửa sổ Giả lập (Observer Mode)");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleEndAuction(ActionEvent event) {
        Item selectedItem = itemTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;

        // Thông báo kết thúc phiên (có thể mở rộng gửi lên Server tương tự như BID)
        selectedItem.setAuctionActive(false);
        AuctionManager.getInstance().notifyPriceChanged(selectedItem.getId(), selectedItem.getCurrentPrice());
        showNotification("Thành công", "Đã kết thúc phiên!");
    }

    public void initData(User user) {
        this.currentUser = user;
        userInfoLabel.setText("Xin chào: " + user.getName() + " | Quyền: " + user.getRole());

        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        if (btnEndAuction != null) {
            btnEndAuction.setVisible(isAdmin);
            btnEndAuction.setManaged(isAdmin);
        }
    }

    @FXML
    private void handleOpenAddItem() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddItem.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleOpenHistory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TransactionHistory.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showNotification(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}