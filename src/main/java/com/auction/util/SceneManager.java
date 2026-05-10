package com.auction.util;

import com.auction.controller.AuctionListController;
import com.auction.model.User;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import java.io.IOException;

public class SceneManager {

    /**
     * Hàm chuyển cảnh đơn giản (Dùng cho Login -> Register)
     * Đã sửa để lấy Stage trực tiếp từ sự kiện nếu cần,
     * nhưng ở đây ta vẫn giữ logic lấy stage hiện tại để bạn dễ dùng.
     */
    public static void switchScene(String fxmlFile) {
        try {
            String path = fxmlFile.startsWith("/") ? fxmlFile : "/fxml/" + fxmlFile;
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(path));
            Parent root = loader.load();

            // Tìm stage đang hoạt động
            Stage stage = (Stage) Stage.getWindows().filtered(w -> w.isShowing()).get(0);
            if (stage != null) {
                stage.setScene(new Scene(root)); // Tạo Scene mới để đảm bảo reset hoàn toàn các luồng
                stage.centerOnScreen();
                stage.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Hàm chuyển cảnh có truyền User (Dùng khi Đăng nhập thành công)
     * QUAN TRỌNG: Đảm bảo Controller được kích hoạt và Observer được đăng ký.
     */
    public static void switchSceneWithUser(String fxmlFile, User user) {
        try {
            String path = fxmlFile.startsWith("/") ? fxmlFile : "/fxml/" + fxmlFile;
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(path));
            Parent root = loader.load();

            // 1. Lấy đúng Controller mà FXML vừa tạo ra
            Object controller = loader.getController();

            // 2. Ép kiểu và truyền dữ liệu
            if (controller instanceof AuctionListController) {
                AuctionListController auctionController = (AuctionListController) controller;
                auctionController.initData(user);
                // Lưu ý: Hàm initialize() trong AuctionListController sẽ tự chạy khi loader.load()
                // nên việc đăng ký Observer đã được thực hiện tại đó.
            }

            // 3. Hiển thị lên Stage hiện tại của cửa sổ đăng nhập đó
            Stage stage = (Stage) Stage.getWindows().filtered(w -> w.isShowing()).stream()
                    .filter(w -> ((Stage)w).getTitle().contains("Đăng nhập") || w.getScene().getRoot().toString().contains("AnchorPane"))
                    .findFirst().orElse((Stage) Stage.getWindows().get(0));

            if (stage != null) {
                stage.setScene(new Scene(root));
                stage.setTitle("Hệ thống Đấu giá - Người dùng: " + user.getName());
                stage.centerOnScreen();
                stage.show();
            }
        } catch (IOException e) {
            System.err.println("Lỗi tại SceneManager (switchWithUser): " + e.getMessage());
            e.printStackTrace();
        }
    }
}