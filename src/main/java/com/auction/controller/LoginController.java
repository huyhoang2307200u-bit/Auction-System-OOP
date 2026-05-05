package com.auction.controller;

import com.auction.model.User;
import com.auction.service.AuctionManager;
import com.auction.util.SceneManager; // Import SceneManager mới tạo
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // 1. Kiểm tra và LẤY ĐỐI TƯỢNG USER từ hệ thống
        User loggedInUser = AuctionManager.getInstance().authenticate(username, password);

        if (loggedInUser != null) {
            // 2. Chuyển màn hình và TRUYỀN USER qua
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Gọi hàm mới (chúng ta sẽ tạo ở bước 2)
            SceneManager.switchSceneWithUser(stage, "AuctionList.fxml", "Danh sách sản phẩm", loggedInUser);

        } else {
            showErrorAlert("Đăng nhập thất bại", "Sai tài khoản hoặc mật khẩu!");
        }
    }

    @FXML
    public void handleRegister(ActionEvent event) {
        // Tương tự, sau này bạn có thể dùng SceneManager.switchScene(...) ở đây
        System.out.println("Chuyển sang màn hình Đăng ký...");
    }

    @FXML
    public void handleForgotPassword(ActionEvent event) {
        System.out.println("Mở màn hình Quên mật khẩu...");
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
