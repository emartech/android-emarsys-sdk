package com.emarsys.core.response;

import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.testUtil.TimeoutUtils;
import com.emarsys.core.provider.timestamp.TimestampProvider;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResponseModelTest {

    private static final long timestamp = 4200;

    private int statusCode;
    private String message;
    private Map<String, String> headers;
    private Map<String, List<String>> listHeaders;
    private String body;
    private TimestampProvider timestampProvider;
    private RequestModel requestModel;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        statusCode = 200;
        message = "OK";
        headers = createHeaders();
        listHeaders = createListHeaders();
        body = "payload";
        timestampProvider = mock(TimestampProvider.class);
        when(timestampProvider.provideTimestamp()).thenReturn(timestamp);
        requestModel = mock(RequestModel.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_statusCodeShouldNotBeBelow200() {
        new ResponseModel(199, message, headers, body, timestamp, requestModel);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_statusCodeShouldNotBeOver600() {
        new ResponseModel(600, message, headers, body, timestamp, requestModel);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_messageShouldNotBeNull() {
        new ResponseModel(statusCode, null, headers, body, timestamp, requestModel);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_headersShouldNotBeNull() {
        new ResponseModel(statusCode, message, null, body, timestamp, requestModel);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_requestModelShouldNotBeNull() {
        new ResponseModel(statusCode, message, headers, body, timestamp, null);
    }

    @Test
    public void testGetParsedBody_whenBodyIsAValidJson() throws JSONException {
        ResponseModel responseModel = new ResponseModel(200,
                "", new HashMap<String, String>(),
                "{ 'foo': 'bar', 'a': 1, 'nested': { 'b': 'c' }}",
                timestamp,
                requestModel);

        JSONObject result = responseModel.getParsedBody();
        JSONObject expected = new JSONObject()
                .put("foo", "bar")
                .put("a", 1)
                .put("nested", new JSONObject().put("b", "c"));

        assertEquals(expected.toString(), result.toString());
    }

    @Test
    public void testGetParsedBody_whenBodyIsAnInvalidJson() throws JSONException {
        ResponseModel responseModel = new ResponseModel(200,
                "", new HashMap<String, String>(),
                "<html>Not valid json</html>",
                timestamp,
                requestModel);

        JSONObject result = responseModel.getParsedBody();
        assertNull(result);
    }

    @Test
    public void testGetParsedBody_whenBodyIsNull() throws JSONException {
        ResponseModel responseModel = new ResponseModel(200,
                "", new HashMap<String, String>(),
                null,
                timestamp,
                requestModel);

        JSONObject result = responseModel.getParsedBody();
        assertNull(result);
    }

    @Test
    public void testBuilder_withAllArguments() {
        ResponseModel expected = new ResponseModel(statusCode, message, headers, body, timestamp, requestModel);
        ResponseModel result = new ResponseModel.Builder(timestampProvider)
                .statusCode(statusCode)
                .message(message)
                .headers(listHeaders)
                .body(body)
                .requestModel(requestModel)
                .build();
        assertEquals(expected, result);
    }

    @Test
    public void testBuilder_withMandatoryArguments() {
        ResponseModel expected = new ResponseModel(statusCode, message, new HashMap<String, String>(), body, timestamp, requestModel);
        ResponseModel result = new ResponseModel.Builder(timestampProvider)
                .statusCode(statusCode)
                .message(message)
                .body(body)
                .requestModel(requestModel)
                .build();
        assertEquals(expected, result);
    }

    @Test
    public void testBuilder_joinShouldReturnConcatenatedValue() {
        String expected = "alma, korte, szilva, malna";
        List<String> inputList = Arrays.asList("alma", "korte", "szilva", "malna");
        String deliminator = ", ";
        ResponseModel.Builder builder = new ResponseModel.Builder();
        String actual = builder.join(deliminator, inputList);
        assertEquals(expected, actual);
    }

    @Test
    public void testBuilder_convertHeadersShouldReturnJoinedHeaderValues() {
        Map<String, String> expected = new HashMap<>();
        expected.put("one", "alma; szilva");
        expected.put("two", "korte; malna");

        Map<String, List<String>> inputHeaders = new HashMap<>();
        inputHeaders.put("one", Arrays.asList("alma", "szilva"));
        inputHeaders.put("two", Arrays.asList("korte", "malna"));

        ResponseModel.Builder builder = new ResponseModel.Builder();
        Map<String, String> result = builder.convertHeaders(inputHeaders);
        assertEquals(expected, result);
    }

    private Map<String, String> createHeaders() {
        Map<String, String> result = new HashMap<>();
        result.put("content", "application/x-www-form-urlencoded");
        result.put("set-cookie", "UserID=JohnDoe; Max-Age=3600; Version=1");
        return result;
    }

    private Map<String, List<String>> createListHeaders() {
        Map<String, List<String>> result = new HashMap<>();
        result.put("content", Arrays.asList("application/x-www-form-urlencoded"));
        result.put("set-cookie", Arrays.asList("UserID=JohnDoe", "Max-Age=3600", "Version=1"));
        return result;
    }

}