package magid.connector;

import javafx.scene.control.Alert;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class MagidConnector {
    URL url;
    String baseURL;
    String request;
    String freshnessKey = "";
    String currentMenuName = "";
    String sessionId = "";
    String fileRequestToken = "";
    URLConnection connection;
    public static String MAGID_ANCHOR = "magid://";
    // Carries the session id, plain (no "Bearer"). Authorization is reserved for a user's identity token.
    public static final String SESSION_HEADER = "Magid-Session";
    // The command that carries a reply to the story; cmd itself only names server commands (set-xml, list-xmls, …).
    public static final String STORY_REPLY = "story-reply";

    public void setSessionId(String clientId) {
        this.sessionId = clientId;
    }

    public void setFileRequestToken(String fiReTo) {
        fileRequestToken = fiReTo;
    }
    public String getFileRequestToken() {
        return fileRequestToken;
    }

    public MagidConnector(String baseURL) {
        this.baseURL = baseURL;
    }

    public String getBaseURL(){
        return baseURL;
    }

    public String resolveAnchor(String resource){
        return resource.replace(MAGID_ANCHOR, baseURL);
    }

    public void connect()  {
        connect("");
    }
    private boolean silentConnectionErrors = false;
    public void setSilentConnectionErrors(boolean silentConnectionErrors) {
        this.silentConnectionErrors = silentConnectionErrors;
    }

    private String formBasicGetString(String command) {
        return baseURL + "?cmd=" + URLEncoder.encode(command, StandardCharsets.UTF_8) + "&freshness-key=" + freshnessKey  + "&current-scene=" + URLEncoder.encode(currentMenuName, StandardCharsets.UTF_8 ) + "&client-type=javafx";
    }
    private void connect(String command, Map<String, String> moreParams) {
        StringBuilder url = new StringBuilder(formBasicGetString(command));
        for ( Map.Entry<String, String> entry : moreParams.entrySet()) {
            url.append("&").append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)).append("=").append(
                 URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)
            );
        }
        _connect(url.toString());

    }
    private void connect(String command) {
        _connect(formBasicGetString(command));
    }
    private void _connect(String urlString) {
        try {
            url = new URL(urlString);
            connection = url.openConnection();
            if ( !sessionId.isEmpty() ) {
                connection.setRequestProperty(SESSION_HEADER, sessionId);
            }
            connection.connect();
        }
        catch (MalformedURLException e) {
            if ( !silentConnectionErrors ) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("There was a mistake with URL");
                alert.setContentText(e.getMessage() + "\n\n" + "Please double check your URL.");
                alert.showAndWait();
            }
               throw new RuntimeException("There was a mistake with URL: " + e.getMessage());
        }
        catch (IOException e) {
            if ( !silentConnectionErrors ) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("The server "+baseURL+" refused to connect. The action: " + (request.isEmpty()?"—":request) + " was not performed.");
                alert.setContentText(e.getMessage() + "\n\n" + "Please double check your URL.");
                alert.showAndWait();
            }
            throw new RuntimeException("There was a mistake with I/O operations: " + e.getMessage());
        }
    }

    public String getResponse() {
        return getResponse("");
    }

    public String connect(MagidRequest request) {
        _connect(request.getFullRequestString());
        return _getResponse();
    }

    public String getResponse(String command, Map<String, String> moreParams) {
        connect(command, moreParams);
        return _getResponse();
    }
    public String getResponse(String command) {
        connect(command);
        return _getResponse();
    }

    // A reply to the story (a menu choice, update-symbol, …), sent as cmd=story-reply&reply=… whatever it is called, so
    // it can't be taken for a server command. "" asks for the current menu (or starts a session) and stays cmd=.
    public String getStoryReplyResponse(String reply, Map<String, String> moreParams) {
        if (reply.isEmpty()) {
            return getResponse("", moreParams);
        }
        Map<String, String> params = new LinkedHashMap<>(moreParams);
        params.put("reply", reply);
        return getResponse(STORY_REPLY, params);
    }
    private String _getResponse() {

        StringBuilder response = new StringBuilder();

        try (BufferedReader responseReader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String tempLine = "";
            while ((tempLine = responseReader.readLine()) != null) {
                response.append(tempLine);
            }
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }
        return resolveAnchor(response.toString());
    }

    public String requestXml(String xmlPath) {
        String request = "set-xml";
        return getResponse(request, Map.of("path", xmlPath));
    }

    public String serverStatus() {
        return getResponse("server-status");
    }

    public JsonArray getXmlList() {
        String response = getResponse("list-xmls");
        JsonReader json = Json.createReader(new StringReader(response));
        JsonArray jsonArray = json.readArray();
        json.close();
        return jsonArray;
    }

    public void setFreshnessKey(String freshnessKey) {
        System.out.println("Freshness key ("+this.freshnessKey+") changed to " + freshnessKey);
        this.freshnessKey = freshnessKey;
    }

    public String getFreshnessKey() {
        return freshnessKey;
    }

    public void setCurrentMenuName(String currentMenuName) {
        this.currentMenuName = currentMenuName;
    }

    public String getCurrentMenuName() {
        return currentMenuName;
    }

    public void resetFreshnessKeyAndMenuName() {
        this.freshnessKey = "";
        this.currentMenuName = "";
    }

}
