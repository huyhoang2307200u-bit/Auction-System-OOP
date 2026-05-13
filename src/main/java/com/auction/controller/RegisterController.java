package com.auction.controller;

import com.auction.model.Bidder;
import com.auction.model.Seller;
import com.auction.model.User;
import com.auction.service.AuthService;
import com.auction.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("BIDDER", "SELLER");
    }

    @FXML
    private void handleRegister() {
        String user = usernameField.getText().trim();
        String pass = passwordField.getText().trim();
        String role = roleComboBox.getValue();

        if (user.isEmpty() || pass.isEmpty() || role == null) {
            showAlert("Lỗi", "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        // Khởi tạo User theo vai trò. Tạm cấp ID bằng Random, sau này Server tự cấp.
        int tempId = (int)(Math.random() * 1000);
        User newUser = role.equals("SELLER")
                ? new Seller(tempId, user, user, pass, "SELLER")
                : new Bidder(tempId, user, user, pass, "BIDDER");

        if (AuthService.register(newUser)) {
            showAlert("Thành công", "Đăng ký tài khoản thành công!");
            SceneManager.switchScene("Login.fxml");
        } else {
            showAlert("Thất bại", "Lỗi đăng ký hệ thống!");
        }
    }

    @FXML
    private void goToLogin() {
        SceneManager.switchScene("Login.fxml");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}