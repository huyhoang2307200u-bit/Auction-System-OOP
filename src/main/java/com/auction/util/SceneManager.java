package com.auction.util;

import com.auction.controller.AuctionListController;
import com.auction.model.User;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import java.io.IOException;

public class SceneManager {

    // Hàm bổ trợ để lấy Stage hiện tại (tránh lặp code)
    private static Stage getCurrentStage() {
        return (Stage) Stage.getWindows().stream()
                .filter(Window::isShowing)
                .findFirst()
                .orElse(null);
    }

    /**
     * Hàm chuyển cảnh đơn giản nhất (Dùng cho Login -> Register)
     */
    public static void switchScene(String fxmlFile) {
        try {
            String path = fxmlFile.startsWith("/") ? fxmlFile : "/fxml/" + fxmlFile;

            System.out.println("Đang chuyển cảnh tới: " + path);
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(path));
            Parent root = loader.load();

            Stage stage = getCurrentStage();
            if (stage != null) {
                stage.getScene().setRoot(root); // Thay đổi root thay vì tạo Scene mới sẽ mượt hơn
                stage.sizeToScene(); // Tự động căn chỉnh kích thước
                stage.centerOnScreen();
                stage.show();
            }
        } catch (IOException e) {
            System.err.println("Lỗi chuyển cảnh: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Hàm chuyển cảnh có truyền User (Dùng khi Đăng nhập thành công)
     */
    public static void switchSceneWithUser(String fxmlFile, User user) {
        try {
            String path = fxmlFile.startsWith("/") ? fxmlFile : "/fxml/" + fxmlFile;
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(path));
            Parent root = loader.load();

            // Truyền dữ liệu vào Controller của màn hình đấu giá
            AuctionListController controller = loader.getController();
            if (controller != null) {
                controller.initData(user);
            }

            Stage stage = getCurrentStage();
            if (stage != null) {
                stage.setScene(new Scene(root));
                stage.setTitle("Hệ thống Đấu giá - Người dùng: " + user.getUsername());
                stage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}