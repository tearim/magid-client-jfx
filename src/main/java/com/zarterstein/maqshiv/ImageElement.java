package com.zarterstein.maqshiv;

import javax.json.JsonObject;

/**
 * An image on its own: {@code <img>} outside any text ({@code element-type: image} without a {@code text-anchor}).
 * An image inside a text is a {@link DetachedElement} instead.
 */
public class ImageElement extends UiElement {
    private JsonObject data;

    public ImageElement() {
    }

    public ImageElement createFromJson(JsonObject jObj) {
        data = jObj;
        getStyle().applyFromJson(jObj, "");
        if ( jObj.containsKey("defer") ) {
            try {
                setDefer(Long.parseLong(jObj.getString("defer")));
            } catch ( NumberFormatException e ) {
                setDefer(0);
            }
        }
        return this;
    }

    public String getField(String key) {
        return getStringIfSet(data, key);
    }
}
