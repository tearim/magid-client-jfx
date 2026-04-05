package com.zarterstein.maqshiv;

import javax.json.JsonObject;

public class Narration extends UiElement {
    public Narration() {

    }

    public Narration createFromJson(JsonObject object) {
        String name = object.containsKey("narration")?object.getString("narration"):object.getString("text");
        setDisplayText(name);
        getStyle().applyFromJson(object, "");
        if ( getStyle().getHtmlClass().isEmpty()) {
            getStyle().setHtmlClass("magid-default-narration");
        }
        if ( object.containsKey("defer") ) {
            try {
                setDefer(Long.parseLong(object.getString("defer")));
            } catch ( NumberFormatException e ) {
                // This means we ignore it
                setDefer(0);
            }
        }
        return this;
    }
}
