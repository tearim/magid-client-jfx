package com.zarterstein.magidclientjfx;

import com.zarterstein.maqshiv.Client;
import com.zarterstein.maqshiv.utils.JsonUtils;
import com.zarterstein.prefs.PrefHelper;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import magid.connector.MagidConnector;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OptionsController {
    private MagidConnector connector;
    private Client client;
    private Stage stage;
    class XmlsData {
        String name = "";
        String description = "";
        String path = "";
        String absolutePath = "";
        boolean onlyPath = false;
        public XmlsData(String name, String description, String path, String absolutePath) {
            this.name = name;
            this.description = description;
            this.path = path;
            this.absolutePath = absolutePath;
        }

        public XmlsData(String pathOnly) {
            this.path = pathOnly;
            this.onlyPath = true;
        }
        @Override
        public String toString() {
            return (name.isEmpty() ? "–untitled–" : name ) + " (" + path + ") ";
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof XmlsData other) {
                if ( onlyPath ) {
                    return other.path.equals(this.path);
                }
                return other.name.equals(name) && other.path.equals(path) && other.absolutePath.equals(absolutePath);
            }
            if ( obj instanceof String ) {
                return  ((String) obj).equals(path);
            }
            return false;
        }
    }

    @FXML
    private TextField magidServerAddress;
    @FXML
    private TextField magidServerPort;
    @FXML
    private ChoiceBox<XmlsData> xmlListChoiceBox;
    @FXML
    private Slider musicSlider;
    @FXML
    private CheckBox ignoreTimelines;
    @FXML
    private CheckBox ignoreTextTimelines;
    @FXML
    private CheckBox ignoreViewPortMaximization;
    @FXML
    private Label currentXmlInfo;


    private MagidMainController  mainController;
    private static final List<XmlsData> xmlsList = new ArrayList<>();

    public static List<XmlsData> getXmlsList() {
        return xmlsList;
    }

    public OptionsController(){}

    public void init(MagidMainController controller, MagidConnector connector, Client client) {
        this.connector = connector;
        this.client = client;
        mainController = controller;

        magidServerAddress.setText(PrefHelper.get("server.address"));
        magidServerAddress.textProperty().addListener((observable, oldValue, newValue) -> {
            currentSettings.put("server.address", newValue);
        });
        musicSlider.setMin(0);
        musicSlider.setMax(1);
        musicSlider.setBlockIncrement(0.01);
        musicSlider.snapToTicksProperty().setValue(true);
        musicSlider.setMajorTickUnit(0.2);
        musicSlider.setValue(PrefHelper.getDouble("music.volume"));
        musicSlider.valueProperty().addListener((ov, oldValue, newValue) -> {
            currentSettings.put("music.volume", newValue);
        });

        xmlListChoiceBox.valueProperty().addListener((ov, oldValue, newValue) -> {
            currentSettings.put("story.xml", newValue);
        });
        ignoreTimelines.setSelected(PrefHelper.getBoolean("narration.ignoretimelines"));
        ignoreTimelines.selectedProperty().addListener((ov, oldValue, newValue) -> {
            currentSettings.put("narration.ignoretimelines", newValue);
            client.getRenderer().setIngoreTimelines(newValue);
        });
        ignoreTextTimelines.setSelected(PrefHelper.getBoolean("narration.ignoretexttimelines"));
        ignoreTextTimelines.selectedProperty().addListener((ov, oldValue, newValue) -> {
            currentSettings.put("narration.ignoretexttimelines", newValue);
            client.getRenderer().setIngoreTexttimelines(newValue);
        });
        ignoreViewPortMaximization.setSelected(PrefHelper.getBoolean("viewport.ignoremaximization"));
        ignoreViewPortMaximization.selectedProperty().addListener((ov, oldValue, newValue) -> {
            currentSettings.put("viewport.ignoremaximization", newValue);
            client.getRenderer().setIgnoreMaximization(newValue);
        });

    }

    private static final Map<String, Object> currentSettings = new HashMap<>();
    private static final Map<String, Object> previousSettings = new HashMap<>();

    public static String getCurrentSetting(String name) {
        if ( currentSettings.containsKey(name) ) {
            return (String) currentSettings.get(name);
        }
        return "";
    }

    public static float getCurrentSettingAsFloat(String name) {
        if ( currentSettings.containsKey(name) ) {
            return (float) currentSettings.get(name);
        }
        return 0;
    }
    public static int getCurrentSettingAsInt(String name) {
        if ( currentSettings.containsKey(name) ) {
            return (int) currentSettings.get(name);
        }
        return 0;
    }
    public static boolean getCurrentSettingAsBoolean(String name) {
        if ( currentSettings.containsKey(name) ) {
            return (boolean) currentSettings.get(name);
        }
        return false;
    }
    public static double getCurrentSettingAsDouble(String name) {
        if ( currentSettings.containsKey(name) ) {
            return (double) currentSettings.get(name);
        }
        return 0;
    }

    public void requestSelectedXml()  {
        XmlsData current = xmlListChoiceBox.getValue();
        setXmlStatusOnLabel(xmlListChoiceBox.getValue().toString());
        String response = connector.requestXml(current.path);
        if ( JsonUtils.getStringIfSet(response, "status").equals("The server was reloaded.") ) {
            mainController.showMessage("The server has been reloaded");
        }
    }
    private static void putSettingsIntoMap(Map<String, Object> map) {
        map.put("server.address", PrefHelper.get("server.address"));
        map.put("music.volume", PrefHelper.getDouble("music.volume"));
        map.put("narration.ignoretimelines", PrefHelper.getBoolean("narration.ignoretimelines"));
        map.put("narration.ignoretexttimelines", PrefHelper.getBoolean("narration.ignoretexttimelines"));
        map.put("viewport.ignoremaximization", PrefHelper.getBoolean("viewport.ignoremaximization"));
        map.put("story.xml", PrefHelper.get("story.xml"));
    }
    public static void initSettings() {
        putSettingsIntoMap(currentSettings);
    }

    public void restoreSettings() {
        currentSettings.clear();
        currentSettings.putAll(previousSettings);
    }

    public void initialize() {
        putSettingsIntoMap(previousSettings);
        initSettings();
    }

    public void connect() {
        connector.setSilentConnectionErrors(true);
        try {
            JsonArray jsonArray = connector.getXmlList();
            connector.setSilentConnectionErrors(false);
            xmlsList.clear();
            for (JsonValue jsonValue : jsonArray) {
                JsonObject object = jsonValue.asJsonObject();

                XmlsData newItem = new XmlsData(JsonUtils.getStringIfSet(object,"name"), JsonUtils.getStringIfSet(object,"description"), JsonUtils.getStringIfSet(object,"path"), JsonUtils.getStringIfSet(object,"absolutePath"));
                xmlListChoiceBox.getItems().add(newItem);
                xmlsList.add(newItem);
                //System.out.println("added xml: " + newItem.toString());
            }
            xmlListChoiceBox.setValue(new XmlsData(PrefHelper.get("story.xml")));
        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
            // Means that the server is misconfigg'ed. That can happen.
        }
        connector.setSilentConnectionErrors(false);
    }

    public void setXmlStatusOnLabel(String text) {
        currentXmlInfo.setText("Currently armed XML: " + text);
    }

    public void savePreferences() {
        PrefHelper.set("server.address", magidServerAddress.getText());
        PrefHelper.setDouble("music.volume", musicSlider.getValue());
        PrefHelper.setBoolean("narration.ignoretimelines", ignoreTimelines.isSelected());
        PrefHelper.setBoolean("narration.ignoretexttimelines", ignoreTextTimelines.isSelected());
        PrefHelper.setBoolean("viewport.ignoremaximization", ignoreViewPortMaximization.isSelected());
        PrefHelper.set("story.xml", xmlListChoiceBox.getValue().path);
        restoreOnExit = false;
        close();
    }

    public Stage show(Scene scene) throws IOException {
        connect();
        setXmlStatusOnLabel(xmlListChoiceBox.getValue().toString());
        stage = new Stage();
        stage.setTitle("Magid Options");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(event -> close());
        return stage;
    }
    private boolean restoreOnExit = true;
    public void close() {
      if ( restoreOnExit ) {
          restoreSettings();
      } else {
          restoreOnExit = true;
      }
      mainController.noMoreShowingOptions();
      stage.close();
    }
}
