package com.emarsys.core.request.model;

import android.net.Uri;

import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.provider.uuid.UUIDProvider;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestModelTest {
    private String url;
    private RequestMethod method;
    private Map<String, Object> payload;
    private Map<String, String> headers;
    private long timestamp;
    private long ttl;
    private String id;
    private TimestampProvider timestampProvider;
    private UUIDProvider uuidProvider;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        url = "https://google.com";
        method = RequestMethod.PUT;
        payload = createPayload();
        headers = createHeaders();
        timestamp = System.currentTimeMillis();
        ttl = 6000;
        id = "uuid";
        timestampProvider = new TimestampProvider();
        uuidProvider = new UUIDProvider();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_urlShouldNotBeNull() {
        new RequestModel(null, method, payload, headers, timestamp, ttl, id);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_methodShouldNotBeNull() {
        new RequestModel(url, null, payload, headers, timestamp, ttl, id);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_headersShouldNotBeNull() {
        new RequestModel(url, method, payload, null, timestamp, ttl, id);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_idShouldNotBeNull() {
        new RequestModel(url, method, payload, headers, timestamp, ttl, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilder_constructor_requestModel_shouldNotBeNull() {
        new RequestModel.Builder(null);
    }

    @Test
    public void testBuilder_mandatoryArgumentsInitialized() throws Exception {
        RequestModel result = new RequestModel.Builder(timestampProvider, uuidProvider)
                .url(url)
                .build();

        assertEquals(new URL(url), result.getUrl());
    }

    @Test
    public void testBuilder_optionalArgumentsInitializedWithDefaultValue() {
        RequestModel result = new RequestModel.Builder(timestampProvider, uuidProvider).url(url).build();

        assertEquals(new HashMap<String, String>(), result.getHeaders());
        assertEquals(null, result.getPayload());
        assertEquals(RequestMethod.POST, result.getMethod());
        assertEquals(Long.MAX_VALUE, result.getTtl());
    }

    @Test
    public void testBuilder_idAndTimestampInitialized() {
        RequestModel result = new RequestModel.Builder(timestampProvider, uuidProvider).url(url).build();

        assertNotNull(result.getTimestamp());
        assertNotNull(result.getId());
    }

    @Test
    public void testBuilder_withAllArguments() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("q1", "v1");

        RequestModel result = new RequestModel.Builder(timestampProvider, uuidProvider)
                .url(url)
                .method(method)
                .payload(payload)
                .headers(headers)
                .queryParams(queryParams)
                .ttl(ttl)
                .build();

        String id = result.getId();
        long timestamp = result.getTimestamp();
        String urlWithQueryParams = url + "?q1=v1";
        RequestModel expected = new RequestModel(urlWithQueryParams, method, payload, headers, timestamp, ttl, id);

        assertEquals(expected, result);
    }

    @Test
    public void testBuilder_timestampCorrectlySet() {
        TimestampProvider timestampProvider = mock(TimestampProvider.class);
        final long timestamp = 1L;
        when(timestampProvider.provideTimestamp()).thenReturn(timestamp);

        RequestModel result = new RequestModel.Builder(timestampProvider, uuidProvider)
                .url(url)
                .build();

        assertEquals(timestamp, result.getTimestamp());
    }

    @Test
    public void testBuilder_requestIdCorrectlySet() {
        UUIDProvider uuidProvider = mock(UUIDProvider.class);
        final String requestId = "REQUEST_ID";
        when(uuidProvider.provideId()).thenReturn(requestId);

        RequestModel result = new RequestModel.Builder(timestampProvider, uuidProvider)
                .url(url)
                .build();

        assertEquals(requestId, result.getId());
    }

    @Test
    public void testBuilder_queryParamIsCorrectlySet() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("key1", "value1");

        RequestModel result = new RequestModel.Builder(timestampProvider, uuidProvider)
                .url("https://emarsys.com")
                .queryParams(queryParams).build();

        Uri uri = Uri.parse(result.getUrl().toString());

        assertEquals("emarsys.com", uri.getHost());
        assertEquals("value1", uri.getQueryParameter("key1"));
        assertEquals(1, uri.getQueryParameterNames().size());
    }

    @Test
    public void testBuilder_multipleQueryParamsAreCorrectlySet() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("key1", "value1");
        queryParams.put("key2", "value2");
        RequestModel result = new RequestModel.Builder(timestampProvider, uuidProvider)
                .url("https://emarsys.com")
                .queryParams(queryParams).build();

        Uri uri = Uri.parse(result.getUrl().toString());

        assertEquals("emarsys.com", uri.getHost());
        assertEquals("value1", uri.getQueryParameter("key1"));
        assertEquals("value2", uri.getQueryParameter("key2"));
        assertEquals(2, uri.getQueryParameterNames().size());
    }

    @Test
    public void testBuilder_ignoresEmptyMap() {
        RequestModel result = new RequestModel.Builder(timestampProvider, uuidProvider)
                .url("https://emarsys.com")
                .queryParams(new HashMap<String, String>()).build();

        assertEquals("https://emarsys.com", result.getUrl().toString());
    }

    @Test
    public void testBuilder_from() {
        RequestModel expected = new RequestModel(url, method, payload, headers, timestamp, ttl, id);

        RequestModel result = new RequestModel.Builder(expected).build();

        assertEquals(expected, result);
    }

    private Map<String, Object> createPayload() {
        Map<String, Object> result = new HashMap<>();
        result.put("key1", "value1");

        Map<String, Object> value2 = new HashMap<>();
        value2.put("key3", "value3");
        value2.put("key4", 5);

        result.put("key2", value2);

        return result;
    }

    private Map<String, String> createHeaders() {
        Map<String, String> result = new HashMap<>();
        result.put("content", "application/x-www-form-urlencoded");
        result.put("accept", "application/json");
        return result;
    }
}