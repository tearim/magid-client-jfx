package com.zarterstein.maqshiv;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public enum ResponseNodeType {
    MENU(Menu.class, "menu", "menu-name"),
    COMMAND(Command.class, "command", "command-name"),
    RESPONSES(Responses.class, "responses"),
    NARRATION(Command.class, "narration");

    public final Class<? extends UIElement> uiElClass;
    public final String[] labels;

    private static final Map<String, Class<? extends UIElement>> ELEM_TYPE = new HashMap<>();

    static {
        for ( ResponseNodeType type : values() ) {
            for ( String label: type.labels ) {
                ELEM_TYPE.put(label, type.uiElClass);
            }
        }
    }
    private ResponseNodeType(Class<? extends UIElement> tClass, String ...label){
        uiElClass = tClass;
        this.labels = label;
    }

    public static UIElement byLabel(String label) {
        Class<? extends UIElement> elemClass = ELEM_TYPE.get(label);
        if ( elemClass == null) {
            return null;
        }
        try {
            Constructor<? extends UIElement> constructor = elemClass.getConstructor();
            UIElement element = constructor.newInstance();
            return element;
        } catch (Exception e) {
            return null;
        }

    }
}
