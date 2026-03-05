package com.zarterstein.magidclientjfx;

import com.zarterstein.maqshiv.Client;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
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

    @FXML
    protected void showOptionsMenu() {
        OptionsController optionsController = new OptionsController();
        if ( connector == null ) {
            connector = new MagidConnector(baseUrl4Magid);
        }
        //optionsController.init();
        try {
            optionsController.show(connector, client);
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
        client.enableLogging();
        client.beginStory("");
    }
}
