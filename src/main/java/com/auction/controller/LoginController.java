package com.auction.controller;

import com.auction.model.User;
import com.auction.service.AuctionManager;
import com.auction.util.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // 1. Kiểm tra xác thực từ Database thông qua AuctionManager
        User loggedInUser = AuctionManager.getInstance().authenticate(username, password);

        if (loggedInUser != null) {
            // 2. Đăng nhập thành công -> Chuyển sang màn hình đấu giá và truyền dữ liệu User
            // Sử dụng hàm switchSceneWithUser đã tối ưu trong SceneManager
            SceneManager.switchSceneWithUser("AuctionList.fxml", loggedInUser);
        } else {
            showErrorAlert("Đăng nhập thất bại", "Sai tài khoản hoặc mật khẩu!");
        }
    }

    @FXML
    public void handleRegister(ActionEvent event) {
        // Chuyển sang màn hình đăng ký
        // Lưu ý: Chỉ cần truyền "Register.fxml", SceneManager sẽ tự xử lý đường dẫn /fxml/
        SceneManager.switchScene("Register.fxml");
    }

    @FXML
    public void handleForgotPassword(ActionEvent event) {
        // Có thể hiển thị thông báo tạm thời cho tính năng này
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText("Tính năng Quên mật khẩu đang được phát triển!");
        alert.showAndWait();
    }

    // Hàm tiện ích để hiển thị lỗi
    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}