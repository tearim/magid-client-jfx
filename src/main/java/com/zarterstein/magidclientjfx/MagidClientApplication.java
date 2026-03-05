package com.zarterstein.magidclientjfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MagidClientApplication extends Application {
    private static Scene mainScene;
    private static Stage stage;
    public  static Scene getMainScene() {
        return mainScene;
    }
    public  static Stage getStage() {
        return stage;
    }
    @Override
    public void start(Stage stage) throws IOException {
        MagidClientApplication.stage = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(MagidClientApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        mainScene = scene;
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }
}
