package com.zarterstein.magidclientjfx;

import com.zarterstein.maqshiv.Client;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import magid.connector.MagidConnector;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    private Pane responsePane;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @FXML
    protected void onEstablishConnectionClick() {
        MagidConnector connector = new MagidConnector("http://localhost:8080");
        Client client = new Client(connector);
        client.setCanvas(responsePane);
        client.beginStory("");


        //welcomeText.setText( response);
    }
}
