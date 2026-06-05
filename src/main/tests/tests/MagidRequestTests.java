package tests;

import magid.connector.MagidRequest;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MagidRequestTests {
    @Test
    void emptyRequestThrows() {
        MagidRequest.Builder request = new MagidRequest.Builder();
        Exception exception = assertThrows(IllegalArgumentException.class, request::build);
        assertEquals("Cannot build MagidRequest: Request string is null", exception.getMessage());
    }

    @Test
    void emptyBaseURLThrows() {
        MagidRequest.Builder request = new MagidRequest.Builder().request("hello");
        Exception exception = assertThrows(IllegalArgumentException.class, request::build);
        assertEquals("Cannot build MagidRequest: Base URL is null", exception.getMessage());
    }

    @Test
    void buildRequest() {
        MagidRequest request = new MagidRequest.Builder().request("hello").baseURL("https://example.com").build();
        assertEquals("hello", request.getRequest());
        assertEquals("https://example.com", request.getBaseURL());
    }

    @Test
    void fullBuildRequest() {
        Map<String, String> params = new HashMap<>();
        params.put("param1", "value1");
        params.put("param2", "value2");
        MagidRequest request = new MagidRequest.Builder()
                .request("hello")
                .baseURL("https://example.com")
                .freshnessKey("freshnessKey-01")
                .state("state-01")
                .params(params)
                .build();
        assertEquals("hello", request.getRequest());
        assertEquals("https://example.com", request.getBaseURL());
        assertEquals("freshnessKey-01", request.getFreshnessKey());
        assertEquals("state-01", request.getState());
        assertEquals(params, request.getParams());
    }

    @Test
    void getFullRequestString() {
        MagidRequest request = new MagidRequest.Builder().request("hello").baseURL("https://example.com").build();
        assertEquals("https://example.com/?freshnessKey=&cmd=hello", request.getFullRequestString());
    }

    @Test
    void getFullRequestStringWithParams() {
        Map<String, String> params = new HashMap<>();
        params.put("param1", "value1");
        params.put("param2", "value2");
        MagidRequest request = new MagidRequest.Builder()
                .request("hello")
                .baseURL("https://example.com")
                .params(params)
                .build();
        assertEquals("https://example.com/?freshnessKey=&cmd=hello&param1=value1&param2=value2", request.getFullRequestString());
    }
}