package com.zarterstein.maqshiv;

import com.zarterstein.maqshiv.utils.JsonUtils;

import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
            getClient().addCssFile(name + "?cmd=resolve-magid-protocol&file-request-token=" + getClient().getConnector().getFileRequestToken() );

        }
    }


    public Config createFromJson(JsonObject object) {
        if ( !object.containsKey("config")) {
            return null;
        }
        JsonObject config = object.getJsonObject("config");
        getClient().getConnector().setFreshnessKey(JsonUtils.getStringIfSet(config, "freshness-key"));
        if ( config.containsKey("css-files-javafx")) {
            processCssFiles(JsonUtils.getStringIfSet(config, "css-files-javafx"));
        } else {
            processCssFiles(JsonUtils.getStringIfSet(config, "css-files"));
        }
        List<String> notVarnames = List.of("css-files", "freshness-key");
        for ( String name : config.keySet()) {
            if ( !notVarnames.contains(name)) {
                getClient().setVar(name, config.getString(name));
            }
        }
        return this;
    }
}
