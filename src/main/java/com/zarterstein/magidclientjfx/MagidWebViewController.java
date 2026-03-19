package com.zarterstein.magidclientjfx;

import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class MagidWebViewController {

    @FXML
    private WebView webView;

    private WebEngine webEngine;

    @FXML
    private Button resetButton;

    @FXML
    private void initialize() {
        webEngine = webView.getEngine();
        //engine.load("https://www.google.com");
    }

    private MagidWebViewClientApplicationTest magidWebViewClientApplication;

    public WebView getWebView() {
        return webView;
    }

    public WebEngine getWebEngine() {
        return webEngine;
    }
    public void init(MagidWebViewClientApplicationTest application) {
        this.magidWebViewClientApplication = application;

        webView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.FAILED) {
                System.out.println("Error: " + webView.getEngine().getLoadWorker().getException());
            }
            if ( newState == Worker.State.SUCCEEDED) {
                System.out.println("Success, showing: " + webView.getEngine().getTitle() + "; " + webView.getEngine().getUserAgent());
            }
        });
    }

}
