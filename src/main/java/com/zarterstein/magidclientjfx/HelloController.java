package com.zarterstein.magidclientjfx;

import com.zarterstein.maqshiv.Client;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import magid.connector.MagidConnector;

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
        connector = new MagidConnector("http://localhost:8080");
        client = new Client(connector, responsePane);
        client.enableLogging();
        client.beginStory("");
        effectPane.prefHeightProperty().bind(
                responsePane.heightProperty()
        );
        effectPane.prefWidthProperty().bind(
                responsePane.widthProperty()
        );
    }
}
