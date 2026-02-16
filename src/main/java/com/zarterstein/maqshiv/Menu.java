package com.zarterstein.maqshiv;

import javax.json.JsonArray;
import javax.json.JsonObject;

public class Menu extends UiElement {

    public Menu createFromJson(JsonObject jObj) {

        getStyle().applyFromJson(jObj, "menu-");
        setName(getStringIfSet(jObj, "menu-name"));
        setDisplayText(getStringIfSet(jObj, "menu-description"));
        if (jObj.containsKey("menu-options")) {
            JsonArray array = jObj.getJsonArray("menu-options");
            for ( JsonObject item : array.getValuesAs(JsonObject.class)) {
                Command cmd = new Command(item, getClient());
                getChildren().add(cmd);
            }
        }
        return this;
    }

    public Menu(){

    }
}
