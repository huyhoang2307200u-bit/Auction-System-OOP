package com.auction.controller;

import com.auction.client.ServerApiClient;
import com.auction.common.AuctionDTO;
import com.auction.common.Response;
import com.auction.dto.DepositRequestDto;
import com.auction.dto.NotificationDto;
import com.auction.model.Item;
import com.auction.model.Role;
import com.auction.model.ServerAuctionItem;
import com.auction.model.User;
import com.auction.util.MoneyUtil;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class AuctionListController {

    @FXML private TableView<Item> itemTable;
    @FXML private TableColumn<Item, String> colId;
    @FXML private TableColumn<Item, String> colName;
    @FXML private TableColumn<Item, String> colCurrentPrice;
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
    @FXML private ImageView itemImageView;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM");

    private final ServerApiClient apiClient = ServerApiClient.getInstance();
    private final ObservableList<Item> masterData = FXCollections.observableArrayList();
    private final XYChart.Series<String, Number> priceSeries = new XYChart.Series<>();
    private ObservableList<DepositRequestDto> pendingDepositDialogData;
    private TableView<DepositRequestDto> pendingDepositDialogTable;
    private User currentUser;

    @FXML
    public void initialize() {
        configureTable();
        configureChart();
        startCountdownTimer();
        apiClient.addRealtimeListener(this::handleRealtimeEvent);

        itemTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            updateInputStates(newSelection);
            updateChart(newSelection);
            updateImage(newSelection);
        });
    }

    private void startCountdownTimer() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            itemTable.refresh();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
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

        colId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colCurrentPrice.setCellValueFactory(data -> new SimpleStringProperty(MoneyUtil.formatVnd(data.getValue().getCurrentPriceValue())));
        colHighestBidder.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getHighestBidderName()));
        colEndTime.setCellValueFactory(data -> {
            if (data.getValue() instanceof ServerAuctionItem serverItem && "PENDING_APPROVAL".equalsIgnoreCase(serverItem.getServerStatus())) {
                return new SimpleStringProperty("Chưa bắt đầu");
            }
            LocalDateTime end = data.getValue().getEndTime();
            if (end == null) return new SimpleStringProperty("Chưa đặt");
            if (!data.getValue().isAuctionActive()) return new SimpleStringProperty("Đã kết thúc");
            
            java.time.Duration duration = java.time.Duration.between(LocalDateTime.now(), end);
            if (duration.isNegative() || duration.isZero()) {
                return new SimpleStringProperty("Đã kết thúc");
            }
            long hours = duration.toHours();
            long minutes = duration.toMinutesPart();
            long seconds = duration.toSecondsPart();
            return new SimpleStringProperty(String.format("%02d:%02d:%02d", hours, minutes, seconds));
        });
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
        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean canBid = user.getRole() == Role.BIDDER;

        if (bidAmountField != null) {
            bidAmountField.setPromptText("VD: 1.000.000VND");
        }
        if (autoMaxBidField != null) {
            autoMaxBidField.setPromptText("Giá tối đa, VD: 2.000.000VND");
        }
        if (autoIncrementField != null) {
            autoIncrementField.setPromptText("Bước giá, VD: 50.000VND");
        }
        if (depositAmountField != null) {
            depositAmountField.setPromptText("VD: 1.000.000VND");
        }

        if (btnAddItem != null) {
            btnAddItem.setVisible(canManageProducts);
            btnAddItem.setManaged(canManageProducts);
        }
        if (btnEndAuction != null) {
            btnEndAuction.setVisible(isAdmin);
            btnEndAuction.setManaged(isAdmin);
        }
        if (btnBid != null) btnBid.setDisable(!canBid);
        if (btnAutoBid != null) btnAutoBid.setDisable(!canBid);
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
            btnReviewDeposits.setVisible(isAdmin);
            btnReviewDeposits.setManaged(isAdmin);
        }
        if (pendingDepositLabel != null) {
            pendingDepositLabel.setVisible(isAdmin);
            pendingDepositLabel.setManaged(isAdmin);
        }

        loadData();
        updateDepositReviewIndicator();
        showUnreadNotificationsForCurrentUser();
    }

    private void loadData() {
        try {
            List<AuctionDTO> auctions = apiClient.getAuctions();
            List<Item> items = auctions.stream().map(ServerAuctionItem::new).map(item -> (Item) item).toList();
            Item selected = itemTable == null ? null : itemTable.getSelectionModel().getSelectedItem();
            masterData.setAll(items);
            itemTable.setItems(masterData);
            if (selected != null) {
                masterData.stream()
                        .filter(item -> item.getId().equals(selected.getId()))
                        .findFirst()
                        .ifPresent(item -> itemTable.getSelectionModel().select(item));
            }
            itemTable.refresh();
        } catch (Exception e) {
            showNotification("Lỗi tải dữ liệu", e.getMessage());
        }
    }

    private void updateInputStates(Item item) {
        boolean selectedAndActive = item != null && item.isAuctionActive();
        boolean isBidder = currentUser != null && currentUser.getRole() == Role.BIDDER;
        boolean isAdmin = currentUser != null && currentUser.getRole() == Role.ADMIN;
        boolean pendingApproval = item instanceof ServerAuctionItem serverItem
                && "PENDING_APPROVAL".equalsIgnoreCase(serverItem.getServerStatus());

        if (selectedItemLabel != null) {
            selectedItemLabel.setText(item == null ? "Chưa chọn phiên" : "Đang xem: " + item.getName());
        }
        if (btnBid != null) btnBid.setDisable(!selectedAndActive || !isBidder);
        if (btnAutoBid != null) btnAutoBid.setDisable(!selectedAndActive || !isBidder);
        if (bidAmountField != null) {
            bidAmountField.setDisable(!selectedAndActive || !isBidder);
            bidAmountField.setPromptText(selectedAndActive ? "VD: 1.000.000VND" : "Không thể đấu giá");
            if (!selectedAndActive) bidAmountField.clear();
        }
        if (autoMaxBidField != null) autoMaxBidField.setDisable(!selectedAndActive || !isBidder);
        if (autoIncrementField != null) autoIncrementField.setDisable(!selectedAndActive || !isBidder);

        if (btnEndAuction != null && isAdmin) {
            btnEndAuction.setText(pendingApproval ? "Duyệt phiên" : "Kết thúc phiên");
            btnEndAuction.setDisable(item == null || (!pendingApproval && !selectedAndActive));
        }
    }

    private void updateImage(Item item) {
        if (itemImageView == null) return;
        if (item != null && item.getImageDataUrl() != null && !item.getImageDataUrl().isEmpty()) {
            try {
                Image image = new Image(item.getImageDataUrl());
                itemImageView.setImage(image);
            } catch (Exception e) {
                itemImageView.setImage(null);
            }
        } else {
            itemImageView.setImage(null);
        }
    }

    @FXML
    public void handleBidAction() {
        ServerAuctionItem selectedItem = getSelectedServerItem();
        if (selectedItem == null) {
            showNotification("Thông báo", "Vui lòng chọn sản phẩm!");
            return;
        }

        try {
            double amount = MoneyUtil.parseUserAmount(bidAmountField.getText()).doubleValue();
            apiClient.placeBid(selectedItem.getAuctionId(), amount);
            loadData();
            updateBalanceLabel();
            updateChart(selectedItem);
            showNotification("Thành công", "Đã đặt giá " + MoneyUtil.formatVnd(amount) + " thành công!");
            bidAmountField.clear();
        } catch (NumberFormatException e) {
            showNotification("Lỗi", "Vui lòng nhập số tiền hợp lệ. Ví dụ: 1.000.000VND");
        } catch (Exception e) {
            showNotification("Lỗi", e.getMessage());
        }
    }

    @FXML
    public void handleAutoBidAction() {
        ServerAuctionItem selectedItem = getSelectedServerItem();
        if (selectedItem == null) {
            showNotification("Thông báo", "Vui lòng chọn sản phẩm!");
            return;
        }

        try {
            double maxBid = MoneyUtil.parseUserAmount(autoMaxBidField.getText()).doubleValue();
            double increment = MoneyUtil.parseUserAmount(autoIncrementField.getText()).doubleValue();
            apiClient.registerAutoBid(selectedItem.getAuctionId(), maxBid, increment);
            loadData();
            showNotification("Thành công", "Server đã nhận cấu hình auto-bid.");
        } catch (NumberFormatException e) {
            showNotification("Lỗi", "Max bid và bước giá phải là số tiền hợp lệ. Ví dụ: 2.000.000VND");
        } catch (Exception e) {
            showNotification("Lỗi", e.getMessage());
        }
    }

    @FXML
    public void handleDepositAction() {
        try {
            double amount = MoneyUtil.parseUserAmount(depositAmountField.getText()).doubleValue();
            if (amount <= 0) {
                showNotification("Lỗi", "Số tiền nạp phải lớn hơn 0.");
                return;
            }
            apiClient.requestDeposit(amount);
            depositAmountField.clear();
            updateDepositReviewIndicator();
            showNotification("Đã gửi yêu cầu", "Yêu cầu nạp " + MoneyUtil.formatVnd(amount)
                    + " đã được gửi cho Admin kiểm duyệt. Số dư chỉ tăng sau khi Admin duyệt.");
        } catch (NumberFormatException e) {
            showNotification("Lỗi", "Vui lòng nhập số tiền nạp hợp lệ. Ví dụ: 1.000.000VND");
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

        TableView<DepositRequestDto> table = new TableView<>();
        table.setPrefSize(680, 320);

        TableColumn<DepositRequestDto, String> idCol = new TableColumn<>("Mã");
        idCol.setPrefWidth(70);
        idCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));

        TableColumn<DepositRequestDto, String> userCol = new TableColumn<>("Người yêu cầu");
        userCol.setPrefWidth(180);
        userCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));

        TableColumn<DepositRequestDto, String> amountCol = new TableColumn<>("Số tiền");
        amountCol.setPrefWidth(140);
        amountCol.setCellValueFactory(data -> new SimpleStringProperty(MoneyUtil.formatVnd(data.getValue().getAmount())));

        TableColumn<DepositRequestDto, String> timeCol = new TableColumn<>("Thời điểm gửi");
        timeCol.setPrefWidth(190);
        timeCol.setCellValueFactory(data -> new SimpleStringProperty(formatDateTime(data.getValue().getRequestedAt())));

        TableColumn<DepositRequestDto, String> statusCol = new TableColumn<>("Trạng thái");
        statusCol.setPrefWidth(100);
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

        table.getColumns().setAll(idCol, userCol, amountCol, timeCol, statusCol);
        pendingDepositDialogTable = table;
        pendingDepositDialogData = FXCollections.observableArrayList(loadPendingDepositsSafe());
        table.setItems(pendingDepositDialogData);

        TextArea reasonArea = new TextArea();
        reasonArea.setPromptText("Lý do từ chối (nếu từ chối)");
        reasonArea.setPrefRowCount(3);

        Button approveButton = new Button("Duyệt nạp tiền");
        Button rejectButton = new Button("Từ chối");
        approveButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        rejectButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");

        approveButton.setOnAction(event -> {
            DepositRequestDto selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showNotification("Thông báo", "Vui lòng chọn một yêu cầu nạp tiền.");
                return;
            }
            try {
                apiClient.approveDeposit(selected.getId());
                refreshPendingDepositDialog();
                updateDepositReviewIndicator();
                showNotification("Thành công", "Đã duyệt nạp tiền cho " + selected.getUsername() + ".");
            } catch (Exception e) {
                showNotification("Lỗi", e.getMessage());
            }
        });

        rejectButton.setOnAction(event -> {
            DepositRequestDto selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showNotification("Thông báo", "Vui lòng chọn một yêu cầu nạp tiền.");
                return;
            }
            try {
                apiClient.rejectDeposit(selected.getId(), reasonArea.getText());
                refreshPendingDepositDialog();
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
            pendingDepositDialogTable = null;
            pendingDepositDialogData = null;
            updateDepositReviewIndicator();
        });

        Node closeButton = dialog.getDialogPane().lookupButton(dialog.getDialogPane().getButtonTypes().get(0));
        if (closeButton != null) closeButton.setFocusTraversable(false);
        dialog.showAndWait();
    }

    @FXML
    public void handleEndAuction(ActionEvent event) {
        ServerAuctionItem selectedItem = getSelectedServerItem();
        if (selectedItem == null) return;

        try {
            if ("PENDING_APPROVAL".equalsIgnoreCase(selectedItem.getServerStatus())) {
                apiClient.approveAuction(selectedItem.getAuctionId());
                showNotification("Thành công", "Đã duyệt phiên: " + selectedItem.getName());
            } else {
                apiClient.finishAuction(selectedItem.getAuctionId());
                showNotification("Thành công", "Đã kết thúc phiên: " + selectedItem.getName());
            }
            loadData();
        } catch (Exception e) {
            showNotification("Lỗi", e.getMessage());
        }
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
            showNotification("Lỗi", "Không thể mở màn hình thêm sản phẩm: " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenHistory(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TransactionHistory.fxml"));
            javafx.scene.Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root, 700, 450)); // Kích thước giao diện
            stage.setTitle("Lịch sử giao dịch hệ thống");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showNotification("Lỗi", "Không thể mở màn hình lịch sử giao dịch: " + e.getMessage());
        }
    }
    @FXML
    private void handleOpenNewWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();

            Stage newStage = new Stage();
            newStage.setTitle("Client mới");
            newStage.setScene(new Scene(root));
            newStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showNotification("Lỗi", "Không thể mở cửa sổ mới: " + e.getMessage());
        }
    }

    private void updateChart(Item item) {
        if (priceChart == null || item == null) return;

        new Thread(() -> {
            try {
                List<com.auction.dto.BidDto> allHistory = apiClient.getTransactionHistory();
                Platform.runLater(() -> {
                    priceSeries.getData().clear();

                    List<com.auction.dto.BidDto> itemBids = allHistory.stream()
                            .filter(b -> item.getName().equals(b.getItemName()))
                            .sorted((b1, b2) -> {
                                try {
                                    java.time.LocalDateTime t1 = b1.getTimestamp() != null ? java.time.LocalDateTime.parse(b1.getTimestamp()) : java.time.LocalDateTime.MIN;
                                    java.time.LocalDateTime t2 = b2.getTimestamp() != null ? java.time.LocalDateTime.parse(b2.getTimestamp()) : java.time.LocalDateTime.MIN;
                                    return t1.compareTo(t2);
                                } catch (Exception e) { return 0; }
                            })
                            .toList();

                    if (itemBids.isEmpty()) {
                        priceSeries.getData().add(new XYChart.Data<>("Khởi điểm", item.getCurrentPrice()));
                    } else {
                        int index = 1;
                        for (com.auction.dto.BidDto bid : itemBids) {
                            String timeStr = "Lần " + index++;
                            try {
                                if (bid.getTimestamp() != null) {
                                    java.time.LocalDateTime dt = java.time.LocalDateTime.parse(bid.getTimestamp());
                                    timeStr = dt.format(TIME_FORMATTER);
                                    // Make category unique by appending hidden spaces or index if needed, but let's try direct time first
                                    // XYChart categories must be unique. Let's append newline and index to ensure uniqueness.
                                    timeStr += "\n(" + (index - 1) + ")";
                                }
                            } catch (Exception e) {}
                            priceSeries.getData().add(new XYChart.Data<>(timeStr, bid.getAmount().doubleValue()));
                        }
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    priceSeries.getData().clear();
                    priceSeries.getData().add(new XYChart.Data<>("Hiện tại", item.getCurrentPrice()));
                });
            }
        }).start();
    }

    private void updateDepositReviewIndicator() {
        boolean isAdmin = currentUser != null && currentUser.getRole() == Role.ADMIN;
        int pendingCount = 0;
        if (isAdmin) {
            pendingCount = loadPendingDepositsSafe().size();
        }

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
        refreshPendingDepositDialog();
    }

    private void updateBalanceLabel() {
        if (balanceLabel != null && currentUser != null) {
            try {
                double balance = apiClient.getBalance();
                currentUser.setBalance(balance);
                balanceLabel.setText("Số dư ví: " + MoneyUtil.formatVnd(balance));
            } catch (Exception e) {
                balanceLabel.setText("Số dư ví: không tải được");
            }
        }
    }

    private void handleRealtimeEvent(Response response) {
        Platform.runLater(() -> {
            loadData();
            updateBalanceLabel();
            updateDepositReviewIndicator();
            showUnreadNotificationsForCurrentUser();
            updateChart(getSelectedServerItem());
        });
    }

    private ServerAuctionItem getSelectedServerItem() {
        Item selected = itemTable.getSelectionModel().getSelectedItem();
        if (selected instanceof ServerAuctionItem serverItem) {
            return serverItem;
        }
        return null;
    }

    private List<DepositRequestDto> loadPendingDepositsSafe() {
        try {
            return apiClient.getPendingDepositRequests();
        } catch (Exception e) {
            return List.of();
        }
    }

    private void refreshPendingDepositDialog() {
        if (pendingDepositDialogData != null) {
            pendingDepositDialogData.setAll(loadPendingDepositsSafe());
        }
        if (pendingDepositDialogTable != null) {
            pendingDepositDialogTable.refresh();
        }
    }

    private String formatDateTime(String value) {
        if (value == null || value.isBlank()) return "";
        try {
            return LocalDateTime.parse(value).format(TIME_FORMATTER);
        } catch (Exception e) {
            return value;
        }
    }

    private void showUnreadNotificationsForCurrentUser() {
        if (currentUser == null) {
            return;
        }
        try {
            List<NotificationDto> notifications = apiClient.getUnreadNotifications();
            for (NotificationDto notification : notifications) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(notification.getTitle());
                alert.setHeaderText(notification.getTitle() + formatNotificationTime(notification.getCreatedAt()));
                alert.setContentText(notification.getMessage());
                alert.showAndWait();
                apiClient.markNotificationRead(notification.getId());
            }
        } catch (Exception e) {
            // Không chặn giao diện nếu chỉ lỗi tải thông báo.
            System.out.println("[AuctionListController] Không thể tải thông báo: " + e.getMessage());
        }
    }

    private String formatNotificationTime(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        try {
            return " - " + LocalDateTime.parse(value).format(TIME_FORMATTER);
        } catch (Exception e) {
            return "";
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
