package com.zarterstein.maqshiv;

import javax.json.JsonArray;
import javax.json.JsonObject;

public class Responses extends UIElement{
    public Responses() {}
    public void createFromJson(JsonObject jObj) {
        JsonArray array = jObj.getJsonArray("responses");
        for ( JsonObject object : array.getValuesAs(JsonObject.class)) {
            if ( object.containsKey("narration")) {
                Narration narration = new Narration();
                narration.setDisplayText(object.getString("narration"));
                this.getChildren().add(narration);
            }
            if ( object.containsKey("menu-name")) {
                Menu menu = new Menu().createFromJson(object);
                this.getChildren().add(menu);
            }
        }
    }
}
