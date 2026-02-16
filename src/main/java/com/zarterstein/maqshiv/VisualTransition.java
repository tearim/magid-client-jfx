package com.zarterstein.maqshiv;

import javax.json.JsonObject;

public class VisualTransition extends UiElement {


    private String type = "";
    private double length = 0;
    private String target = "";
    private boolean block = false;
    private boolean deferRedraw = false;
    public VisualTransition() {

    }

    public VisualTransition createFromJson(JsonObject object) {
        type = UiElement.getStringIfSet(object, "transition-type");
        String lengthString = UiElement.getStringIfSet(object,"transition-length");
        try {
            length = Double.parseDouble(lengthString);
        } catch (Exception e ) {
            //
        }
        target = UiElement.getStringIfSet(object,"transition-target");
        block = Boolean.parseBoolean(UiElement.getStringIfSet(object, "transition-blocking"));
        return this;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
    public void setBlocking(boolean blocking) {
        block = blocking;
    }
    public boolean isBlocking() {
        return block;
    }

    public void setDeferRedraw(boolean defer) {
        deferRedraw = defer;
    }

    public boolean isDeferringRedraw() {
        return deferRedraw;
    }
}
