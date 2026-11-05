package com.zarterstein.maqshiv.ui;

import com.zarterstein.maqshiv.Client;
import com.zarterstein.maqshiv.DetachedElement;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Draws detached elements and images. {@link #RENDERERS} maps {@code element-type} → node; add new element types there
 * only.
 */
public class DetachedNodes {
    private static final Map<String, BiFunction<DetachedElement, Client, Node>> RENDERERS = Map.of(
            "input", DetachedNodes::input,
            "image", (element, client) -> image(element::getField, element.getStyle(), client)
    );
    // Like the web client: an async input sends update-symbol once the user has stopped changing it for this long.
    private static final Duration ASYNC_DEBOUNCE = Duration.millis(500);

    private DetachedNodes() {
    }

    // Null (and a log line) for an element type this client doesn't know.
    public static Node create(DetachedElement element, Client client) {
        BiFunction<DetachedElement, Client, Node> renderer = RENDERERS.get(element.getElementType());
        if (renderer == null) {
            System.out.println("[magid] Unknown detached element-type: " + element.getElementType());
            return null;
        }
        return renderer.apply(element, client);
    }

    private static Node input(DetachedElement element, Client client) {
        String name = element.getField("input-name");
        String type = element.getField("input-type").isEmpty() ? "text" : element.getField("input-type");
        boolean async = element.getField("input-async").equalsIgnoreCase("true");
        PauseTransition debounce = new PauseTransition(ASYNC_DEBOUNCE);
        debounce.setOnFinished(e -> client.proceedWith("update-symbol"));

        Control control;
        switch (type) {
            case "checkbox" -> {
                CheckBox checkBox = new CheckBox();
                checkBox.setSelected(client.getUserInput(name).equals("true"));
                checkBox.selectedProperty().addListener((o, before, now) ->
                        changed(client, name, now ? "true" : "false", async, debounce));
                control = checkBox;
            }
            case "text", "number" -> {
                TextField field = new TextField(client.getUserInput(name));
                if (type.equals("number")) {
                    field.setTextFormatter(new TextFormatter<String>(change ->
                            change.getControlNewText().matches("-?\\d*([.,]\\d*)?") ? change : null));
                }
                field.textProperty().addListener((o, before, now) -> changed(client, name, now, async, debounce));
                control = field;
            }
            default -> {
                System.out.println("[magid] Unknown input-type: " + type);
                return null;
            }
        }
        control.getStyleClass().add("magid-input-element");
        addClasses(control, element.getStyle().getHtmlClass());
        control.setStyle(element.getStyle().getCss());

        HBox wrapper = new HBox(6);
        wrapper.setAlignment(Pos.CENTER_LEFT);
        wrapper.getStyleClass().add("magid-input-wrapper");
        if (!element.getField("input-label").isEmpty()) {
            Label label = new Label(element.getField("input-label"));
            label.setLabelFor(control);
            wrapper.getChildren().add(label);
        }
        wrapper.getChildren().add(control);
        return wrapper;
    }

    private static void changed(Client client, String name, String value, boolean async, PauseTransition debounce) {
        client.setUserInput(name, value);
        if (async && !value.isEmpty()) {
            debounce.playFromStart();
        }
    }

    /**
     * An author's {@code <img>}: {@code image} (the URL), {@code alt}, {@code width}/{@code height} (a bare number is
     * pixels; with only one of them the image keeps its proportions), and the author's class and css on the image.
     */
    public static Node image(Function<String, String> field, Style style, Client client) {
        String url = withFileRequestToken(field.apply("image"), client);
        if (url.isEmpty()) {
            return null;
        }
        String alt = field.apply("alt");
        ImageView view = new ImageView(new Image(url, true));
        Optional<Double> width = pixels(field.apply("width"));
        Optional<Double> height = pixels(field.apply("height"));
        width.ifPresent(view::setFitWidth);
        height.ifPresent(view::setFitHeight);
        // As in HTML: both sizes stretch the image, one keeps its proportions.
        view.setPreserveRatio(width.isEmpty() || height.isEmpty());
        view.getStyleClass().add("magid-image");
        addClasses(view, style.getHtmlClass());
        view.setStyle(style.getCss());
        if (!alt.isEmpty()) {
            view.setAccessibleText(alt);
            Tooltip.install(view, new Tooltip(alt));
        }
        return view;
    }

    // Files from the Magid server need the session's file-request-token: an image request can't send the session
    // header. Images from elsewhere are left alone, so the token never leaves the server.
    static String withFileRequestToken(String url, Client client) {
        String base = client.getConnector().getBaseURL().replaceAll("/+$", "");
        String token = client.getConnector().getFileRequestToken();
        if (url == null || token == null || token.isEmpty()
                || !(url.equals(base) || url.startsWith(base + "/") || url.startsWith(base + "?"))) {
            return url == null ? "" : url;
        }
        return url + (url.contains("?") ? "&" : "?") + "file-request-token="
                + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }

    private static Optional<Double> pixels(String value) {
        String v = value == null ? "" : value.trim().replaceFirst("px$", "");
        try {
            return v.isEmpty() ? Optional.empty() : Optional.of(Double.parseDouble(v));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private static void addClasses(Node node, String classes) {
        if (classes == null) {
            return;
        }
        for (String className : classes.trim().split("\\s+")) {
            if (!className.isEmpty()) {
                node.getStyleClass().add(className);
            }
        }
    }
}
