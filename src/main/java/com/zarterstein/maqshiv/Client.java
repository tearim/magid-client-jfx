package com.zarterstein.maqshiv;

import com.zarterstein.magidclientjfx.MagidClientApplication;
import com.zarterstein.maqshiv.advanced.clientvars.ClientVar;
import com.zarterstein.maqshiv.ui.UiElementRenderer;
import com.zarterstein.maqshiv.utils.PathToInputStream;
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
    private String responsePaneDefaultClassName;
    private String underRootDefaultClassName;

    private boolean logCommunications = false;
    private Scene scene;
    private UiElementRenderer renderer;
    private final Map<String, String> envVars = new HashMap<>();
    public String getVar(String varName) {
        return envVars.get(varName);
    }
    public void setVar(String varName, String varValue) {
        envVars.put(varName, varValue);
        ClientVar cVar = new ClientVar(varName, varValue);
        cVar.process();
    }
    public boolean hasVar(String varName) {
        return envVars.containsKey(varName);
    }
    public void clearVar(String varName) {
        envVars.remove(varName);
    }

    public boolean isVar(String varName, String test) {
        return hasVar(varName) && test.equals(getVar(varName));
    }

    public Client(MagidConnector connector, Pane pane) {
        this.connector = connector;
        storyToParse = "";
        ResponseNodeType.setClient(this);
        scene = MagidClientApplication.getMainScene();
        responsePane = pane;
        renderer = new UiElementRenderer(responsePane, scene, this);
        ClientVar.setClient(this);
        underRootDefaultClassName = "magid-outer-background";
        responsePaneDefaultClassName = "magid-response-pane";
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

    public Pane getResponsePane() {
        return responsePane;
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

    public String getUnderRootDefaultStyle() {
        return underRootDefaultClassName;
    }

    public Client setUnderRootDefaultStyle(String underRootDefaultClassName) {
        this.underRootDefaultClassName = underRootDefaultClassName;
        return this;
    }

    public String getResponsePaneDefaultStyle() {
        return responsePaneDefaultClassName;
    }

    public Client setResponsePaneDefaultStyle(String responsePaneDefaultClassName) {
        this.responsePaneDefaultClassName = responsePaneDefaultClassName;
        return this;
    }
    private final Map<String, String> cssFileSources = new HashMap<>();

    private boolean wasCssAdded = false;
    public void addCssFile(String fileName) {
        scene.getStylesheets().add(fileName + "?cmd=resolveMagidProtocol");
        cssFileSources.put(fileName, readResourceFileAsString(fileName+ "?cmd=resolveMagidProtocol") );
        wasCssAdded = true;
    }

    public Map<String, String> getCssFileSources() {
        return cssFileSources;
    }



    public String readResourceFileAsString(String fileName) {
        try (InputStream is = PathToInputStream.get(fileName)) {
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
