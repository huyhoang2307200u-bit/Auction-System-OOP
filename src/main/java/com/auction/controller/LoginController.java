package com.auction.controller;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    // Các tên biến này khớp với fx:id đã đặt trong Scene Builder
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    // Hàm này sẽ chạy khi nhấn nút Đăng nhập
    @FXML
    public void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        System.out.println("Đang đăng nhập với tài khoản: " + username);
    }
}
