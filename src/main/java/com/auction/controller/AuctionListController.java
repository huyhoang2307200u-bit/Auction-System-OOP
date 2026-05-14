package com.auction.controller;

import com.auction.model.BidTransaction;
import com.auction.model.DepositRequest;
import com.auction.model.Item;
import com.auction.model.Role;
import com.auction.model.User;
import com.auction.service.AuctionManager;
import com.auction.service.AuctionObserver;
import com.auction.service.BidService;
import com.auction.service.WalletManager;
import com.auction.service.WalletObserver;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AuctionListController implements AuctionObserver, WalletObserver {

    @FXML private TableView<Item> itemTable;
    @FXML private TableColumn<Item, String> colId;
    @FXML private TableColumn<Item, String> colName;
    @FXML private TableColumn<Item, Double> colCurrentPrice;
    @FXML private TableColumn<Item, String> colHighestBidder;
    @FXML private TableColumn<Item, String> colEndTime;
    @FXML private TableColumn<Item, String> colStatus;

    @FXML private TextField bidAmountField;
    @FXML private TextField autoMaxBidField;
    @FXML private TextField autoIncrementField;
    @FXML private TextField depositAmountField;
    @FXML private Label balanceLabel;
    @FXML private Button btnEndAuction;
    @FXML private Button btnBid;
    @FXML private Button btnAutoBid;
    @FXML private Button btnDeposit;
    @FXML private Button btnReviewDeposits;
    @FXML private Label pendingDepositLabel;
    @FXML private Button btnAddItem;
    @FXML private Label userInfoLabel;
    @FXML private Label selectedItemLabel;
    @FXML private LineChart<String, Number> priceChart;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM");

    private final BidService bidService = new BidService();
    private final ObservableList<Item> masterData = FXCollections.observableArrayList();
    private final XYChart.Series<String, Number> priceSeries = new XYChart.Series<>();
    private ObservableList<DepositRequest> pendingDepositDialogData;
    private TableView<DepositRequest> pendingDepositDialogTable;
    private User currentUser;

    @FXML
    public void initialize() {
        configureTable();
        configureChart();
        AuctionManager.getInstance().addObserver(this);
        WalletManager.getInstance().addObserver(this);

        itemTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            updateInputStates(newSelection);
            updateChart(newSelection);
        });
    }

    private void configureTable() {
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

        colId.setCellValueFactory(data -> new SimpleStringProperty(shortId(data.getValue().getId())));
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colCurrentPrice.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getCurrentPrice()).asObject());
        colHighestBidder.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getHighestBidderName()));
        colEndTime.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getEndTime() == null ? "Chưa đặt" : data.getValue().getEndTime().format(TIME_FORMATTER)));
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
    }

    private void configureChart() {
        if (priceChart != null) {
            priceSeries.setName("Giá cao nhất theo thời gian");
            priceChart.getData().setAll(priceSeries);
            priceChart.setAnimated(false);
        }
    }

    public void initData(User user) {
        this.currentUser = user;
        userInfoLabel.setText("Xin chào: " + user.getName() + " | Quyền: " + user.getRole());
        updateBalanceLabel();

        boolean canManageProducts = user.getRole() == Role.SELLER || user.getRole() == Role.ADMIN;
        boolean canForceEnd = user.getRole() == Role.ADMIN;
        boolean canBid = user.getRole() == Role.BIDDER;

        if (btnAddItem != null) {
            btnAddItem.setVisible(canManageProducts);
            btnAddItem.setManaged(canManageProducts);
        }
        if (btnEndAuction != null) {
            btnEndAuction.setVisible(canForceEnd);
            btnEndAuction.setManaged(canForceEnd);
        }
        if (btnBid != null) {
            btnBid.setDisable(!canBid);
        }
        if (btnAutoBid != null) {
            btnAutoBid.setDisable(!canBid);
        }
        if (depositAmountField != null) {
            depositAmountField.setVisible(canBid);
            depositAmountField.setManaged(canBid);
        }
        if (balanceLabel != null) {
            balanceLabel.setVisible(canBid);
            balanceLabel.setManaged(canBid);
        }
        if (btnDeposit != null) {
            btnDeposit.setVisible(canBid);
            btnDeposit.setManaged(canBid);
            btnDeposit.setText("Gửi yêu cầu nạp");
        }
        if (btnReviewDeposits != null) {
            btnReviewDeposits.setVisible(canForceEnd);
            btnReviewDeposits.setManaged(canForceEnd);
        }
        if (pendingDepositLabel != null) {
            pendingDepositLabel.setVisible(canForceEnd);
            pendingDepositLabel.setManaged(canForceEnd);
        }

        loadData();
        updateDepositReviewIndicator();
    }

    private void loadData() {
        List<Item> items = AuctionManager.getInstance().getAvailableItems();
        masterData.setAll(items);
        itemTable.setItems(masterData);
        itemTable.refresh();
    }

    private void updateInputStates(Item item) {
        boolean selectedAndActive = item != null && item.isAuctionActive();
        boolean isBidder = currentUser != null && currentUser.getRole() == Role.BIDDER;

        if (selectedItemLabel != null) {
            selectedItemLabel.setText(item == null ? "Chưa chọn phiên" : "Đang xem: " + item.getName());
        }
        if (btnBid != null) {
            btnBid.setDisable(!selectedAndActive || !isBidder);
        }
        if (btnAutoBid != null) {
            btnAutoBid.setDisable(!selectedAndActive || !isBidder);
        }
        if (bidAmountField != null) {
            bidAmountField.setDisable(!selectedAndActive || !isBidder);
            bidAmountField.setPromptText(selectedAndActive ? "Nhập giá..." : "Đã kết thúc");
            if (!selectedAndActive) {
                bidAmountField.clear();
            }
        }
        if (autoMaxBidField != null) {
            autoMaxBidField.setDisable(!selectedAndActive || !isBidder);
        }
        if (autoIncrementField != null) {
            autoIncrementField.setDisable(!selectedAndActive || !isBidder);
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
                updateBalanceLabel();
                updateChart(selectedItem);
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
    public void handleAutoBidAction() {
        Item selectedItem = itemTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showNotification("Thông báo", "Vui lòng chọn sản phẩm!");
            return;
        }

        try {
            double maxBid = Double.parseDouble(autoMaxBidField.getText().trim());
            double increment = Double.parseDouble(autoIncrementField.getText().trim());
            AuctionManager.getInstance().registerAutoBid(selectedItem.getId(), currentUser, maxBid, increment);
            loadData();
            updateBalanceLabel();
            updateChart(selectedItem);
            showNotification("Thành công", "Đã bật đấu giá tự động.");
        } catch (NumberFormatException e) {
            showNotification("Lỗi", "Max bid và bước giá phải là số hợp lệ!");
        } catch (Exception e) {
            showNotification("Lỗi", e.getMessage());
        }
    }

    @FXML
    public void handleDepositAction() {
        if (currentUser == null) {
            showNotification("Lỗi", "Bạn cần đăng nhập trước khi yêu cầu nạp tiền.");
            return;
        }
        try {
            double amount = Double.parseDouble(depositAmountField.getText().trim());
            if (amount <= 0) {
                showNotification("Lỗi", "Số tiền nạp phải lớn hơn 0.");
                return;
            }
            DepositRequest request = WalletManager.getInstance().requestDeposit(currentUser, amount);
            depositAmountField.clear();
            updateDepositReviewIndicator();
            showNotification("Đã gửi yêu cầu",
                    "Yêu cầu nạp " + amount + " đã được gửi cho Admin kiểm duyệt. "
                            + "Số dư chỉ tăng sau khi Admin duyệt. Mã yêu cầu: " + shortId(request.getId()));
        } catch (NumberFormatException e) {
            showNotification("Lỗi", "Vui lòng nhập số tiền nạp hợp lệ!");
        } catch (Exception e) {
            showNotification("Lỗi", e.getMessage());
        }
    }

    @FXML
    private void handleReviewDeposits() {
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            showNotification("Lỗi", "Chỉ Admin mới được kiểm duyệt yêu cầu nạp tiền.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Kiểm duyệt yêu cầu nạp tiền");
        dialog.setHeaderText("Admin duyệt hoặc từ chối các yêu cầu nạp tiền đang chờ.");

        TableView<DepositRequest> table = new TableView<>();
        table.setPrefSize(680, 320);

        TableColumn<DepositRequest, String> idCol = new TableColumn<>("Mã");
        idCol.setPrefWidth(90);
        idCol.setCellValueFactory(data -> new SimpleStringProperty(shortId(data.getValue().getId())));

        TableColumn<DepositRequest, String> userCol = new TableColumn<>("Người yêu cầu");
        userCol.setPrefWidth(180);
        userCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDisplayName() + " (" + data.getValue().getUsername() + ")"));

        TableColumn<DepositRequest, Double> amountCol = new TableColumn<>("Số tiền");
        amountCol.setPrefWidth(120);
        amountCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getAmount()).asObject());

        TableColumn<DepositRequest, String> timeCol = new TableColumn<>("Thời điểm gửi");
        timeCol.setPrefWidth(160);
        timeCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getRequestedAt().format(TIME_FORMATTER)));

        TableColumn<DepositRequest, String> statusCol = new TableColumn<>("Trạng thái");
        statusCol.setPrefWidth(110);
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().name()));

        table.getColumns().setAll(idCol, userCol, amountCol, timeCol, statusCol);
        pendingDepositDialogTable = table;
        pendingDepositDialogData = FXCollections.observableArrayList(WalletManager.getInstance().getPendingRequests());
        table.setItems(pendingDepositDialogData);

        TextArea reasonArea = new TextArea();
        reasonArea.setPromptText("Lý do từ chối (nếu từ chối)");
        reasonArea.setPrefRowCount(3);

        Button approveButton = new Button("Duyệt nạp tiền");
        Button rejectButton = new Button("Từ chối");
        approveButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        rejectButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");

        approveButton.setOnAction(event -> {
            DepositRequest selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showNotification("Thông báo", "Vui lòng chọn một yêu cầu nạp tiền.");
                return;
            }
            try {
                WalletManager.getInstance().approveDeposit(selected.getId(), currentUser);
                updateDepositReviewIndicator();
                updateBalanceLabel();
                showNotification("Thành công", "Đã duyệt nạp tiền cho " + selected.getUsername()
                        + ". Số dư người dùng đã được cộng.");
            } catch (Exception e) {
                showNotification("Lỗi", e.getMessage());
            }
        });

        rejectButton.setOnAction(event -> {
            DepositRequest selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showNotification("Thông báo", "Vui lòng chọn một yêu cầu nạp tiền.");
                return;
            }
            try {
                WalletManager.getInstance().rejectDeposit(selected.getId(), currentUser, reasonArea.getText());
                updateDepositReviewIndicator();
                reasonArea.clear();
                showNotification("Thành công", "Đã từ chối yêu cầu nạp tiền của " + selected.getUsername() + ".");
            } catch (Exception e) {
                showNotification("Lỗi", e.getMessage());
            }
        });

        HBox actions = new HBox(10, approveButton, rejectButton);
        VBox content = new VBox(10, table, reasonArea, actions);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE));
        dialog.setOnHidden(event -> {
            if (pendingDepositDialogTable == table) {
                pendingDepositDialogTable = null;
                pendingDepositDialogData = null;
            }
            updateDepositReviewIndicator();
        });

        Node closeButton = dialog.getDialogPane().lookupButton(dialog.getDialogPane().getButtonTypes().get(0));
        if (closeButton != null) {
            closeButton.setFocusTraversable(false);
        }
        dialog.showAndWait();
    }

    @FXML
    public void handleEndAuction(ActionEvent event) {
        Item selectedItem = itemTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            return;
        }

        try {
            AuctionManager.getInstance().finishAuction(selectedItem.getId());
            loadData();
            showNotification("Thành công", "Đã kết thúc phiên cho: " + selectedItem.getName());
        } catch (Exception e) {
            showNotification("Lỗi", e.getMessage());
        }
    }

    @Override
    public void onPriceChanged(String itemId, double newPrice) {
        Platform.runLater(() -> {
            Item selected = itemTable.getSelectionModel().getSelectedItem();
            loadData();
            if (selected != null) {
                itemTable.getSelectionModel().select(selected);
            }
            updateInputStates(selected);
            updateBalanceLabel();
            updateChart(selected);
        });
    }

    @Override
    public void onWalletChanged(String username) {
        Platform.runLater(() -> {
            updateBalanceLabel();
            updateDepositReviewIndicator();
        });
    }

    @FXML
    private void handleOpenAddItem() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddItem.fxml"));
            Parent root = loader.load();
            AddItemController controller = loader.getController();
            controller.initData(currentUser);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setTitle("Thêm sản phẩm mới");
            stage.showAndWait();
            loadData();
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();

            Stage newStage = new Stage();
            newStage.setTitle("Giả lập người dùng (Client mới)");
            newStage.setScene(new Scene(root));
            newStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showNotification("Lỗi", "Không thể mở cửa sổ mới: " + e.getMessage());
        }
    }

    private void updateChart(Item item) {
        if (priceChart == null || item == null) {
            return;
        }
        priceSeries.getData().clear();
        List<BidTransaction> history = AuctionManager.getInstance().getBidHistory(item.getId());
        if (history.isEmpty()) {
            priceSeries.getData().add(new XYChart.Data<>("Khởi điểm", item.getCurrentPrice()));
            return;
        }
        for (BidTransaction bid : history) {
            String label = bid.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            priceSeries.getData().add(new XYChart.Data<>(label, bid.getBidAmount()));
        }
    }

    private void updateDepositReviewIndicator() {
        boolean isAdmin = currentUser != null && currentUser.getRole() == Role.ADMIN;
        int pendingCount = WalletManager.getInstance().getPendingRequests().size();

        if (btnReviewDeposits != null && isAdmin) {
            btnReviewDeposits.setText(pendingCount > 0
                    ? "Duyệt yêu cầu nạp tiền (" + pendingCount + ")"
                    : "Duyệt yêu cầu nạp tiền");
        }

        if (pendingDepositLabel != null) {
            pendingDepositLabel.setVisible(isAdmin);
            pendingDepositLabel.setManaged(isAdmin);
            if (isAdmin) {
                pendingDepositLabel.setText(pendingCount > 0
                        ? "Có " + pendingCount + " yêu cầu nạp đang chờ duyệt."
                        : "Không có yêu cầu nạp đang chờ.");
            }
        }

        if (pendingDepositDialogData != null) {
            pendingDepositDialogData.setAll(WalletManager.getInstance().getPendingRequests());
        }
        if (pendingDepositDialogTable != null) {
            pendingDepositDialogTable.refresh();
        }
    }

    private void updateBalanceLabel() {
        if (balanceLabel != null && currentUser != null) {
            balanceLabel.setText(String.format("Số dư ví: %.2f", currentUser.getBalance()));
        }
    }

    private String shortId(String id) {
        if (id == null || id.length() <= 8) {
            return id;
        }
        return id.substring(0, 8);
    }

    private void showNotification(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
