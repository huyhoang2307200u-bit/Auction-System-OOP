package com.auction.controller;

import com.auction.model.Item;
import com.auction.model.ItemCategory;
import com.auction.model.ItemFactory;
import com.auction.model.User;
import com.auction.service.AuctionManager;
import com.auction.util.MoneyUtil;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddItemController {
    @FXML private TextField nameField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField priceField;
    @FXML private TextField durationMinutesField;
    @FXML private ComboBox<String> categoryComboBox;

    private User currentUser;

    @FXML
    public void initialize() {
        categoryComboBox.getItems().setAll("ELECTRONICS", "ART", "VEHICLE");
        categoryComboBox.setValue("ELECTRONICS");
        durationMinutesField.setText("10");
    }

    public void initData(User user) {
        this.currentUser = user;
    }

    @FXML
    public void handleSaveItem() {
        try {
            String name = nameField.getText().trim();
            String description = descriptionArea.getText() == null ? "" : descriptionArea.getText().trim();
            String priceText = priceField.getText().trim();
            String durationText = durationMinutesField.getText().trim();

            if (name.isEmpty() || priceText.isEmpty() || durationText.isEmpty()) {
                showError("Vui lòng nhập tên sản phẩm, giá khởi điểm và thời lượng phiên.");
                return;
            }

            BigDecimal price = MoneyUtil.fromDouble(Double.parseDouble(priceText));
            long durationMinutes = Long.parseLong(durationText);
            if (price.compareTo(BigDecimal.ZERO) <= 0 || durationMinutes <= 0) {
                showError("Giá khởi điểm và thời lượng phiên phải lớn hơn 0.");
                return;
            }

            ItemCategory category = ItemCategory.valueOf(categoryComboBox.getValue());
            String sellerId = currentUser == null ? "seller" : currentUser.getId();
            Item newItem = ItemFactory.create(
                    category,
                    sellerId,
                    name,
                    description,
                    price,
                    "Generic",
                    12,
                    "Unknown artist",
                    "Unknown material",
                    "Unknown manufacturer",
                    LocalDateTime.now().getYear()
            );
            newItem.setEndTime(LocalDateTime.now().plusMinutes(durationMinutes));
            newItem.setAuctionActive(true);

            AuctionManager.getInstance().addNewItem(newItem);
            closeWindow();
        } catch (NumberFormatException e) {
            showError("Giá khởi điểm và thời lượng phiên phải là số hợp lệ.");
        } catch (Exception e) {
            showError("Có lỗi xảy ra: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi nhập liệu");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
