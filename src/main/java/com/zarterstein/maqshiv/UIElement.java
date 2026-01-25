package com.zarterstein.maqshiv;

import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

public abstract class UIElement {
    private Style style;


    private List<UIElement> children;
    private UIElement parent;
    private String name;
    private String displayText;

    public UIElement() {
        children = new ArrayList<>();
        parent = null;
        displayText = "";
        style = new Style();
    }
    public UIElement(UIElement parent) {
        this();
        this.parent = parent;
    }

    public UIElement(UIElement parent, List<UIElement> children) {
        this(parent);
        this.children = children;
    }


    public Style getStyle() {
        return style;
    }

    public void setStyle(Style style) {
        this.style = style;
    }

    public List<UIElement> getChildren() {
        return children;
    }

    public int getChildrenCount() {
        return children.size();
    }

    public UIElement getFirstChild() {
        if ( children == null) {
            return null;
        }
        return children.getFirst();
    }

    public UIElement getLastChild() {
        if ( children == null) {
            return null;
        }
        return children.getLast();
    }

    public void setChildren(List<UIElement> children) {
        this.children = children;
    }

    public UIElement getParent() {
        return parent;
    }

    public void setParent(UIElement parent) {
        this.parent = parent;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    /**
     * Returns an empty string if no corresponding field is found in the supplied JSON. To be used in the cases,
     * where a string is needed and an empty string is preferred over a null value.
     * @param jObj
     * @param key
     * @return
     */
    public static String getStringIfSet(JsonObject jObj, String key) {
        if ( jObj.containsKey(key)) {
            return jObj.getString(key);
        }
        return "";
    }

}
