package com.auction.controller;

import com.auction.client.ServerApiClient;
import com.auction.model.User;
import com.auction.util.MoneyUtil;
import java.math.BigDecimal;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

public class AddItemController {
    @FXML private TextField nameField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField priceField;
    @FXML private TextField durationMinutesField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private Label imageNameLabel;
    @FXML private ImageView previewImageView;

    private User currentUser;
    private String selectedImageDataUrl = "";

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
    public void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh sản phẩm");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        Stage stage = (Stage) nameField.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                byte[] fileContent = Files.readAllBytes(file.toPath());
                String base64Str = Base64.getEncoder().encodeToString(fileContent);
                String extension = "";
                String fileName = file.getName();
                int i = fileName.lastIndexOf('.');
                if (i > 0) {
                    extension = fileName.substring(i + 1).toLowerCase();
                }
                selectedImageDataUrl = "data:image/" + extension + ";base64," + base64Str;
                
                imageNameLabel.setText(file.getName());
                Image image = new Image(file.toURI().toString());
                previewImageView.setImage(image);
            } catch (IOException e) {
                showError("Không thể đọc file ảnh: " + e.getMessage());
            }
        }
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
                    durationMinutes,
                    selectedImageDataUrl
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
