package com.emarsys.core.request;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.emarsys.core.Mapper;
import com.emarsys.core.Registry;
import com.emarsys.core.concurrency.CoreSdkHandlerProvider;
import com.emarsys.core.connection.ConnectionProvider;
import com.emarsys.core.connection.ConnectionWatchDog;
import com.emarsys.core.database.helper.CoreDbHelper;
import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.database.trigger.TriggerKey;
import com.emarsys.core.fake.FakeCompletionHandler;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.provider.uuid.UUIDProvider;
import com.emarsys.core.request.factory.CoreCompletionHandlerMiddlewareProvider;
import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.request.model.RequestModelRepository;
import com.emarsys.core.response.ResponseHandlersProcessor;
import com.emarsys.core.shard.ShardModel;
import com.emarsys.core.shard.ShardModelRepository;
import com.emarsys.core.worker.DefaultWorker;
import com.emarsys.core.worker.Worker;
import com.emarsys.testUtil.ConnectionTestUtils;
import com.emarsys.testUtil.DatabaseTestUtils;
import com.emarsys.testUtil.InstrumentationRegistry;
import com.emarsys.testUtil.TimeoutUtils;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static com.emarsys.testUtil.TestUrls.DENNA_ECHO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestManagerDennaTest {

    private RequestManager manager;
    private Map<String, String> headers;
    private RequestModel model;
    private CountDownLatch latch;
    private FakeCompletionHandler fakeCompletionHandler;
    private CoreSdkHandlerProvider provider;
    private Handler coreSdkHandler;
    private Handler uiHandler;
    private Worker worker;
    private TimestampProvider timestampProvider;
    private UUIDProvider uuidProvider;
    private CoreCompletionHandlerMiddlewareProvider coreCompletionHandlerMiddlewareProvider;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();
    private Mapper<RequestModel, RequestModel> mockRequestModelMapper;

    @Before
    @SuppressWarnings("unchecked")
    public void init() {
        DatabaseTestUtils.deleteCoreDatabase();

        List<Mapper<RequestModel, RequestModel>> requestModelMappers = new ArrayList<>();
        mockRequestModelMapper = mock(Mapper.class);

        when(mockRequestModelMapper.map(any(RequestModel.class))).thenAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                return args[0];
            }
        });

        requestModelMappers.add(mockRequestModelMapper);

        Context context = InstrumentationRegistry.getTargetContext();
        ConnectionTestUtils.checkConnection(context);

        provider = new CoreSdkHandlerProvider();
        coreSdkHandler = provider.provideHandler();
        uiHandler = new Handler(Looper.getMainLooper());

        ConnectionWatchDog connectionWatchDog = new ConnectionWatchDog(context, coreSdkHandler);
        CoreDbHelper coreDbHelper = new CoreDbHelper(context, new HashMap<TriggerKey, List<Runnable>>());
        Repository<RequestModel, SqlSpecification> requestRepository = new RequestModelRepository(coreDbHelper);
        Repository<ShardModel, SqlSpecification> shardRepository = new ShardModelRepository(coreDbHelper);
        latch = new CountDownLatch(1);
        fakeCompletionHandler = new FakeCompletionHandler(latch);
        RestClient restClient = new RestClient(new ConnectionProvider(), mock(TimestampProvider.class), mock(ResponseHandlersProcessor.class), requestModelMappers);
        coreCompletionHandlerMiddlewareProvider = new CoreCompletionHandlerMiddlewareProvider(fakeCompletionHandler, requestRepository, uiHandler, coreSdkHandler);
        worker = new DefaultWorker(requestRepository, connectionWatchDog, uiHandler, fakeCompletionHandler, restClient, coreCompletionHandlerMiddlewareProvider);
        timestampProvider = new TimestampProvider();
        uuidProvider = new UUIDProvider();
        manager = new RequestManager(
                coreSdkHandler,
                requestRepository,
                shardRepository,
                worker,
                restClient,
                mock(Registry.class),
                fakeCompletionHandler);
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
        model = new RequestModel.Builder(timestampProvider, uuidProvider).url(DENNA_ECHO).method(RequestMethod.GET).headers(headers).build();
        manager.submit(model, null);
        latch.await();

        assertEquals(null, fakeCompletionHandler.getException());
        assertEquals(0, fakeCompletionHandler.getOnErrorCount());
        assertEquals(1, fakeCompletionHandler.getOnSuccessCount());
        assertEquals(200, fakeCompletionHandler.getSuccessResponseModel().getStatusCode());

        JSONObject responseJson = new JSONObject(fakeCompletionHandler.getSuccessResponseModel().getBody());
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

        model = new RequestModel.Builder(timestampProvider, uuidProvider).url(DENNA_ECHO).method(RequestMethod.POST).headers(headers).payload(payload).build();
        manager.submit(model, null);
        latch.await();

        assertEquals(null, fakeCompletionHandler.getException());
        assertEquals(0, fakeCompletionHandler.getOnErrorCount());
        assertEquals(1, fakeCompletionHandler.getOnSuccessCount());
        assertEquals(200, fakeCompletionHandler.getSuccessResponseModel().getStatusCode());

        JSONObject responseJson = new JSONObject(fakeCompletionHandler.getSuccessResponseModel().getBody());
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
    public void testPut() throws Exception {
        model = new RequestModel.Builder(timestampProvider, uuidProvider).url(DENNA_ECHO).method(RequestMethod.PUT).headers(headers).build();
        manager.submit(model, null);
        latch.await();

        assertEquals(null, fakeCompletionHandler.getException());
        assertEquals(0, fakeCompletionHandler.getOnErrorCount());
        assertEquals(1, fakeCompletionHandler.getOnSuccessCount());
        assertEquals(200, fakeCompletionHandler.getSuccessResponseModel().getStatusCode());

        JSONObject responseJson = new JSONObject(fakeCompletionHandler.getSuccessResponseModel().getBody());
        JSONObject headers = responseJson.getJSONObject("headers");

        assertEquals("value1", headers.get("Header1"));
        assertEquals("value2", headers.get("Header2"));
        assertEquals("application/json", headers.get("Accept"));
        assertEquals("application/x-www-form-urlencoded", headers.get("Content"));
        assertEquals("PUT", responseJson.get("method"));
        assertFalse(responseJson.has("body"));
    }

    @Test
    public void testDelete() throws Exception {
        model = new RequestModel.Builder(timestampProvider, uuidProvider).url(DENNA_ECHO).method(RequestMethod.DELETE).headers(headers).build();
        manager.submit(model, null);
        latch.await();

        assertEquals(null, fakeCompletionHandler.getException());
        assertEquals(0, fakeCompletionHandler.getOnErrorCount());
        assertEquals(1, fakeCompletionHandler.getOnSuccessCount());
        assertEquals(200, fakeCompletionHandler.getSuccessResponseModel().getStatusCode());

        JSONObject responseJson = new JSONObject(fakeCompletionHandler.getSuccessResponseModel().getBody());
        JSONObject headers = responseJson.getJSONObject("headers");

        assertEquals("value1", headers.get("Header1"));
        assertEquals("value2", headers.get("Header2"));
        assertEquals("application/json", headers.get("Accept"));
        assertEquals("application/x-www-form-urlencoded", headers.get("Content"));
        assertEquals("DELETE", responseJson.get("method"));
        assertFalse(responseJson.has("body"));
    }

}