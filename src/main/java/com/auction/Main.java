package com.auction;

import com.auction.client.ServerApiClient;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Hệ thống đấu giá");
        primaryStage.setScene(new Scene(root));
        primaryStage.setOnCloseRequest(event -> closeServerConnection());
        primaryStage.show();
    }

    @Override
    public void stop() {
        closeServerConnection();
    }

    private void closeServerConnection() {
        try {
            ServerApiClient.getInstance().close();
        } catch (Exception ignored) {
            // Không chặn việc đóng giao diện nếu server đã ngắt kết nối.
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
