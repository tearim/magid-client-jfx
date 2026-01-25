    package com.zarterstein.maqshiv;

    import javax.json.JsonObject;

    public class Command extends UIElement{
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
        setDescription(jObj.getString("command-description"));
        setResponseCommand(jObj.getString("command-name"));
        getStyle().applyFromJson(jObj, "command-");
    }

}
