package com.auction.controller;

import com.auction.model.User;
import com.auction.service.AuthService; // <-- Phải import đúng dòng này
import com.auction.util.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // Dùng AuthService để kiểm tra
        User loggedInUser = AuthService.login(username, password);

        if (loggedInUser != null) {
            SceneManager.switchSceneWithUser("AuctionList.fxml", loggedInUser);
        } else {
            showErrorAlert("Đăng nhập thất bại", "Sai tài khoản hoặc mật khẩu!");
        }
    }

    @FXML
    public void handleRegister(ActionEvent event) {
        SceneManager.switchScene("Register.fxml");
    }

    @FXML
    public void handleForgotPassword(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText("Tính năng Quên mật khẩu đang được phát triển!");
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}