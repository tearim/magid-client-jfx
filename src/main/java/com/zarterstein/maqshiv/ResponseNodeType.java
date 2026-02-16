package com.zarterstein.maqshiv;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public enum ResponseNodeType {
    MENU(Menu.class, "menu", "menu-name"),
    COMMAND(Command.class, "command", "command-name"),
    RESPONSES(Responses.class, "responses"),
    NARRATION(Narration.class, "narration", "text"),
    CONFIG(Config.class, "config", "configuration"),
    VISUAL_TRANSITION(VisualTransition.class, "visual");

    public final Class<? extends UiElement> uiElClass;
    public final String[] labels;

    private static Client client;

    private static final Map<String, Class<? extends UiElement>> ELEM_TYPE = new HashMap<>();

    static {
        for ( ResponseNodeType type : values() ) {
            for ( String label: type.labels ) {
                ELEM_TYPE.put(label, type.uiElClass);
            }
        }
    }
    private ResponseNodeType(Class<? extends UiElement> tClass, String ...label){
        uiElClass = tClass;
        this.labels = label;
    }

    public static void setClient(Client client) {
        ResponseNodeType.client = client;
    }

    public static UiElement byLabel(String label) {
        Class<? extends UiElement> elemClass = ELEM_TYPE.get(label);
        if ( elemClass == null) {
            return null;
        }
        try {
            Constructor<? extends UiElement> constructor = elemClass.getConstructor();
            UiElement element = constructor.newInstance();
            element.setClient(client);
            return element;
        } catch (Exception e) {
            return null;
        }

    }
}
