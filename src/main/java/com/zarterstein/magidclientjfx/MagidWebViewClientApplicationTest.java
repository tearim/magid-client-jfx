package com.zarterstein.magidclientjfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.IOException;

public class MagidWebViewClientApplicationTest extends Application {
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
        MagidWebViewClientApplicationTest.stage = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(MagidWebViewClientApplicationTest.class.getResource("web-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        MagidWebViewController controller = fxmlLoader.getController();

        WebView webView = controller.getWebView();
        WebEngine webEngine = webView.getEngine();

        webEngine.load("http://localhost:8090/");

        controller.init(this);

        stage.setTitle("Magid Client v0.2.015");
        stage.setScene(scene);
        setIcon();
        stage.show();
//        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
//        MagidMainController controller = fxmlLoader.getController();
//        String xml  = PrefHelper.get("story.xml");
//        controller.showMessage("The Magid Client has started ("+xml+")", 2000);
//        controller.init(xml, PrefHelper.getBoolean("startup.arm.xml"));
//        mainScene = scene;
//        stage.setTitle("Magid Client v0.2.015");
//        stage.setScene(scene);
//        setIcon();
//        stage.show();
    }


}
