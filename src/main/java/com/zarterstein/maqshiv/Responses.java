package com.zarterstein.maqshiv;

import javax.json.JsonArray;
import javax.json.JsonObject;

public class Responses extends UiElement {
    public Responses() {}
    public Responses createFromJson(JsonObject jObj) {
        JsonArray array = jObj.getJsonArray("responses");
        Client.addElementsToRootFromJson(this, array, getClient());
        return this;
    }
}
