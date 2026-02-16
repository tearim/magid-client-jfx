package com.zarterstein.maqshiv.ui;

import com.zarterstein.maqshiv.*;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class UiElementRenderer {
    private final Region root;
    private final Region underRoot;
    private final Scene scene;
    private final Client client;
    private UiElement uiElementRoot;

    boolean useTextTimeline = false;
    int timeOffset = 0;
    Timeline textTimeline = new Timeline();
    Timeline visualEffectTimeline;
    boolean blockedByTransition = false;
    private final List<Long> blockedMillis = new ArrayList<>();
    private final List<Integer> currentYOffsets = new ArrayList<>();

    private void clearYOffsets() {
        currentYOffsets.clear();
    }

    private void updateYLastOffset(int e) {
        if( !currentYOffsets.isEmpty()) {
            currentYOffsets.removeLast();
        }
        currentYOffsets.add(e);
    }
    private int getCurrentYOffset() {
        return currentYOffsets.stream().mapToInt(Integer::intValue).sum();
    }
    private void resetPane() {
        if ( requestResetPane && ( root != null ) ){
            ((Pane)root).getChildren().clear();
        }

        requestResetPane = false;
    }
    private boolean requestResetPane;
    public void redraw(UiElement rootElement) {
        // to be called when the client gets a new response
        if ( uiElementRoot != null) {
            uiElementRoot.getChildren().clear();
        }
        currentYOffsets.clear();

        uiElementRoot = rootElement;
        textTimeline = new Timeline();
        useTextTimeline = false;
        timeOffset = 0;
        currentYOffsets.add(10);
        requestResetPane = true;
        if ( rootElement instanceof Responses) {
            renderResponses(rootElement);
        }
        if ( rootElement instanceof Menu) {
            resetPane();
            renderMenu(rootElement);
        }
        if ( rootElement instanceof Narration) {
            resetPane();
            renderNarration(rootElement);
        }
    }

    private void renderResponses(UiElement current) {
        for ( UiElement element : current.getChildren()) {

            if ( element instanceof Menu) {
                resetPane();
                renderHelper( () -> renderMenu(element));
            }
            if ( element instanceof Narration) {
                resetPane();
                renderHelper(() -> renderNarration(element));
            }
            if ( element instanceof VisualTransition) {
                if ( !((VisualTransition) element).isDeferringRedraw() ) {
                    resetPane();
                }
                renderHelper( () -> renderVisualTransition((VisualTransition) element));
            }
        }
    }
    private void renderVisualTransition__fade(VisualTransition current) {
        if ( current.isBlocking() ) {
            blockedByTransition = true;
            blockedMillis.add((long) current.getLength());
        }
        //responsePane.setStyle("-fx-background-color");
        FadeTransition transition = new FadeTransition(Duration.millis(current.getLength()), root);

        transition.setFromValue(1);
        transition.setToValue(0);
        transition.setCycleCount(1);
        underRoot.styleProperty().setValue("-fx-background-color:" + current.getTarget());
        transition.play();
    }
    private void renderVisualTransition__unfade(VisualTransition current) {
        if ( current.isBlocking() ) {
            blockedByTransition = true;
            blockedMillis.add((long) current.getLength());
        }
        //responsePane.setStyle("-fx-background-color");
        FadeTransition transition = new FadeTransition(Duration.millis(current.getLength()), root);

        transition.setFromValue(0);
        transition.setToValue(1);
        transition.setCycleCount(1);
        visualEffectTimeline = new Timeline(
                new KeyFrame( Duration.millis(current.getLength()), e ->   underRoot.styleProperty().setValue("-fx-background-color: transparent") )
        );
        visualEffectTimeline.playFromStart();
        transition.play();
    }

    private void renderVisualTransition(VisualTransition current) {
            //TODO: allow extendability
            //      create Renderer objects that would be extendable
            //      maybe as an abstract class that would be extended per UIElement type
            //      visual transition should be also divided into classes that would be
            //      initialized in a factory and would render everything they need.
            //      But meanwhile, we'll suffice with fade.
        switch ( current.getType()) {
            case "fade":
                renderVisualTransition__fade(current);
                break;
            case "unfade":
                renderVisualTransition__unfade(current);
                break;
        }
    }

    private void renderHelper( Runnable action ) {
        if ( blockedByTransition ) {
            new Thread( () -> {

                  final long blockedMillisLong = blockedMillis.stream().mapToLong(Long::longValue).sum();
                try {
                  Thread.sleep(blockedMillisLong);

                   Platform.runLater( () -> {
                       action.run();
                       blockedMillis.removeLast();
                       blockedByTransition = !blockedMillis.isEmpty();
                   });

               } catch (InterruptedException e ) {
                   System.out.println(e.getMessage());
                   Thread.currentThread().interrupt();
               }
            }).start();
        } else {
            action.run();
        }
    }


    private void renderTextWithTimeline(UiElement current, StringProperty textHolder) {
        useTextTimeline = true;
        String what = current.getDisplayText();
        String[] parts = what.split(Client.STOP_PREFIX);
        for ( String part : parts )  {
            int millis = 0;
            if (part.contains("@")) {
                String[] split = part.split("@", 2);
                //if (!DEBUG_NARRATION)
                try {
                    millis = Integer.parseInt(split[0]);
                    timeOffset += millis;
                } catch (Exception e) {
                    //
                }
                textTimeline.getKeyFrames().add(new KeyFrame(
                        Duration.millis(timeOffset),
                        e -> {
                            textHolder.setValue(textHolder.getValue() + split[1]);
                            ((Region)textHolder.getBean()).applyCss();
                            updateYLastOffset((int)Math.round(20 + ((Region)textHolder.getBean()).getHeight()));
                        }
                ));
               // int frameNo = textTimeline.getKeyFrames().size() - 1;
                //textTimeline.getKeyFrames().get(frameNo - 1);

            } else {
                textTimeline.getKeyFrames().add(new KeyFrame(
                        Duration.millis(timeOffset),
                        e -> {
                            textHolder.setValue(textHolder.getValue() + part);
                            ((Region)textHolder.getBean()).applyCss();
                            updateYLastOffset((int)Math.round(20 + ((Region)textHolder.getBean()).getHeight()));
                        }
                ));

            }


        }

        textTimeline.playFromStart();
    }

    private void renderNarration(UiElement current) {
        Label label = new Label();
        handleElementStyleAndClass(current, label);
        ((Pane)root).getChildren().add(label);
        label.relocate(10, getCurrentYOffset());

        String what = current.getDisplayText();
        root.applyCss();
        root.layout();
        if (what.contains(Client.STOP_PREFIX) || useTextTimeline) {
            renderTextWithTimeline(current, label.textProperty());
        } else {
            label.setText( what );
            label.applyCss();
            currentYOffsets.add( (int)(20 + label.getHeight()));
        }


    }
    private void renderMenu(UiElement current) {
        TextArea menuMessage = new TextArea();
        System.out.println("Y is currently: " + getCurrentYOffset() + " elems: " + currentYOffsets.size());
        currentYOffsets.add(30);
        renderMenuText(current, menuMessage,  current.getDisplayText()  + " " + root.widthProperty());
        ((Pane)root).getChildren().add(menuMessage);
        setMenuProperties(menuMessage, current);
        menuMessage.relocate(10,  getCurrentYOffset());
        menuMessage.getStyleClass().add(current.getStyle().getHtmlClass() + "__text-area");
        menuMessage.applyCss();
        handleElementStyleAndClass(current, null);
        root.applyCss();

        int i = 0;
        int optionCount = current.getChildrenCount();
        for ( UiElement cmd : current.getChildren()) {
            Button button = new Button();
            button.setText(cmd.getDisplayText());
            if ( cmd.getDisplayText().equals("Show description again")) {
                button.relocate(0, -500);
                continue;
            }
            handleElementStyleAndClass(cmd, button);
            double width = root.getWidth();
            ((Pane)root).getChildren().add(button);
            button.setOnAction( action -> {
                client.proceedWith(((Command) cmd).getResponseCommand());
            });
            if ( !cmd.getStyle().hasExplicitCssPositioning()) {
                button.relocate((10 + width / optionCount) * i, getCurrentYOffset());
                i++;
                if (i > 2) {
                    i = 0;
                    currentYOffsets.add(40);
                }
            }
        }
    }

    private void renderMenuText(UiElement current, TextArea menuMessage, String what ) {
        if (what.contains(Client.STOP_PREFIX) || useTextTimeline) {
            renderTextWithTimeline(current, menuMessage.textProperty());
        } else {
            menuMessage.setText( what );
        }
    }
    private void setMenuProperties(TextArea menuMessage, UiElement element) {
        menuMessage.getStyleClass().add("magid-menu-textarea");
        if (element != null ) {
            if ( element.getStyle().isCssPropertyPresent("-mg-window-width") )  {
                menuMessage.getScene().getWindow().setWidth(
                        element.getStyle().getCssPropertyDouble("-mg-window-width")
                );
                root.setPrefWidth(element.getStyle().getCssPropertyDouble("-mg-window-width"));
            }
            if ( element.getStyle().isCssPropertyPresent("-mg-window-height") )  {
                menuMessage.getScene().getWindow().setHeight(
                        element.getStyle().getCssPropertyDouble("-mg-window-height")
                );
                root.setPrefHeight(element.getStyle().getCssPropertyDouble("-mg-window-height"));
            }
            if ( element.getStyle().isCssPropertyPresent("-mg-window-resizable")) {
                boolean resizable = Boolean.getBoolean(element.getStyle().getCssProperty("-mg-widnow-resizable"));
                ((Stage)menuMessage.getScene().getWindow()).setResizable(resizable);
            }
        }
        menuMessage.setWrapText(true);
        menuMessage.setEditable(false);
        menuMessage.setPrefWidth(600);
        menuMessage.setPrefHeight(450);
        menuMessage.setPrefRowCount(6);
    }
    private void handleElementStyleAndClass(UiElement element, Control control) {
        if ( control != null) {
            element.setControl(control);
        }
        element.setRegion(root)
                .setClient(client);
        if ( control != null) {
            element.applyControlStyleClasses();
            control.applyCss();
            control.setStyle(element.getStyle().getCss());
        } else {
            element.applyRegionStyleClasses();
        }
        element.processStyles();

    }

    public UiElementRenderer(Region root, Scene scene, Client client) {
        this.root = root;
        this.scene = scene;
        this.client = client;
        underRoot = (Pane)root.getParent();
    }

}
