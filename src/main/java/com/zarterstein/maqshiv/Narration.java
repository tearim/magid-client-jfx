package com.zarterstein.maqshiv;

import javax.json.JsonObject;

public class Narration extends UiElement {
    public Narration() {

    }

    public Narration createFromJson(JsonObject object) {
        String name = object.containsKey("narration")?object.getString("narration"):object.getString("text");
        setDisplayText(name);
        getStyle().applyFromJson(object, "");
        if ( object.containsKey("defer") ) {
            try {
                setDefer(Long.parseLong(object.getString("defer")));
            } catch ( NumberFormatException e ) {
                setDefer(0);
            }
        }
        return this;
    }
}
