package com.auction.controller;

import com.auction.service.AuctionManager;
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

        // Lấy đối tượng AuctionManager duy nhất (Singleton)
        AuctionManager manager = AuctionManager.getInstance();

        // GỌI HÀM KIỂM TRA (Lưu ý: Bạn cần mở file AuctionManager.java lên
        // xem hàm kiểm tra tài khoản tên là gì, ở đây mình giả định là checkLogin)
        // Nếu chưa có hàm đó, bạn phải sang file AuctionManager.java để thêm vào nhé!
        if (manager.checkLogin(username, password)) {
            System.out.println("Đăng nhập thành công!");
            // Viết tiếp code chuyển sang màn hình AuctionList tại đây
        } else {
            System.out.println("Sai tài khoản hoặc mật khẩu!");
        }
    }
}
