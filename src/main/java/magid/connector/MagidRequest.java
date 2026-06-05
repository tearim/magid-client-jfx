package magid.connector;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class MagidRequest {
    private static final Logger logger = Logger.getLogger(MagidRequest.class.getName());
    private final String protocol;
    private final String request;
    private final String baseURL;
    private final String state;
    private final String freshnessKey;
    private final Map<String, String> params;

    public String getRequest() {
        return request;
    }
    public String getBaseURL() {
        return protocol + "://" + baseURL;
    }
    public String getState() {
        return state;
    }
    public String getFreshnessKey() {
        return freshnessKey;
    }
    public Map<String, String> getParams() {
        return params;
    }

    public String getFullRequestString() {
        StringBuilder urlParamPart = new StringBuilder();
        int i = 0;
        params.put("cmd", request == null ? "" :  request);
        params.put("freshnessKey", freshnessKey == null ? "" : freshnessKey);
        for ( Map.Entry<String, String> entry : params.entrySet()) {
            urlParamPart
                    .append(i==0?"":"&")
                        .append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                    .append("=")
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)
            );
            i++;
        }
        URL url = null;
        try {
            url = new URI(
                    protocol,
                    baseURL.replaceAll("^[\\w]+\\:\\/\\/", "").replace("/", ""),
                    "/",
                    urlParamPart.isEmpty() ? null : urlParamPart.toString(), // Java auto-prepends the '?' safely
                    null
            ).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            logger.severe(e.getMessage());
        }
        if ( url == null) {
            throw new RuntimeException("Could not create URL");
        }
        return
              url.toString();
    }
    private MagidRequest(String request, String baseURL, String state, String freshnessKey, Map<String, String> params) {
        this.request = request;
        this.baseURL = baseURL.replaceAll("^[\\w]+\\:\\/\\/", "").replace("/", "");

        String[] candidate = baseURL.split(":", 2);
        this.protocol = candidate.length < 2 ? "https" : candidate[0];

        this.state = state;
        this.freshnessKey = freshnessKey;
        this.params = params == null ? new HashMap<>() : params;
    }
    public static class Builder {
        private MagidRequest magidRequest;
        private String request;
        private String baseURL;
        private String state;
        private String freshnessKey;
        private Map<String, String> params;
        public Builder() {
        }

        /**
         * Constructor for a MagidRequest with a request and baseURL.
         * @param request usually, a command
         * @param baseURL a URL to the Magid server
         */
        public Builder(String request, String baseURL) {
            this.request = request;
        }

        public Builder request(String request) {
            this.request = request;
            return this;
        }

        public Builder baseURL(String baseURL) {
            this.baseURL = baseURL;
            return this;
        }

        public Builder state(String state) {
            this.state = state;
            return this;
        }

        public Builder freshnessKey(String freshnessKey) {
            this.freshnessKey = freshnessKey;
            return this;
        }

        public Builder params (Map<String, String> parameters) {
            this.params = parameters;
            return this;
        }

        public MagidRequest build() {
            if ( request == null ) {
                throw new IllegalArgumentException("Cannot build MagidRequest: Request string is null");
            }
            if ( baseURL == null ) {
                throw new IllegalArgumentException("Cannot build MagidRequest: Base URL is null");
            }
            return new MagidRequest(request, baseURL, state, freshnessKey, params);
        }
    }

}
