package com.zarterstein.maqshiv.ui;

import com.zarterstein.maqshiv.DetachedElement;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Display text with detached elements at their anchors, as a {@link TextFlow}. Each anchor becomes one
 * {@link #ANCHOR_CHAR} in the text, so a typewriter reveals it like a letter: the element appears whole when the
 * reveal reaches it. Write the (partly) revealed text to {@link #textProperty()}.
 */
public class AnchoredText {
    public static final String ANCHOR_CHAR = "￼";
    private static final Pattern ANCHOR_SPLIT = Pattern.compile(Pattern.quote(ANCHOR_CHAR));

    private final TextFlow flow = new TextFlow();
    private final List<Text> texts = new ArrayList<>();
    private final List<Node> nodes = new ArrayList<>();
    private final StringProperty shown = new SimpleStringProperty(flow, "text", "");
    private final String markedText;

    /**
     * @param text     the display text, anchors included
     * @param elements the detached elements whose anchors are in the text
     * @param toNode   draws an element; null draws nothing at its anchor
     */
    public AnchoredText(String text, List<DetachedElement> elements, Function<DetachedElement, Node> toNode) {
        List<DetachedElement> inOrder = elements.stream()
                .filter(e -> e.isAnchoredIn(text))
                .sorted(Comparator.comparingInt(e -> text.indexOf(e.getTextAnchor())))
                .toList();
        StringBuilder marked = new StringBuilder();
        int from = 0;
        texts.add(new Text());
        flow.getChildren().add(texts.getLast());
        for (DetachedElement element : inOrder) {
            int at = text.indexOf(element.getTextAnchor(), from);
            if (at < 0) {
                continue;
            }
            marked.append(text, from, at).append(ANCHOR_CHAR);
            from = at + element.getTextAnchor().length();
            Node node = toNode.apply(element);
            if (node == null) {
                node = new Region();
            }
            node.setVisible(false);
            node.setManaged(false);
            nodes.add(node);
            texts.add(new Text());
            flow.getChildren().addAll(node, texts.getLast());
        }
        marked.append(text.substring(from));
        markedText = marked.toString();
        shown.addListener((observable, before, now) -> reveal(now));
    }

    private void reveal(String revealed) {
        String[] parts = ANCHOR_SPLIT.split(revealed == null ? "" : revealed, -1);
        for (int i = 0; i < texts.size(); i++) {
            texts.get(i).setText(i < parts.length ? parts[i] : "");
        }
        for (int i = 0; i < nodes.size(); i++) {
            boolean reached = i < parts.length - 1;
            nodes.get(i).setVisible(reached);
            nodes.get(i).setManaged(reached);
        }
    }

    // A TextFlow's Text nodes don't take -fx-text-fill, so they follow the font and colour that the story's CSS gives
    // the control holding the flow.
    public void followTextStyleOf(Labeled holder) {
        for (Text text : texts) {
            text.fontProperty().bind(holder.fontProperty());
            text.fillProperty().bind(holder.textFillProperty());
        }
    }

    public TextFlow getFlow() {
        return flow;
    }

    public StringProperty textProperty() {
        return shown;
    }

    // The display text with each placed anchor replaced by ANCHOR_CHAR.
    public String getMarkedText() {
        return markedText;
    }
}
