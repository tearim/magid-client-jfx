package com.zarterstein.magidclientjfx;

import com.zarterstein.prefs.PrefHelper;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
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
    private void setIcon()  {
        Image appIcon = new Image(getClass().getResourceAsStream("images/icon.png"));
        // Add the icon to the stage
        stage.getIcons().add(appIcon);

    }
    @Override
    public void start(Stage stage) throws IOException {
        MagidClientApplication.stage = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(MagidClientApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        MagidMainController controller = fxmlLoader.getController();
        String xml  = PrefHelper.get("story.xml");
        controller.showMessage("The Magid Client has started ("+xml+")", 2000);
        controller.init(xml, PrefHelper.getBoolean("startup.arm.xml"));
        mainScene = scene;
        stage.setTitle("Magid Client v0.2.015");
        stage.setScene(scene);
        setIcon();
        stage.show();
    }


}
