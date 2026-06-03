package com.auction.controller;

import com.auction.client.ServerApiClient;
import com.auction.model.User;
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
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showErrorAlert("Thiếu thông tin", "Vui lòng nhập tên đăng nhập và mật khẩu.");
            return;
        }

        try {
            User loggedInUser = ServerApiClient.getInstance().login(username, password);
            SceneManager.switchSceneWithUser(usernameField, "AuctionList.fxml", loggedInUser);
        } catch (Exception e) {
            showErrorAlert("Đăng nhập thất bại", e.getMessage() == null ? "Không thể kết nối server." : e.getMessage());
        }
    }

    @FXML
    public void handleRegister(ActionEvent event) {
        SceneManager.switchScene(usernameField, "Register.fxml");
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
