package com.zarterstein.maqshiv;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import magid.connector.MagidConnector;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;

public class Client {
    private String storyToParse;
    private UIElement root;
    private MagidConnector connector;
    private Pane responsePane;
    public Client(MagidConnector connector) {
        this.connector = connector;
        storyToParse = "";
    }
    public Client(MagidConnector connector, String story) {
        this(connector);
        storyToParse = story;
    }
    private int currentY = 10;
    private void renderResponses(UIElement current) {
        for ( UIElement element : current.getChildren()) {
            if ( element instanceof Menu) {
                renderMenu(element);
            }
            if ( element instanceof Narration) {
                renderNarration(element);
            }
        }
    }
    private void renderNarration(UIElement current) {
        Label label = new Label();
        label.setText(current.getDisplayText());
        responsePane.getChildren().add(label);
        label.relocate(10, currentY);
        currentY += 30;
    }
    private void renderMenu(UIElement current) {
        Label label = new Label();
        label.setText(current.getDisplayText());
        responsePane.getChildren().add(label);
        label.relocate(10, currentY);
        int i = 0;
        int optionCount = current.getChildrenCount();
        currentY += 200;
        for ( UIElement cmd : current.getChildren()) {
            Button button = new Button();
            button.setText(cmd.getDisplayText());
            double width = responsePane.getWidth();
            responsePane.getChildren().add(button);
            button.setStyle(cmd.getStyle().getCss());
            button.setOnAction( action -> {
                proceedWith(((Command) cmd).getResponseCommand());

            });
            button.relocate((10 + width / optionCount) * i, currentY);
            i++;
        }
    }
    public void drawOnPane( UIElement root) {
        responsePane.getChildren().clear();
        currentY = 10;
        if ( root instanceof Responses ) {
            renderResponses(root);
        }
        if ( root instanceof Menu) {
            renderMenu(root);
        }

    }

    public void beginStory(String command) {
        storyToParse = connector.getResponse(command);
        drawOnPane(parse());
    }

    public void proceedWith(String command) {
        storyToParse = connector.getResponse(command);
        drawOnPane(parse());
    }

    public void setCanvas(Pane pane) {
        responsePane = pane;
    }

    public UIElement parse() {
        String additionalResponse = "";
        root = null;
        try {
            try (JsonReader json = Json.createReader(new StringReader(storyToParse))) {
                JsonObject jObj = json.readObject();
                if ( jObj.containsKey("responses")) {
                    UIElement responses = ResponseNodeType.byLabel("responses");
                    ((Responses) responses).createFromJson(jObj);
                    root = responses;
                }
                if ( jObj.containsKey("menu-name") ) {
                    UIElement menu  = ResponseNodeType.byLabel("menu-name");
                    ((Menu) menu).createFromJson(jObj);
                    if ( root == null ) {
                        root = menu;
                    } else {
                        root.getChildren().add(menu);
                    }
                }
                int a = 0;
            }
        } catch (Exception e ) {
            additionalResponse = "Unfortunately, the server spoke gibberish: " + storyToParse +". \n Json parser was not happpy: " + e.getMessage();
        }
        return root;
    }
    //public MenuNodeType

}

/*
Menu().Options();
Responses().getResponse(n).showMenu();
 */