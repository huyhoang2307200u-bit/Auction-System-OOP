package com.auction;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

    public class App extends Application {
        @Override
        public void start(Stage stage) {
            StackPane root = new StackPane(new Label("Auction System"));
            Scene scene = new Scene(root, 700, 300);

            stage.setTitle("Auction System");
            stage.setScene(scene);
            stage.show();
        }

        public static void main(String[] args) {
            launch(args);
        }
    }

