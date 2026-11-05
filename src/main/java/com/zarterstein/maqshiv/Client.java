package com.zarterstein.maqshiv;

import com.zarterstein.magidclientjfx.MagidClientApplication;
import com.zarterstein.maqshiv.advanced.clientvars.ClientVar;
import com.zarterstein.maqshiv.ui.UiElementRenderer;
import com.zarterstein.maqshiv.utils.JsonUtils;
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
import java.util.LinkedHashMap;
import java.util.List;
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
    private boolean onRetry = false;
    private String retryCommand = "";

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
        clearCssFiles();
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

    // What the user typed into the current menu's inputs, by input-name; sent with every command as user-input-<name>.
    private final Map<String, String> userInputs = new LinkedHashMap<>();
    public String getUserInput(String name) {
        return userInputs.getOrDefault(name, "");
    }
    public void setUserInput(String name, String value) {
        userInputs.put(name, value);
    }
    private Map<String, String> userInputParams() {
        Map<String, String> params = new LinkedHashMap<>();
        userInputs.forEach((name, value) -> params.put("user-input-" + name, value));
        return params;
    }

    public void proceedWith(String command) {
        storyToParse = connector.getResponse(command, userInputParams());
        retryCommand = command;
        renderer.redraw(parse());
    }

    public void setCanvas(Pane pane) {
        responsePane = pane;
    }

    public UiElement parse() {
        String errorLine = "";
        root = null;
        if ( logCommunications) {
            System.out.println(storyToParse);
        }
        try {
            try (JsonReader json = Json.createReader(new StringReader(storyToParse))) {
                JsonObject jsonResult = json.readObject();
                if ( jsonResult.containsKey("status") && jsonResult.getString("status").equals("error")) {
                    if ( jsonResult.containsKey("code") && jsonResult.getString("code").equals(MagidServerErrorCodes.SESSION_NOT_FOUND)) {
                        connector.setSessionId("");
                        connector.setFileRequestToken("");
                    }
                }
                if ( jsonResult.containsKey("session-id")) {
                    connector.setSessionId( jsonResult.getString("session-id"));
                    connector.setFileRequestToken( jsonResult.getString("file-request-token"));
                    proceedWith("");
                    return null;
                }
                if ( jsonResult.containsKey("status") && jsonResult.getString("status").equals("error")) {
                    if (jsonResult.containsKey("message")) {
                        errorLine = jsonResult.getString("message");
                        System.out.println(errorLine);
                    }
                }
                if ( jsonResult.containsKey("freshness-key")) {
                    connector.setFreshnessKey(jsonResult.getString("freshness-key"));
                    if ( !onRetry ) {
                        onRetry = true;
                        proceedWith(retryCommand);
                    }
                } else {
                    onRetry = false;
                    root = addElementsToRootFromJson(root, jsonResult, this);
                    routeDetachedElements(root);
                }
            }
        } catch (Exception e ) {
            errorLine = "Unfortunately, the server spoke something, parser couldn't parse: " + storyToParse +". \n Or just Json parser was not happpy: " + e.getMessage() + ". " ;
            e.printStackTrace();
            System.out.println(errorLine);
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
        UiElement placed = placedElementFromJson(jObj, client);
        if (placed != null) {
            return addToRoot(currentRoot, placed);
        }
        for (String key: jObj.keySet()) {
            UiElement element = ResponseNodeType.byLabel(key);
            if (element != null) {
                element.setClient(client);
                element.createFromJson(jObj);
                currentRoot = addToRoot(currentRoot, element);
            }
        }
        return currentRoot;
    }

    private static UiElement addToRoot(UiElement currentRoot, UiElement element) {
        if (currentRoot == null) {
            return element;
        }
        currentRoot.getChildren().add(element);
        return currentRoot;
    }

    // Detached elements and images are recognized by "detached"/"element-type", not by a key of their own ("image" is
    // also a field of other elements). An image is detached when it has a text anchor (it stood inside a text) and stands
    // on its own otherwise. Either way the object yields no other element.
    private static UiElement placedElementFromJson(JsonObject jObj, Client client) {
        String elementType = JsonUtils.getStringIfSet(jObj, "element-type");
        UiElement element;
        if (elementType.equals("image") && JsonUtils.getStringIfSet(jObj, "text-anchor").isEmpty()) {
            element = new ImageElement();
        } else if (!elementType.isEmpty() || jObj.containsKey("detached")) {
            element = new DetachedElement();
        } else {
            return null;
        }
        element.setClient(client);
        element.createFromJson(jObj);
        return element;
    }

    // Hands each detached element of the batch to the narration whose text contains its anchor, otherwise to the menu,
    // as magid-client-core does. A new menu also resets the inputs to the defaults ("input-value") of this batch.
    private void routeDetachedElements(UiElement root) {
        if (root == null) {
            return;
        }
        List<UiElement> batch = root instanceof Responses ? root.getChildren() : List.of(root);
        List<DetachedElement> detached = batch.stream()
                .filter(DetachedElement.class::isInstance).map(DetachedElement.class::cast).toList();
        Menu menu = batch.stream().filter(Menu.class::isInstance).map(Menu.class::cast).findFirst().orElse(null);
        if (menu != null) {
            userInputs.clear();
            for (DetachedElement element : detached) {
                if (element.getElementType().equals("input") && !element.getField("input-name").isEmpty()
                        && element.hasField("input-value")) {
                    userInputs.put(element.getField("input-name"), element.getField("input-value"));
                }
            }
        }
        for (DetachedElement element : detached) {
            UiElement owner = batch.stream()
                    .filter(e -> e instanceof Narration && element.isAnchoredIn(e.getDisplayText()))
                    .findFirst().orElse(menu);
            if (owner == null) {
                System.out.println("[magid] Detached " + element.getElementType() + " has no narration or menu to go to");
            } else {
                owner.getDetachedElements().add(element);
            }
        }
        if (root instanceof Responses) {
            root.getChildren().removeAll(detached);
        }
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
        if ( !cssFileSources.containsKey(fileName)) {
            scene.getStylesheets().add(fileName);
            cssFileSources.put(fileName, readResourceFileAsString(fileName));
            wasCssAdded = true;
        }
    }

    public void clearCssFiles()  {
        cssFileSources.clear();
        scene.getStylesheets().clear();
    }

    public Map<String, String> getCssFileSources() {
        return cssFileSources;
    }



    public String readResourceFileAsString(String fileName) {
        if ( fileName == null || fileName.isEmpty())
            return "";
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

    public UiElementRenderer getRenderer() {
        return renderer;
    }

    public MagidConnector getConnector() {
        return connector;
    }
}
