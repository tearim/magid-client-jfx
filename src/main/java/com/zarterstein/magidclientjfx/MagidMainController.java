package com.zarterstein.magidclientjfx;

import com.zarterstein.maqshiv.Client;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;
import magid.connector.MagidConnector;

public class MagidMainController {
    private MagidConnector connector;
    private String baseUrl4Magid = "http://localhost:8090";

    @FXML
    private AnchorPane anchorPane;
    @FXML
    private Client client;

    @FXML
    private Label welcomeText;

    @FXML
    private Pane effectPane;

    @FXML
    private Pane responsePane;

    @FXML
    protected void onReloadServerXmlClick() {
        connector.getResponse("reload-xml");
    }

    private boolean showingOptions = false;
    private Stage optionsStage;
    public void noMoreShowingOptions() {
        showingOptions = false;
        optionsStage = null;
    }
    @FXML
    protected void showOptionsMenu() {
        //OptionsController optionsController = new OptionsController();
        if ( connector == null ) {
            connector = new MagidConnector(baseUrl4Magid);
            client = new Client(connector, responsePane);
        }
        if ( showingOptions && optionsStage != null) {
            optionsStage.requestFocus();
            return;
        }
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MagidClientApplication.class.getResource("options-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            OptionsController instance = fxmlLoader.getController();
            instance.init(this, connector, client);
            optionsStage = instance.show(scene);
            showingOptions = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onEstablishConnectionClick() {

        Window window  = effectPane.getScene().getWindow();
        window.heightProperty().addListener((observable, oldValue, newValue) -> {
            effectPane.setPrefHeight(newValue.doubleValue());
        });
        window.widthProperty().addListener((observable, oldValue, newValue) -> {
            effectPane.setPrefWidth(newValue.doubleValue());
        });

        responsePane.prefHeightProperty().bind(
                effectPane.heightProperty()
        );
        responsePane.prefWidthProperty().bind(
                effectPane.widthProperty()
        );
        OptionsController.initSettings();
        baseUrl4Magid = OptionsController.getCurrentSetting("server.address").trim().isEmpty()
                    ?"http://localhost:8090"
                    : OptionsController.getCurrentSetting("server.address");

        System.out.println("Connecting to Magid server @"+baseUrl4Magid);
        connector = new MagidConnector(baseUrl4Magid);
        client = new Client(connector, responsePane);
        client.getRenderer().setIngoreTexttimelines(OptionsController.getCurrentSettingAsBoolean("narration.ignoretexttimelines"));
        client.getRenderer().setIngoreTimelines(OptionsController.getCurrentSettingAsBoolean("narration.ignoretimelines"));
        client.enableLogging();
        client.beginStory("");
    }
}
