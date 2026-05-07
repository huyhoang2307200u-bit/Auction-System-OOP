package com.auction.controller;

import com.auction.dao.ItemDAO;
import com.auction.model.Item;
import com.auction.model.User;
import com.auction.service.AuctionManager;
import com.auction.service.AuctionObserver;
import com.auction.service.BidService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class AuctionListController implements AuctionObserver {

    @FXML private TableView<Item> itemTable;
    @FXML private TableColumn<Item, String> colId;
    @FXML private TableColumn<Item, String> colName;
    @FXML private TableColumn<Item, Double> colCurrentPrice;
    @FXML private TableColumn<Item, String> colStatus;

    @FXML private TextField bidAmountField;
    @FXML private Button btnEndAuction;
    @FXML private Label userInfoLabel;
    @FXML private Button btnBid;

    private final BidService bidService = new BidService();
    private final ItemDAO itemDAO = new ItemDAO();
    private User currentUser;
    // Sử dụng ObservableList để quản lý dữ liệu bảng hiệu quả hơn
    private ObservableList<Item> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configureTable();
        // Đăng ký nhận thông báo thay đổi từ Manager
        AuctionManager.getInstance().addObserver(this);

        // Lắng nghe sự kiện chọn dòng để bật/tắt nút đấu giá
        itemTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            updateInputStates(newSelection);
        });
    }

    private void updateInputStates(Item item) {
        if (item != null) {
            boolean isActive = item.isAuctionActive();
            btnBid.setDisable(!isActive);
            bidAmountField.setDisable(!isActive);
            bidAmountField.setPromptText(isActive ? "Nhập giá..." : "Đã kết thúc");
            if (!isActive) bidAmountField.clear();
        }
    }

    private void configureTable() {
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

        colId.setCellValueFactory(data -> data.getValue().idProperty());
        colName.setCellValueFactory(data -> data.getValue().nameProperty());
        colCurrentPrice.setCellValueFactory(data -> data.getValue().currentPriceProperty().asObject());
        colStatus.setCellValueFactory(data -> data.getValue().statusProperty());
    }

    private void loadData() {
        List<Item> items = itemDAO.getAllItems();
        masterData.setAll(items);
        itemTable.setItems(masterData);
        itemTable.refresh();
    }

    public void initData(User user) {
        this.currentUser = user;
        userInfoLabel.setText("Xin chào: " + user.getName() + " | Quyền: " + user.getRole());

        // Chỉ hiện nút Kết thúc nếu là ADMIN (Logic thực tế)
        boolean isAdmin = "ADMIN".equals(user.getRole());
        if (btnEndAuction != null) {
            btnEndAuction.setVisible(isAdmin);
            btnEndAuction.setManaged(isAdmin);
        }

        loadData();
    }

    @FXML
    public void handleBidAction() {
        Item selectedItem = itemTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showNotification("Thông báo", "Vui lòng chọn sản phẩm trên bảng!");
            return;
        }

        if (!selectedItem.isAuctionActive()) {
            showNotification("Lỗi", "Sản phẩm này đã kết thúc đấu giá!");
            return;
        }

        try {
            String text = bidAmountField.getText().trim();
            if (text.isEmpty()) return;

            double amount = Double.parseDouble(text);

            if (bidService.placeBid(selectedItem, currentUser, amount)) {
                // SỬA LỖI TẠI ĐÂY: Gọi đúng hàm notifyPriceChanged
                AuctionManager.getInstance().notifyPriceChanged(selectedItem.getId(), amount);
                showNotification("Thành công", "Đã đặt giá: " + amount);
                bidAmountField.clear();
                // Không cần loadData() ở đây vì onPriceChanged sẽ tự lo
            } else {
                showNotification("Lỗi", "Giá đặt phải cao hơn giá hiện tại!");
            }
        } catch (NumberFormatException e) {
            showNotification("Lỗi", "Vui lòng nhập số tiền hợp lệ!");
        }
    }

    @Override
    public void onPriceChanged(String itemId, double newPrice) {
        // Cập nhật giao diện an toàn từ Thread khác
        Platform.runLater(() -> {
            loadData(); // Cách đơn giản nhất để đồng bộ trạng thái màu sắc và giá

            // Giữ lại lựa chọn hiện tại sau khi refresh
            Item selected = itemTable.getSelectionModel().getSelectedItem();
            updateInputStates(selected);
        });
    }

    @FXML
    private void handleEndAuction() {
        Item selectedItem = itemTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null && selectedItem.isAuctionActive()) {
            selectedItem.setAuctionActive(false);
            itemDAO.updateStatus(selectedItem.getId(), false);

            // Thông báo để tất cả các Client khác cũng thấy phiên này đã đóng
            AuctionManager.getInstance().notifyPriceChanged(selectedItem.getId(), selectedItem.getCurrentPrice());

            showNotification("Thành công", "Đã kết thúc phiên đấu giá!");
        } else {
            showNotification("Thông báo", "Vui lòng chọn sản phẩm đang hoạt động!");
        }
    }

    @FXML
    private void handleOpenAddItem() {
        try {
            // Thử lấy file từ thư mục /fxml/
            String fxmlPath = "/fxml/AddItem.fxml";
            java.net.URL location = getClass().getResource(fxmlPath);

            // Nếu không thấy, thử lại ở package view
            if (location == null) {
                location = getClass().getResource("/com/auction/view/AddItem.fxml");
            }

            if (location == null) {
                showNotification("Lỗi", "Không tìm thấy file AddItem.fxml. Hãy kiểm tra lại vị trí file!");
                return;
            }

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(location);
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setTitle("Thêm sản phẩm mới");
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();

            System.out.println(">>> Hệ thống: Mở giao diện thêm sản phẩm thành công.");
        } catch (Exception e) {
            e.printStackTrace();
            showNotification("Lỗi", "Lỗi nạp giao diện: " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenHistory() {
        try {
            String path = "/fxml/TransactionHistory.fxml";
            java.net.URL fxmlLocation = getClass().getResource(path);

            if (fxmlLocation == null) {
                fxmlLocation = getClass().getResource("/com/auction/view/TransactionHistory.fxml");
            }

            if (fxmlLocation == null) {
                showNotification("Lỗi hệ thống", "Không tìm thấy file TransactionHistory.fxml");
                return;
            }

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(fxmlLocation);
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Lịch sử giao dịch hệ thống");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.show();

            System.out.println(">>> Hệ thống: Mở lịch sử giao dịch thành công.");
        } catch (Exception e) {
            e.printStackTrace();
            showNotification("Lỗi", "Lỗi mở lịch sử: " + e.getMessage());
        }
    }

    private void showNotification(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}