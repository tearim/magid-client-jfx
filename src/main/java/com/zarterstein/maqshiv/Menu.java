package com.zarterstein.maqshiv;

import com.zarterstein.maqshiv.advanced.MusicPlayer;

import javax.json.JsonArray;
import javax.json.JsonObject;

public class Menu extends UiElement {

    private MusicPlayer player;


    public Menu createFromJson(JsonObject jObj) {

        getStyle().applyFromJson(jObj, "menu-");
        setName(getStringIfSet(jObj, "menu-name"));
        setDisplayText(getStringIfSet(jObj, "menu-description"));
        if (jObj.containsKey("menu-options")) {
            JsonArray array = jObj.getJsonArray("menu-options");
            for ( JsonObject item : array.getValuesAs(JsonObject.class)) {
                Command cmd = new Command(item, getClient());
                getChildren().add(cmd);
            }
        }
        if ( jObj.containsKey("menu-background-music") ) {
            setPlayer(new MusicPlayer(0.2f, jObj.getString("menu-background-music")));
        }
        return this;
    }

    public Menu(){

    }

    public MusicPlayer getPlayer() {
        return player;
    }

    public Menu setPlayer(MusicPlayer player) {
        this.player = player;
        return this;
    }

}
