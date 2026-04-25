package com.auction.util;

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
}