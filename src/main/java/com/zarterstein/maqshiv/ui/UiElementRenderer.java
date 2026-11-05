package com.zarterstein.maqshiv.ui;

import com.zarterstein.maqshiv.*;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Labeled;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UiElementRenderer {
    private final Region root;
    private final Region underRoot;
    private final Scene scene;
    private final Client client;
    private UiElement uiElementRoot;
    private final MultiStoreUiRelocator uiHouse = new MultiStoreUiRelocator();
    private boolean ignoreTexttimelines = false;
    private boolean ignoreTimelines = false;
    private boolean ignoreViewPortMaximization = false;

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
            root.applyCss();
        }
        requestResetPane = false;
    }
    private boolean requestResetPane;
    public void redraw(UiElement rootElement) {
        removeListenersOnRedraw();
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
            if ( element instanceof ImageElement) {
                resetPane();
                renderHelper(() -> renderImage((ImageElement) element), element.getDefer());
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
                    if (ignoreTimelines) {
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


    private Timeline renderTextWithTimeline(UiElement current, StringProperty textHolder) {
        return renderTextWithTimeline(current, current.getDisplayText(), textHolder);
    }

    private Timeline renderTextWithTimeline(UiElement current, String what, StringProperty textHolder) {
        useTextTimeline = true;
        String[] parts = what.split(Client.STOP_PREFIX);
        Timeline textTimeline = new Timeline();
        for ( String part : parts )  {
            int millis = 0;
            if (part.contains("@")) {
                String[] split = part.split("@", 2);
                //if (!DEBUG_NARRATION)
                try {
                    if  (ignoreTexttimelines) {
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
        return textTimeline;
    }

    // Text with the detached elements placed at their anchors, shown by `holder`, which keeps the element's classes
    // and css (so the story's CSS still applies) and lends its font and colour to the text.
    private AnchoredText anchoredText(UiElement current, Labeled holder, List<DetachedElement> anchored) {
        AnchoredText text = new AnchoredText(current.getDisplayText(), anchored, el -> DetachedNodes.create(el, client));
        text.followTextStyleOf(holder);
        holder.setGraphic(text.getFlow());
        holder.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        holder.setAlignment(Pos.TOP_LEFT);
        // Wrap within the holder's width when the story's CSS sets one (a narration Label doesn't wrap otherwise).
        text.getFlow().maxWidthProperty().bind(Bindings.createDoubleBinding(
                () -> holder.getPrefWidth() > 0
                        ? holder.getPrefWidth() - holder.getInsets().getLeft() - holder.getInsets().getRight()
                        : Double.MAX_VALUE,
                holder.prefWidthProperty(), holder.insetsProperty()));
        return text;
    }

    // A standalone image (<img> outside a text), in its own row. During typing it waits for the text before it.
    private void renderImage(ImageElement current) {
        Node image = DetachedNodes.image(current::getField, current.getStyle(), client);
        if (image == null) {
            return;
        }
        HBox box = new HBox(image);
        box.getStyleClass().add("magid-default-image");
        ((Pane) root).getChildren().add(box);
        box.relocate(10, uiHouse.currentY());
        uiHouse.addFloor(box);
        showAfterTyping(box);
        if (image instanceof ImageView view) {
            view.getImage().progressProperty().addListener((o, before, now) -> {
                if (now.doubleValue() >= 1) {
                    uiHouse.redrawAll();
                }
            });
        }
    }

    // A detached element before or after the menu description, in its own row.
    private Region renderDetachedRow(DetachedElement element) {
        Node node = DetachedNodes.create(element, client);
        if (node == null) {
            return null;
        }
        HBox row = new HBox(node);
        row.getStyleClass().add("magid-detached-row");
        ((Pane) root).getChildren().add(row);
        row.relocate(uiHouse.getXOffset(), uiHouse.currentY());
        uiHouse.addFloor(row);
        return row;
    }

    private void showAfterTyping(Node node) {
        if (!useTextTimeline || ignoreTexttimelines) {
            return;
        }
        node.setVisible(false);
        new Timeline(new KeyFrame(Duration.millis(timeOffset + 1), e -> node.setVisible(true))).playFromStart();
    }

    private void renderNarration(UiElement current) {
        Label label = new Label();
        AnchoredText anchored = current.getDetachedElements().isEmpty()
                ? null
                : anchoredText(current, label, current.getDetachedElements());
        handleElementStyleAndClass(current, label);
        ((Pane)root).getChildren().add(label);

        if ( !current.getStyle().hasExplicitCssPositioning()) {
            label.relocate(10, uiHouse.currentY());
            uiHouse.addFloor(label);
        }

        String what = anchored == null ? current.getDisplayText() : anchored.getMarkedText();
        StringProperty textHolder = anchored == null ? label.textProperty() : anchored.textProperty();
        root.applyCss();
        root.layout();
        label.getStyleClass().add(current.getStyle().getDefaultHtmlClass());
        label.getStyleClass().add(current.getStyle().getHtmlClass());
        label.applyCss();
        if (what.contains(Client.STOP_PREFIX) || useTextTimeline) {
            renderTextWithTimeline(current, what, textHolder);
        } else {
            textHolder.setValue( what );
            uiHouse.redrawAll();
        }


    }
    private void renderMenu(UiElement current) {
        if ( root == null) {
            throw  new RuntimeException("Root Element for menu not found");
        }
        List<DetachedElement> detached = current.getDetachedElements();
        List<DetachedElement> anchored = detached.stream()
                .filter(d -> d.getMode().equals(DetachedElement.BY_ANCHOR) && d.isAnchoredIn(current.getDisplayText()))
                .toList();
        // A by-anchor element whose anchor isn't in the description goes after it, like an appended one.
        List<DetachedElement> appended = detached.stream()
                .filter(d -> !d.getMode().equals(DetachedElement.PREPEND) && !anchored.contains(d))
                .toList();
        detached.stream().filter(d -> d.getMode().equals(DetachedElement.PREPEND)).forEach(this::renderDetachedRow);

        Region menuMessage;
        Timeline typing;
        if ( anchored.isEmpty() ) {
            TextArea textArea = new TextArea();
            typing = renderMenuText(current, textArea.textProperty(), current.getDisplayText());
            menuMessage = textArea;
        } else {
            // A TextArea can't hold the elements, so the description becomes a Label with the TextArea's classes
            // ("text-area" included): the story's .magid-menu-textarea.text-area rules still apply.
            Label holder = new Label();
            holder.getStyleClass().add("text-area");
            holder.setPrefWidth(600);
            AnchoredText text = anchoredText(current, holder, anchored);
            typing = renderMenuText(current, text.textProperty(), text.getMarkedText());
            menuMessage = holder;
        }
        ((Pane)root).getChildren().add(menuMessage);
        setMenuProperties(menuMessage, current);
        menuMessage.relocate(uiHouse.getXOffset(),  uiHouse.currentY());
        uiHouse.addFloor(menuMessage);
        List<Region> appendedRows = appended.stream().map(this::renderDetachedRow).filter(Objects::nonNull).toList();
        if ( typing != null && !appendedRows.isEmpty() ) {
            // Appended elements appear once the description has been typed out.
            appendedRows.forEach(row -> row.setVisible(false));
            typing.setOnFinished(e -> appendedRows.forEach(row -> row.setVisible(true)));
        }
        String firstClassName = "";
        if (current.getStyle().getHtmlClass() != null  ) {
            firstClassName  = current.getStyle().getHtmlClass().trim().contains(" ")
                        ? current.getStyle().getHtmlClass().trim().split(" ")[0]
                        : current.getStyle().getHtmlClass();
        }
        menuMessage.getStyleClass().add( firstClassName + "__text-area");

        ((Pane)root.getParent()).getStyleClass().clear();
        ((Pane)root.getParent()).getStyleClass().add(client.getUnderRootDefaultStyle());
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

    // Returns the typing timeline, or null when the text was shown at once.
    private Timeline renderMenuText(UiElement current, StringProperty textHolder, String what ) {
        if (what.contains(Client.STOP_PREFIX) || useTextTimeline) {
            return renderTextWithTimeline(current, what, textHolder);
        }
        textHolder.setValue( what );
        return null;
    }
    private void setMenuProperties(Region menuMessage, UiElement element) {
        menuMessage.getStyleClass().add("magid-menu-textarea");
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
                boolean resizable = Boolean.getBoolean(element.getStyle().getCssProperty("-mg-window-resizable"));
                ((Stage)menuMessage.getScene().getWindow()).setResizable(resizable);
            }
        }
        if ( menuMessage instanceof TextArea textArea ) {
            textArea.setWrapText(true);
            textArea.setEditable(false);
            //menuMessage.setPrefWidth(600);
            textArea.setPrefRowCount(6);
        }
        menuMessage.setPrefHeight(450);
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

    public void setIgnoreTimelines(boolean really) {
        ignoreTimelines =  really;
    }
    public void setIgnoreTexttimelines(boolean really) {
        ignoreTexttimelines = really;
    }

    public void setIgnoreMaximization(boolean really) {
        ignoreViewPortMaximization = really;
    }
    public boolean getIgnoreMaximization() {
        return ignoreViewPortMaximization;
    }

    public UiElementRenderer(Region root, Scene scene, Client client) {
        this.root = root;
        this.scene = scene;
        this.client = client;
        underRoot = (Pane)root.getParent();
    }


    private Map<ObservableValue<?>, List<ChangeListener<?>>> listeners = new HashMap<>();
    public void registerListener(ObservableValue<?> value, ChangeListener<?> listener ) {
        if ( !listeners.containsKey(value) ) {
            listeners.put(value, new ArrayList<>());
        }
        listeners.get(value).add(listener);
    }

    public void removeListenersOnRedraw() {
        for ( ObservableValue<?> value : listeners.keySet() ) {
            for ( ChangeListener<?> listener : listeners.get(value) ) {
                value.removeListener((ChangeListener)listener);
            }
        }
    }

}
