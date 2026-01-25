package magid.connector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MagidConnector {
    URL url;
    String baseURL;
    String request;
    URLConnection connection;

    public MagidConnector(String baseURL) {
        this.baseURL = baseURL;
    }

    public void connect()  {
        connect("");
    }
    private void connect(String request) {
        try {
            url = new URL(baseURL + "?cmd=" + request);
            connection = url.openConnection();
            connection.connect();
        }
        catch (MalformedURLException e) {
            throw new RuntimeException("There was a mistake with URL: " + e.getMessage());
        }
        catch (IOException e) {
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
        return response.toString();
    }


}
