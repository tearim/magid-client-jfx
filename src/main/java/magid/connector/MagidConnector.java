package magid.connector;

import javafx.scene.control.Alert;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class MagidConnector {
    URL url;
    String baseURL;
    String request;
    URLConnection connection;
    public static String MAGID_ANCHOR = "magid://";

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
    private void connect(String request) {
        try {
            url = new URL(baseURL + "?cmd=" + request);
            connection = url.openConnection();
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


    public String getResponse(String command) {
        connect(command);
        StringBuilder response = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String tempLine = "";
            while ((tempLine = reader.readLine()) != null) {
                response.append(tempLine);
            }
        } catch (IOException exception) {

        }
        return resolveAnchor(response.toString());
    }

    public String requestXml(String xmlPath) {
        String request = "set-xml&path=" + xmlPath;
        return getResponse(request);
    }

    public JsonArray getXmlList() {
        String response = getResponse("list-xmls");
        JsonReader json = Json.createReader(new StringReader(response));
        JsonArray jsonArray = json.readArray();
        json.close();
        return jsonArray;
    }


}
