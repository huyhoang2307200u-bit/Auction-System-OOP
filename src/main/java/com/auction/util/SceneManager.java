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

    private static Stage getCurrentStage() {
        return (Stage) Stage.getWindows().stream()
                .filter(Window::isShowing)
                .findFirst()
                .orElse(null);
    }

    public static void switchScene(String fxmlFile) {
        try {
            String path = fxmlFile.startsWith("/") ? fxmlFile : "/fxml/" + fxmlFile;
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(path));
            Parent root = loader.load();

            Stage stage = getCurrentStage();
            if (stage != null) {
                stage.getScene().setRoot(root);
                stage.sizeToScene();
                stage.centerOnScreen();
                stage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void switchSceneWithUser(String fxmlFile, User user) {
        try {
            String path = fxmlFile.startsWith("/") ? fxmlFile : "/fxml/" + fxmlFile;
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(path));
            Parent root = loader.load();

            AuctionListController controller = loader.getController();
            if (controller != null) {
                controller.initData(user);
            }

            Stage stage = getCurrentStage();
            if (stage != null) {
                stage.setScene(new Scene(root));
                stage.setTitle("Hệ thống Đấu giá - " + user.getRole().name() + ": " + user.getUsername());
                stage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}