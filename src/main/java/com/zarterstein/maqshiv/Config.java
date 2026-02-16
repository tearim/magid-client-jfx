package com.zarterstein.maqshiv;

import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

public class Config extends UiElement {
    public Config() {

    }

    private void processCssFiles(String fileNames) {
        List<String> names = new ArrayList<>();
        if ( fileNames.contains(";")) {
            for ( String name : fileNames.split(";")) {
                names.add(name);
            }
        } else {
            names.add(fileNames);
        }
        for ( String name : names ) {
            getClient().addCssFile(name);
        }
    }


    public Config createFromJson(JsonObject object) {
        if ( !object.containsKey("config")) {
            return null;
        }
        JsonObject config = object.getJsonObject("config");
        for ( String name : config.keySet()) {
            if ( name.equals("css-files")) {
                processCssFiles(config.getString(name));
            } else
                System.out.println(name + ": " + config.get(name));
        }
        /* String name = object.containsKey("narration")?object.getString("narration"):object.getString("text");
        setDisplayText(name);
        getStyle().applyFromJson(object, ""); */
        return this;
    }
}
