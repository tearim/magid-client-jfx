package com.zarterstein.maqshiv;

import javax.json.JsonObject;

/**
 * An element whose place the client decides (an {@code <input>}, an {@code <img>} inside a text, …). The server sends it
 * before the text it belongs to. {@link Client} hands it to the narration whose text contains its {@code text-anchor},
 * otherwise to the menu of the same batch; the renderer draws it at its anchor ({@code by-anchor}) or before/after the
 * menu description ({@code prepend}/{@code append}). Same rules as magid-client-core.
 */
public class DetachedElement extends UiElement {
    public static final String BY_ANCHOR = "by-anchor";
    public static final String PREPEND = "prepend";
    public static final String APPEND = "append";

    private JsonObject data;
    private String mode = BY_ANCHOR;
    private String textAnchor = "";
    private String elementType = "";

    public DetachedElement() {
    }

    public DetachedElement createFromJson(JsonObject jObj) {
        data = jObj;
        elementType = getStringIfSet(jObj, "element-type");
        textAnchor = getStringIfSet(jObj, "text-anchor");
        String detached = getStringIfSet(jObj, "detached");
        // An image is detached only by its anchor; older servers sent it without "detached".
        mode = detached.isEmpty() || elementType.equals("image") ? BY_ANCHOR : detached;
        getStyle().applyFromJson(jObj, "");
        return this;
    }

    public String getField(String key) {
        return getStringIfSet(data, key);
    }

    public boolean hasField(String key) {
        return data.containsKey(key);
    }

    public String getMode() {
        return mode;
    }

    public String getTextAnchor() {
        return textAnchor;
    }

    public String getElementType() {
        return elementType;
    }

    public boolean isAnchoredIn(String text) {
        return !textAnchor.isEmpty() && text != null && text.contains(textAnchor);
    }
}
