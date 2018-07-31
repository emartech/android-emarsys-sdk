package com.emarsys.core.request;

import android.content.Context;
import android.os.Handler;
import android.support.test.InstrumentationRegistry;

import com.emarsys.core.concurrency.CoreSdkHandlerProvider;
import com.emarsys.core.connection.ConnectionWatchDog;
import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.fake.FakeCompletionHandler;
import com.emarsys.core.provider.uuid.UUIDProvider;
import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.request.model.RequestModelRepository;
import com.emarsys.core.testUtil.ConnectionTestUtils;
import com.emarsys.core.testUtil.DatabaseTestUtils;
import com.emarsys.core.testUtil.TimeoutUtils;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.worker.DefaultWorker;
import com.emarsys.core.worker.Worker;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

public class RequestManagerDennaTest {

    private static final String DENNA_ECHO_URL = "https://ems-denna.herokuapp.com/echo";

    private RequestManager manager;
    private Map<String, String> headers;
    private RequestModel model;
    private CountDownLatch latch;
    private FakeCompletionHandler handler;
    private CoreSdkHandlerProvider provider;
    private Handler coreSdkHandler;
    private Worker worker;
    private TimestampProvider timestampProvider;
    private UUIDProvider uuidProvider;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        DatabaseTestUtils.INSTANCE.deleteCoreDatabase();

        Context context = InstrumentationRegistry.getTargetContext();
        ConnectionTestUtils.checkConnection(context);

        provider = new CoreSdkHandlerProvider();
        coreSdkHandler = provider.provideHandler();

        ConnectionWatchDog connectionWatchDog = new ConnectionWatchDog(context, coreSdkHandler);
        Repository<RequestModel, SqlSpecification> requestRepository = new RequestModelRepository(context);

        latch = new CountDownLatch(1);
        handler = new FakeCompletionHandler(latch);
        RestClient restClient = new RestClient(mock(Repository.class), mock(TimestampProvider.class));
        worker = new DefaultWorker(requestRepository, connectionWatchDog, coreSdkHandler, handler, restClient);
        timestampProvider = new TimestampProvider();
        uuidProvider = new UUIDProvider();
        manager = new RequestManager(coreSdkHandler, requestRepository, worker);
        headers = new HashMap<>();
        headers.put("accept", "application/json");
        headers.put("content", "application/x-www-form-urlencoded");
        headers.put("Header1", "value1");
        headers.put("Header2", "value2");
    }

    @After
    public void tearDown() {
        coreSdkHandler.getLooper().quit();
    }

    @Test
    public void testGet() throws Exception {
        model = new RequestModel.Builder(timestampProvider, uuidProvider).url(DENNA_ECHO_URL).method(RequestMethod.GET).headers(headers).build();
        manager.submit(model);
        latch.await();

        assertEquals(0, handler.getOnErrorCount());
        assertEquals(1, handler.getOnSuccessCount());
        assertEquals(200, handler.getSuccessResponseModel().getStatusCode());

        JSONObject responseJson = new JSONObject(handler.getSuccessResponseModel().getBody());
        JSONObject headers = (JSONObject) responseJson.get("headers");

        assertEquals("value1", headers.get("Header1"));
        assertEquals("value2", headers.get("Header2"));
        assertEquals("application/json", headers.get("Accept"));
        assertEquals("application/x-www-form-urlencoded", headers.get("Content"));
        assertEquals("GET", responseJson.get("method"));
        assertFalse(responseJson.has("body"));
    }

    @Test
    public void testPost() throws Exception {
        HashMap<String, Object> deepPayload = new HashMap<>();
        deepPayload.put("deep1", "deepValue1");
        deepPayload.put("deep2", "deepValue2");

        HashMap<String, Object> payload = new HashMap<>();
        payload.put("key1", "val1");
        payload.put("key2", "val2");
        payload.put("key3", "val3");
        payload.put("key4", 4);
        payload.put("deepKey", deepPayload);

        model = new RequestModel.Builder(timestampProvider, uuidProvider).url(DENNA_ECHO_URL).method(RequestMethod.POST).headers(headers).payload(payload).build();
        manager.submit(model);
        latch.await();

        assertEquals(0, handler.getOnErrorCount());
        assertEquals(1, handler.getOnSuccessCount());
        assertEquals(200, handler.getSuccessResponseModel().getStatusCode());

        JSONObject responseJson = new JSONObject(handler.getSuccessResponseModel().getBody());
        JSONObject headers = responseJson.getJSONObject("headers");
        JSONObject body = responseJson.getJSONObject("body");

        assertEquals("value1", headers.get("Header1"));
        assertEquals("value2", headers.get("Header2"));
        assertEquals("application/json", headers.get("Accept"));
        assertEquals("application/x-www-form-urlencoded", headers.get("Content"));
        assertEquals("POST", responseJson.get("method"));
        assertEquals("val1", body.get("key1"));
        assertEquals("val2", body.get("key2"));
        assertEquals("val3", body.get("key3"));
        assertEquals(4, body.get("key4"));

        JSONObject soDeepJson = body.getJSONObject("deepKey");
        assertEquals("deepValue1", soDeepJson.getString("deep1"));
        assertEquals("deepValue2", soDeepJson.getString("deep2"));
    }

    @Test
    public void testDelete() throws Exception {
        model = new RequestModel.Builder(timestampProvider, uuidProvider).url(DENNA_ECHO_URL).method(RequestMethod.DELETE).headers(headers).build();
        manager.submit(model);
        latch.await();

        assertEquals(0, handler.getOnErrorCount());
        assertEquals(1, handler.getOnSuccessCount());
        assertEquals(200, handler.getSuccessResponseModel().getStatusCode());

        JSONObject responseJson = new JSONObject(handler.getSuccessResponseModel().getBody());
        JSONObject headers = responseJson.getJSONObject("headers");

        assertEquals("value1", headers.get("Header1"));
        assertEquals("value2", headers.get("Header2"));
        assertEquals("application/json", headers.get("Accept"));
        assertEquals("application/x-www-form-urlencoded", headers.get("Content"));
        assertEquals("DELETE", responseJson.get("method"));
        assertFalse(responseJson.has("body"));
    }

}