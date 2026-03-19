package com.zarterstein.magidclientjfx;

import com.zarterstein.maqshiv.Client;
import com.zarterstein.maqshiv.utils.JsonUtils;
import com.zarterstein.prefs.PrefHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;
import magid.connector.MagidConnector;

import javax.json.JsonObject;


public class MagidMainController {
    private MagidConnector connector;
    public String DEFAULT_MAGID_ADDRESS = "http://localhost:8090";
    private String baseUrl4Magid = DEFAULT_MAGID_ADDRESS;
    public static int DEFAULT_MESSAGE_SHOW_TIME = 5000;

    @FXML
    private Client client;

    @FXML
    private Label statusLabel;

    @FXML
    private Pane effectPane;

    @FXML
    private Pane responsePane;
    private boolean showingOptions = false;
    private Stage optionsStage;
    private String serverWorkingXmlPath = "";

    @FXML
    protected void onReloadServerXmlClick() {
        if ( connector == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);

            // 2. Set the window title and content
            alert.setTitle("MagidClient is not connected");
            alert.setHeaderText(null);
            alert.setContentText("Please connect first using 'Establish connection' button.");

            // 3. Show the dialog and wait for the user to click "OK"
            alert.showAndWait();
        } else {
            String rawResponse = connector.getResponse("reload-xml");
            //System.out.println(rawResponse);

            if ( JsonUtils.getStringIfSet(rawResponse, "status").equals("The server was reloaded.") ) {
                showMessage("The server has been reloaded");
            }
        }
    }


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
    public void init(String xmlToLoad, boolean doLoad) {
        if ( xmlToLoad.isEmpty() ) {
            return;
        }
        baseUrl4Magid = getServerUrl();
        if ( connector == null ) {
            connector = new MagidConnector(baseUrl4Magid);
            try {
                connector.setSilentConnectionErrors(true);
                JsonObject status = JsonUtils.fromString(connector.serverStatus());
                connector.setSilentConnectionErrors(false);
                if (status != null) {
                    serverWorkingXmlPath = JsonUtils.getStringIfSet(status, "xmlPath");
                }
                if (doLoad) {
                    connector.requestXml(xmlToLoad);
                }
            } catch (Exception e) {
                showMessage("Could not connect to " + baseUrl4Magid);
            }
            //client = new Client(connector, responsePane);
        }
    }

    protected String getServerUrl() {
        return PrefHelper.get("server.address").trim().isEmpty()
                ?DEFAULT_MAGID_ADDRESS
                : PrefHelper.get("server.address");
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
        baseUrl4Magid = getServerUrl();

        System.out.println("Connecting to Magid server @"+baseUrl4Magid);
        connector = new MagidConnector(baseUrl4Magid);
        client = new Client(connector, responsePane);
        client.getRenderer().setIngoreTexttimelines(OptionsController.getCurrentSettingAsBoolean("narration.ignoretexttimelines"));
        client.getRenderer().setIngoreTimelines(OptionsController.getCurrentSettingAsBoolean("narration.ignoretimelines"));
        client.getRenderer().setIgnoreMaximization(OptionsController.getCurrentSettingAsBoolean("viewport.ignoremaximization"));
        client.enableLogging();
        client.beginStory("");
    }


    private int messageNo = 0;
    public void showMessage (String message ) {
        showMessage(message, DEFAULT_MESSAGE_SHOW_TIME);
    }
    public void showMessage (String message, int forHowLong ) {
        int currMessageNo  = ++messageNo;
        makeMessageAppear(message, 0, currMessageNo );
        new Thread( () -> {
            try {
                Thread.sleep(forHowLong);
                Platform.runLater(() -> dissapearingMessage(message + " ", currMessageNo ));
            }  catch (InterruptedException e ) {
                System.out.println(e.getMessage());
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    private void makeMessageAppear(String message, int chars, int currMessageNo ) {
        if ( currMessageNo < messageNo ) {
            return;
        }
        new Thread( () -> {
            try {
                Thread.sleep(5);
                Platform.runLater(() -> {
                    String current = message.substring(0, chars);
                    statusLabel.setText(current);
                    if ( chars < message.length()  ) {
                        makeMessageAppear(message, chars + 1,  currMessageNo);
                    }
                });
            }  catch (InterruptedException e ) {
                System.out.println(e.getMessage());
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void dissapearingMessage(String message, int currMessageNo) {
        if ( currMessageNo < messageNo ) {
            return;
        }
        new Thread( () -> {
            try {
                Thread.sleep(25);
                String messageToShow = message.substring(0, Math.max(0, message.length() - 1));
                if ( messageToShow.isEmpty() ) {
                    Platform.runLater(() -> {
                        statusLabel.setText("");
                    });
                } else {
                    Platform.runLater(() -> {
                        dissapearingMessage(messageToShow,  currMessageNo);
                        statusLabel.setText(messageToShow);
                    });

                }
            }  catch (InterruptedException e ) {
                System.out.println(e.getMessage());
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    public String getServerWorkingXmlPath() {
        return serverWorkingXmlPath;
    }
}
