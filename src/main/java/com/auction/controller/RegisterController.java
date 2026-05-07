package com.auction.controller;

import com.auction.dao.UserDAO;
import com.auction.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;

    private UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("BIDDER", "SELLER");
    }

    @FXML
    private void handleRegister() {
        String user = usernameField.getText();
        String pass = passwordField.getText();
        String role = roleComboBox.getValue();

        // 1. Kiểm tra nhập liệu (Validation)
        if (user.isEmpty() || pass.isEmpty() || role == null) {
            showAlert("Lỗi", "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        // 2. Thực hiện lưu vào Database qua UserDAO
        if (userDAO.register(user, pass, role)) {
            showAlert("Thành công", "Đăng ký tài khoản thành công!");
            // Quay lại màn hình đăng nhập sau khi đăng ký xong
            SceneManager.switchScene("Login.fxml");
        } else {
            showAlert("Thất bại", "Tên đăng nhập đã tồn tại hoặc lỗi hệ thống!");
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