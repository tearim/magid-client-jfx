package com.zarterstein.maqshiv;

import com.zarterstein.magidclientjfx.HelloApplication;
import com.zarterstein.maqshiv.ui.UiElementRenderer;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import magid.connector.MagidConnector;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Client {
    public static final String STOP_PREFIX = "DCSTP_";
    private String storyToParse;
    private UiElement root;
    private MagidConnector connector;
    private Pane responsePane;
    private boolean logCommunications = false;
    private Scene scene;
    private UiElementRenderer renderer;

    public Client(MagidConnector connector, Pane pane) {
        this.connector = connector;
        storyToParse = "";
        ResponseNodeType.setClient(this);
        scene = HelloApplication.getMainScene();
        responsePane = pane;
        renderer = new UiElementRenderer(responsePane, scene, this);
    }
    public Client(MagidConnector connector, Pane pane, String story) {
        this(connector, pane);
        storyToParse = story;
    }

    public void setScene(Scene scene ) {
        this.scene = scene;
    }
    public Scene getScene() {
        return scene;
    }
    public void enableLogging() {
        logCommunications = true;
    }
    public void disableLogging() {
        logCommunications = false;
    }

    public void beginStory(String command) {
        proceedWith(command);
    }

    public void proceedWith(String command) {
        storyToParse = connector.getResponse(command);
        renderer.redraw(parse());
    }

    public void setCanvas(Pane pane) {
        responsePane = pane;
    }

    public UiElement parse() {
        String additionalResponse = "";
        root = null;
        if ( logCommunications) {
            System.out.println(storyToParse);
        }
        try {
            try (JsonReader json = Json.createReader(new StringReader(storyToParse))) {
                root = addElementsToRootFromJson(root, json.readObject(), this);
            }
        } catch (Exception e ) {
            additionalResponse = "Unfortunately, the server spoke something, parser couldn't parse: " + storyToParse +". \n Or just Json parser was not happpy: " + e.getMessage() + ". " ;
            e.printStackTrace();
            System.out.println(additionalResponse);
        }
        if ( wasCssAdded) {
            if ( root!=null) {
                root.recalculateCssPropertiesForSelfAndChildren();
            }
            wasCssAdded = false;
        }
        return root;
    }

    public static UiElement addElementsToRootFromJson(UiElement currentRoot, JsonArray jArr, Client client) {
        for ( JsonObject object : jArr.getValuesAs(JsonObject.class)) {
            currentRoot = addElementsToRootFromJson(currentRoot, object, client);
        }
        return currentRoot;
    }
    public static UiElement addElementsToRootFromJson(UiElement currentRoot, JsonObject jObj, Client client ) {
        for (String key: jObj.keySet()) {
            UiElement element = ResponseNodeType.byLabel(key);
            if (element != null) {
                element.setClient(client);
                element.createFromJson(jObj);
                if (currentRoot == null) {
                    currentRoot = element;
                } else {
                    currentRoot.getChildren().add(element);
                }
            }
        }
        return currentRoot;
    }

    private final Map<String, String> cssFileSources = new HashMap<>();

    private boolean wasCssAdded = false;
    public void addCssFile(String fileName) {
        scene.getStylesheets().add(fileName);
        cssFileSources.put(fileName, readResourceFileAsString(fileName) );
        wasCssAdded = true;
    }

    public Map<String, String> getCssFileSources() {
        return cssFileSources;
    }

    private static InputStream getResourceFileAsInputStream(String fileName) {
        ClassLoader classLoader = Client.class.getClassLoader();
        return classLoader.getResourceAsStream(fileName);
    }

    public static String readResourceFileAsString(String fileName) {
        try (InputStream is = getResourceFileAsInputStream(fileName)) {
            if (is == null) {
                throw new IllegalArgumentException("Resource not found: " + fileName);
            }
            return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining(System.lineSeparator()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to read resource file: " + fileName, e);
        }
    }
}
