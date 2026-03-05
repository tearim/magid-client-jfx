package com.zarterstein.magidclientjfx;

import com.zarterstein.maqshiv.Client;
import com.zarterstein.maqshiv.utils.JsonUtils;
import com.zarterstein.prefs.PrefHelper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
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
            if (obj instanceof XmlsData) {
                XmlsData other = (XmlsData) obj;
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

    private static List<XmlsData> xmlsList = new ArrayList<>();

    public static List<XmlsData> getXmlsList() {
        return xmlsList;
    }

    public OptionsController(){}

    public void init(MagidConnector connector, Client client) {
        this.connector = connector;
        this.client = client;
    }

    private static final Map<String, Object> currentSettings = new HashMap<>();
    private static final Map<String, Object> previousSettings = new HashMap<>();

    public static String getCurrentSetting(String name) {
        if ( currentSettings.keySet().contains(name) ) {
            return (String) currentSettings.get(name);
        }
        return "";
    }

    public static float  getCurrentSettingAsFloat(String name) {
        if ( currentSettings.keySet().contains(name) ) {
            return (float) currentSettings.get(name);
        }
        return 0;
    }
    public static int getCurrentSettingAsInt(String name) {
        if ( currentSettings.keySet().contains(name) ) {
            return (int) currentSettings.get(name);
        }
        return 0;
    }
    public static boolean getCurrentSettingAsBoolean(String name) {
        if ( currentSettings.keySet().contains(name) ) {
            return (boolean) currentSettings.get(name);
        }
        return false;
    }
    public static double getCurrentSettingAsDouble(String name) {
        if ( currentSettings.keySet().contains(name) ) {
            return (double) currentSettings.get(name);
        }
        return 0;
    }

    public void requestXml()  {
        XmlsData current = xmlListChoiceBox.getValue();
        String request = "set-xml&path=" + current.path;
        String response = connector.getResponse(request);
        System.out.println("Response: " + response);
    }

    public static void initSettings() {
        currentSettings.put("server.address", PrefHelper.get("server.address"));
        currentSettings.put("music.volume", PrefHelper.getDouble("music.volume"));
        currentSettings.put("narration.ignoretimelines", PrefHelper.getBoolean("narration.ignoretimelines"));
        currentSettings.put("narration.ignoretexttimelines", PrefHelper.getBoolean("narration.ignoretexttimelines"));
        currentSettings.put("story.xml", PrefHelper.get("story.xml"));

    }

    public void restoreSettings() {
        currentSettings.clear();
        currentSettings.putAll(previousSettings);
    }

    public void initialize() {
        previousSettings.put("server.address", PrefHelper.get("server.address"));
        previousSettings.put("music.volume", PrefHelper.getDouble("music.volume"));
        previousSettings.put("narration.ignoretimelines", PrefHelper.getBoolean("narration.ignoretimelines"));
        previousSettings.put("narration.ignoretexttimelines", PrefHelper.getBoolean("narration.ignoretexttimelines"));
        previousSettings.put("story.xml", PrefHelper.get("story.xml"));

        initSettings();

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
        });
        ignoreTextTimelines.setSelected(PrefHelper.getBoolean("narration.ignoretexttimelines"));
        ignoreTextTimelines.selectedProperty().addListener((ov, oldValue, newValue) -> {
            currentSettings.put("narration.ignoretexttimelines", newValue);
        });
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

    public void savePreferences() {
        PrefHelper.set("server.address", magidServerAddress.getText());
        PrefHelper.setDouble("music.volume", musicSlider.getValue());
        PrefHelper.setBoolean("narration.ignoretimelines", ignoreTimelines.isSelected());
        PrefHelper.setBoolean("narration.ignoretexttimelines", ignoreTextTimelines.isSelected());
        PrefHelper.set("story.xml", xmlListChoiceBox.getValue().path);
        previousSettings.clear();
        initSettings();
        previousSettings.putAll(currentSettings);
    }

    public void show(MagidConnector connector, Client client) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MagidClientApplication.class.getResource("options-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        OptionsController instance = fxmlLoader.getController();
        instance.init(connector, client);
        stage = new Stage();
        instance.connect();
        stage.setTitle("Magid Options");
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(event -> instance.close());
    }

    public void close() {
        restoreSettings();
    }
}
