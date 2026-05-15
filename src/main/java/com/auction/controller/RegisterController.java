package com.auction.controller;

import com.auction.client.ServerApiClient;
import com.auction.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;

    @FXML
    public void initialize() {
        roleComboBox.getItems().setAll("BIDDER", "SELLER");
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText().trim();
        String role = roleComboBox.getValue();

        if (username.isEmpty() || password.isEmpty() || role == null) {
            showAlert("Lỗi", "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        try {
            ServerApiClient.getInstance().register(username, password, role);
            showAlert("Thành công", "Đăng ký tài khoản thành công! Hãy đăng nhập.");
            SceneManager.switchScene(usernameField, "Login.fxml");
        } catch (Exception e) {
            showAlert("Thất bại", e.getMessage() == null ? "Không thể kết nối server." : e.getMessage());
        }
    }

    @FXML
    private void goToLogin() {
        SceneManager.switchScene(usernameField, "Login.fxml");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
