package com.zarterstein.magidclientjfx;

import com.zarterstein.maqshiv.Client;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Window;
import magid.connector.MagidConnector;

import java.awt.*;

public class HelloController {
    private MagidConnector connector;
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

        connector = new MagidConnector("http://localhost:8090");
        client = new Client(connector, responsePane);
        client.enableLogging();
        client.beginStory("");
    }
}
