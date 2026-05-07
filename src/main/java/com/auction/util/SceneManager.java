package com.auction.util;

import com.auction.controller.AuctionListController;
import com.auction.model.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class SceneManager {

    /**
     * Hàm dùng chung để chuyển màn hình.
     * @param stage Cửa sổ hiện tại
     * @param fxmlPath Đường dẫn file fxml (ví dụ: "AuctionList.fxml")
     * @param title Tiêu đề cửa sổ
     */
    public static void switchScene(Stage stage, String fxmlPath, String title) {
        try {

            // Lưu ý: Đảm bảo file FXML của bạn nằm trong thư mục /fxml/
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/fxml/" + fxmlPath));
            Parent root = loader.load();

            Scene scene = stage.getScene();
            if (scene == null) {
                scene = new Scene(root);
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
            }

            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Không tìm thấy file FXML tại: /fxml/" + fxmlPath);
        }
    }
    public static void switchSceneWithUser(Stage stage, String fxmlFile, String title, User user) {
        try {
            System.out.println("Đang load file từ: " + SceneManager.class.getResource("/fxml/" + fxmlFile));
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/fxml/" + fxmlFile));
            Parent root = loader.load();

            // 1. Kiểm tra loader có null không
            AuctionListController controller = loader.getController();
            if (controller == null) {
                throw new IOException("Không thể lấy được Controller từ FXML!");
            }

            // 2. Truyền dữ liệu
            controller.initData(user);

            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            // In ra lỗi chi tiết để xem tại sao nó load fail
            e.printStackTrace();
        }
    }
}