    package com.zarterstein.maqshiv;

    import javax.json.JsonObject;

    public class Command extends UiElement {
    private String responseCommand;

    public String getDescription() {
        return getDisplayText();
    }

    public void setDescription(String description) {
        setDisplayText(description);
    }

    public String getResponseCommand() {
        return responseCommand;
    }

    public void setResponseCommand(String responseCommand) {
        this.responseCommand = responseCommand;
    }



    // Each UIElement child must implement no-args constructor for factory
    public Command() {
    }

    public Command(JsonObject jObj) {
        createFromJson(jObj);
    }

    public Command(JsonObject jObj, Client client) {
        setClient(client);
        createFromJson(jObj);
    }

    public Command createFromJson(JsonObject jObj) {
        setDescription(UiElement.getStringIfSet(jObj, "command-description"));
        setResponseCommand(UiElement.getStringIfSet(jObj, "command-name"));
        getStyle().applyFromJson(jObj, "command-");
        return this;
    }

}
