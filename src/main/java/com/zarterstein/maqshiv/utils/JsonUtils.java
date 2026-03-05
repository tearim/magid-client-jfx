package com.zarterstein.maqshiv.utils;

import javax.json.JsonObject;

public class JsonUtils {
    public static String getStringIfSet(JsonObject jsonObject, String key) {
        if ( jsonObject.containsKey(key) ) {
            return jsonObject.getString(key);
        }
        return "";
    }
}
