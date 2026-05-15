package com.auction.controller;

import com.auction.client.ServerApiClient;
import com.auction.model.User;
import com.auction.util.MoneyUtil;
import java.math.BigDecimal;
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
        priceField.setPromptText("VD: 1.000.000VND");
    }

    public void initData(User user) {
        this.currentUser = user;
    }

    @FXML
    public void handleSaveItem() {
        try {
            String name = nameField.getText() == null ? "" : nameField.getText().trim();
            String description = descriptionArea.getText() == null ? "" : descriptionArea.getText().trim();
            String priceText = priceField.getText() == null ? "" : priceField.getText().trim();
            String durationText = durationMinutesField.getText() == null ? "" : durationMinutesField.getText().trim();

            if (currentUser == null) {
                showError("Bạn cần đăng nhập trước khi tạo sản phẩm.");
                return;
            }
            if (name.isEmpty() || priceText.isEmpty() || durationText.isEmpty()) {
                showError("Vui lòng nhập tên sản phẩm, giá khởi điểm và thời lượng phiên.");
                return;
            }

            BigDecimal price = MoneyUtil.parseUserAmount(priceText);
            int durationMinutes = Integer.parseInt(durationText);
            if (price.compareTo(BigDecimal.ZERO) <= 0 || durationMinutes <= 0) {
                showError("Giá khởi điểm và thời lượng phiên phải lớn hơn 0.");
                return;
            }

            ServerApiClient.getInstance().createAuction(
                    name,
                    description,
                    categoryComboBox.getValue(),
                    price.doubleValue(),
                    durationMinutes
            );
            closeWindow();
        } catch (NumberFormatException e) {
            showError("Giá khởi điểm phải là số tiền hợp lệ, ví dụ: 1.000.000VND. Thời lượng phiên phải là số phút hợp lệ.");
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
