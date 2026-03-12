package com.zarterstein.maqshiv.utils;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.Reader;

public class JsonUtils {
    public static String getStringIfSet(JsonObject jsonObject, String key) {
        if ( jsonObject.containsKey(key) ) {
            return jsonObject.getString(key);
        }
        return "";
    }

    public static String getStringIfSet(String jsonString, String key) {
        try {
            JsonReader jR = Json.createReader(Reader.of(jsonString));
            JsonObject jO = jR.read().asJsonObject();
            return getStringIfSet(jO, key);
        } catch (Exception e) {
            return "";
        }
    }
}
