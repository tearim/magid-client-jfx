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
import javafx.scene.layout.Background;
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
    private final MultiStoreUiRelocator uiHouse = new MultiStoreUiRelocator();
    private boolean ingoreTexttimelines = false;
    private boolean ingoreTimelines = false;
    private boolean ingoreViewPortMaximization = false;

    boolean useTextTimeline = false;
    int timeOffset = 0;
    //Timeline textTimeline = new Timeline();
    Timeline visualEffectTimeline;
    boolean blockedByTransition = false;
    private final List<Long> blockedMillis = new ArrayList<>();

    private void resetPane() {
        if ( requestResetPane && ( root != null ) ){
            ((Pane)root).getChildren().clear();
            root.getStyleClass().clear();
            root.getStyleClass().add(client.getResponsePaneDefaultStyle());
        }
        requestResetPane = false;
    }
    private boolean requestResetPane;
    public void redraw(UiElement rootElement) {
        // to be called when the client gets a new response
        currentFrame++;
        if ( uiElementRoot != null) {
            uiElementRoot.getChildren().clear();
            underRoot.getStyleClass().clear();
            underRoot.getStyleClass().add(uiElementRoot.getStyle().getDefaultHtmlClass());
        }
        uiHouse.vacate();
        uiElementRoot = rootElement;
        //textTimeline = new Timeline();
        useTextTimeline = false;
        timeOffset = 0;

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
            renderHelper(() -> renderNarration(rootElement), rootElement.getDefer());
        }
    }

    private void renderResponses(UiElement current) {
        for ( UiElement element : current.getChildren()) {

            if ( element instanceof Menu) {
                resetPane();
                renderHelper( () -> renderMenu(element), element.getDefer());
            }
            if ( element instanceof Narration) {
                resetPane();
                renderHelper(() -> renderNarration(element), element.getDefer());
            }
            if ( element instanceof VisualTransition) {
                if ( !((VisualTransition) element).isDeferringRedraw() ) {
                    resetPane();
                }
                renderHelper( () -> renderVisualTransition((VisualTransition) element), element.getDefer());
            }
        }
    }
    private Background lastBgBeforeFade ;
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
        lastBgBeforeFade = underRoot.getBackground();
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
                new KeyFrame( Duration.millis(current.getLength()), e ->   underRoot.setBackground(lastBgBeforeFade) )
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
    private int currentFrame = 0;


    private void renderHelper( Runnable action, long defer ) {
        if ( blockedByTransition || defer > 0) {
            final int callFrame =  currentFrame;
            new Thread( () -> {
                final long blockedMillisLong = blockedMillis.stream().mapToLong(Long::longValue).sum() + defer;
                try {
                    if ( ingoreTimelines) {
                        Thread.sleep(1 );
                    }else  {
                        Thread.sleep(blockedMillisLong);
                    }
                    Platform.runLater( () -> {
                       if ( callFrame != currentFrame ) {
                           return;
                       }
                       action.run();
                       if ( !blockedMillis.isEmpty() ) {
                           blockedMillis.removeLast();
                       }
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
        Timeline textTimeline = new Timeline();
        for ( String part : parts )  {
            int millis = 0;
            if (part.contains("@")) {
                String[] split = part.split("@", 2);
                //if (!DEBUG_NARRATION)
                try {
                    if  (ingoreTexttimelines) {
                        millis = 1;
                    } else {
                        millis = Integer.parseInt(split[0]);
                    }
                    timeOffset += millis;
                } catch (Exception e) {
                    //
                }
                textTimeline.getKeyFrames().add(new KeyFrame(
                        Duration.millis(timeOffset),
                        e -> {
                            textHolder.setValue(textHolder.getValue() + split[1]);
                            ((Region)textHolder.getBean()).applyCss();
                        }
                ));


            } else {
                textTimeline.getKeyFrames().add(new KeyFrame(
                        Duration.millis(timeOffset),
                        e -> {
                            textHolder.setValue(textHolder.getValue() + part);
                            ((Region)textHolder.getBean()).applyCss();
                        }
                ));

            }
            if ( !current.getStyle().hasExplicitCssPositioning()) {
                textTimeline.getKeyFrames().add(new KeyFrame(
                        Duration.millis(timeOffset + 1),
                        e -> {
                            uiHouse.redrawAll();
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

        if ( !current.getStyle().hasExplicitCssPositioning()) {
            label.relocate(10, uiHouse.currentY());
            uiHouse.addFloor(label);
        }

        String what = current.getDisplayText();
        root.applyCss();
        root.layout();
        if (what.contains(Client.STOP_PREFIX) || useTextTimeline) {
            renderTextWithTimeline(current, label.textProperty());
        } else {
            label.setText( what );
            label.applyCss();
            uiHouse.redrawAll();
        }


    }
    private void renderMenu(UiElement current) {
        TextArea menuMessage = new TextArea();
        renderMenuText(current, menuMessage,  current.getDisplayText());
        ((Pane)root).getChildren().add(menuMessage);
        setMenuProperties(menuMessage, current);
        menuMessage.relocate(uiHouse.getXOffset(),  uiHouse.currentY());
        uiHouse.addFloor(menuMessage);
        String firstClassName = "";
        if (current.getStyle().getHtmlClass() != null  ) {
            firstClassName  = current.getStyle().getHtmlClass().trim().contains(" ")
                        ? current.getStyle().getHtmlClass().trim().split(" ")[0]
                        : current.getStyle().getHtmlClass();
        }
        menuMessage.getStyleClass().add( firstClassName + "__text-area");

        ((Pane)root.getParent()).getStyleClass().clear();
        ((Pane)root.getParent()).getStyleClass().add(firstClassName + "__outer-pane");

        menuMessage.applyCss();
        handleElementStyleAndClass(current, null);
        root.applyCss();
        if ( ((Menu)current).getPlayer() != null ) {
            ((Menu)current).getPlayer().play();
        }

        int autoPlacedIteratorCount = 0;
        int allButtonIteratorCount = 0;
        int optionCount = current.getChildrenCount();
        boolean addAStore = true;
        int buttonCount = current.getChildren().size();
        for ( UiElement cmd : current.getChildren()) {
            Button button = new Button();
            button.setText(cmd.getDisplayText());
            cmd.setControl(button);
            ((Command)cmd).setDefaultHtmlPositioningClassName(buttonCount, (allButtonIteratorCount+1));
            handleElementStyleAndClass(cmd, button);
            double width = root.getWidth();
            renderHelper( () -> ((Pane) root).getChildren().add(button), cmd.getDefer());
            button.setOnAction( action -> {
                client.proceedWith(((Command) cmd).getResponseCommand());
            });
            allButtonIteratorCount++;
            if ( !cmd.getStyle().hasExplicitCssPositioning()) {
                if ( addAStore ) {
                    uiHouse.addFloor(button);
                    addAStore = false;
                } else {
                    uiHouse.addToLastFloor(button);
                }
                button.relocate((10 + width / optionCount) * autoPlacedIteratorCount, uiHouse.currentY());
                autoPlacedIteratorCount++;
                if (autoPlacedIteratorCount > 2) {
                    autoPlacedIteratorCount = 0;
                    addAStore = true;
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
        menuMessage.getStyleClass().add("textarea.magid-menu-textarea");
        if (element != null ) {

            if ( !client.isVar("view-port", "maximized") && element.getStyle().isCssPropertyPresent("-mg-window-width") )  {
                menuMessage.getScene().getWindow().setWidth(
                        element.getStyle().getCssPropertyDouble("-mg-window-width")
                );
                root.setPrefWidth(element.getStyle().getCssPropertyDouble("-mg-window-width"));
            }
            if ( !client.isVar("view-port", "maximized") &&  element.getStyle().isCssPropertyPresent("-mg-window-height") )  {
                menuMessage.getScene().getWindow().setHeight(
                        element.getStyle().getCssPropertyDouble("-mg-window-height")
                );
                root.setPrefHeight(element.getStyle().getCssPropertyDouble("-mg-window-height"));
            }
            if ( !client.isVar("view-port", "maximized") && element.getStyle().isCssPropertyPresent("-mg-window-resizable")) {
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
        element.recalculateCssPropertiesForSelfAndChildren();
        element.processStyles();


    }

    public void setIngoreTimelines(boolean really) {
        ingoreTimelines =  really;
    }
    public void setIngoreTexttimelines(boolean really) {
        ingoreTexttimelines = really;
    }

    public void setIgnoreMaximization(boolean really) {
        ingoreViewPortMaximization = really;
    }
    public boolean getIgnoreMaximization() {
        return ingoreViewPortMaximization;
    }

    public UiElementRenderer(Region root, Scene scene, Client client) {
        this.root = root;
        this.scene = scene;
        this.client = client;
        underRoot = (Pane)root.getParent();
    }

}
