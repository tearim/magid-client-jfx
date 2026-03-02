package com.zarterstein.maqshiv;

import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            } else {
                getClient().setVar(name, config.getString(name));
            }
        }
        return this;
    }
}
