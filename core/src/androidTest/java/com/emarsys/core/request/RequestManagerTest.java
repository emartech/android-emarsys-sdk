package com.emarsys.core.request;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.Registry;
import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.concurrency.CoreSdkHandlerProvider;
import com.emarsys.core.connection.ConnectionProvider;
import com.emarsys.core.connection.ConnectionWatchDog;
import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.fake.FakeCompletionHandler;
import com.emarsys.core.fake.FakeRunnableFactory;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.provider.uuid.UUIDProvider;
import com.emarsys.core.request.factory.CompletionProxyFactory;
import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.request.model.specification.QueryLatestRequestModel;
import com.emarsys.core.shard.ShardModel;
import com.emarsys.core.worker.DefaultWorker;
import com.emarsys.core.worker.Worker;
import com.emarsys.testUtil.ConnectionTestUtils;
import com.emarsys.testUtil.DatabaseTestUtils;
import com.emarsys.testUtil.InstrumentationRegistry;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.ArgumentCaptor;

import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static com.emarsys.testUtil.TestUrls.customResponse;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RequestManagerTest {
    private static final String GOOGLE_URL = "https://www.google.com";

    private RequestManager manager;
    private RequestModel requestModel;
    private ShardModel shardModel;
    private FakeCompletionHandler handler;
    private CoreCompletionHandler mockDefaultHandler;
    private CountDownLatch completionHandlerLatch;
    private CountDownLatch runnableFactoryLatch;
    private ConnectionWatchDog connectionWatchDog;
    private CoreSdkHandlerProvider coreSdkHandlerProvider;
    private Handler coreSdkHandler;
    private Handler uiHandler;
    private Repository<RequestModel, SqlSpecification> requestRepository;
    private Repository<ShardModel, SqlSpecification> shardRepository;
    private Worker worker;
    private TimestampProvider timestampProvider;
    private UUIDProvider uuidProvider;
    private RestClient restClientMock;
    private Registry<RequestModel, CompletionListener> callbackRegistry;
    private CompletionProxyFactory completionProxyFactory;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    @SuppressWarnings("unchecked")
    public void init() {
        DatabaseTestUtils.deleteCoreDatabase();

        Context context = InstrumentationRegistry.getTargetContext();
        ConnectionTestUtils.checkConnection(context);

        coreSdkHandlerProvider = new CoreSdkHandlerProvider();
        coreSdkHandler = coreSdkHandlerProvider.provideHandler();

        uiHandler = new Handler(Looper.getMainLooper());

        connectionWatchDog = mock(ConnectionWatchDog.class);

        requestRepository = mock(Repository.class);
        when(requestRepository.isEmpty()).thenReturn(true);

        shardRepository = mock(Repository.class);

        callbackRegistry = mock(Registry.class);

        completionHandlerLatch = new CountDownLatch(1);
        handler = new FakeCompletionHandler(completionHandlerLatch);
        mockDefaultHandler = mock(CoreCompletionHandler.class);
        RestClient restClient = new RestClient(new ConnectionProvider(), mock(TimestampProvider.class));
        restClientMock = mock(RestClient.class);
        completionProxyFactory = new CompletionProxyFactory(requestRepository, uiHandler, coreSdkHandler, mockDefaultHandler);
        worker = new DefaultWorker(requestRepository, connectionWatchDog, uiHandler, handler, restClient, completionProxyFactory);
        manager = new RequestManager(
                coreSdkHandler,
                requestRepository,
                shardRepository,
                worker,
                restClientMock,
                callbackRegistry,
                completionProxyFactory);

        timestampProvider = new TimestampProvider();
        uuidProvider = new UUIDProvider();
        runnableFactoryLatch = new CountDownLatch(1);
        manager.runnableFactory = new FakeRunnableFactory(runnableFactoryLatch);

        Map<String, String> headers = new HashMap<>();
        headers.put("accept", "application/json");
        headers.put("content", "application/x-www-form-urlencoded");

        requestModel = new RequestModel.Builder(timestampProvider, uuidProvider)
                .url(GOOGLE_URL)
                .method(RequestMethod.GET)
                .headers(headers)
                .build();

        shardModel = new ShardModel(
                "shard_id",
                "shard_type",
                new HashMap<String, Object>(),
                0,
                Long.MAX_VALUE);
    }

    @After
    public void tearDown() {
        coreSdkHandler.getLooper().quit();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_coreSdkHandlerShouldNotBeNull() {
        new RequestManager(null, requestRepository, shardRepository, worker, restClientMock, callbackRegistry, completionProxyFactory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_requestRepositoryShouldNotBeNull() {
        new RequestManager(coreSdkHandler, null, shardRepository, worker, restClientMock, callbackRegistry, completionProxyFactory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_shardRepositoryShouldNotBeNull() {
        new RequestManager(coreSdkHandler, requestRepository, null, worker, restClientMock, callbackRegistry, completionProxyFactory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_workerShouldNotBeNull() {
        new RequestManager(coreSdkHandler, requestRepository, shardRepository, null, restClientMock, callbackRegistry, completionProxyFactory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_restClientShouldNotBeNull() {
        new RequestManager(coreSdkHandler, requestRepository, shardRepository, worker, null, callbackRegistry, completionProxyFactory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_registryShouldNotBeNull() {
        new RequestManager(coreSdkHandler, requestRepository, shardRepository, worker, restClientMock, null, completionProxyFactory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_completionProxyFactory_mustNotBeNull() {
        new RequestManager(coreSdkHandler, requestRepository, shardRepository, worker, restClientMock, callbackRegistry, null);
    }

    @Test
    public void testInjectDefaultHeaders_withDefaultHeadersSet() {
        Map<String, String> defaultHeaders = new HashMap<>();
        defaultHeaders.put("connection", "close");

        manager.setDefaultHeaders(defaultHeaders);
        manager.injectDefaultHeaders(requestModel);

        Map<String, String> expectedHeaders = new HashMap<>();
        expectedHeaders.put("content", "application/x-www-form-urlencoded");
        expectedHeaders.put("accept", "application/json");
        expectedHeaders.put("connection", "close");

        assertEquals(expectedHeaders, requestModel.getHeaders());
    }

    @Test
    public void testInjectDefaultHeaders_withEmptyDefaultHeaders() {
        manager.injectDefaultHeaders(requestModel);

        Map<String, String> expectedHeaders = new HashMap<>();
        expectedHeaders.put("content", "application/x-www-form-urlencoded");
        expectedHeaders.put("accept", "application/json");

        assertEquals(expectedHeaders, requestModel.getHeaders());
    }

    @Test
    public void testInjectDefaultHeaders_defaultHeadersShouldNotOverrideModelHeaders() {
        Map<String, String> defaultHeaders = new HashMap<>();
        defaultHeaders.put("connection", "close");
        defaultHeaders.put("accept", "magic");

        manager.setDefaultHeaders(defaultHeaders);
        manager.injectDefaultHeaders(requestModel);

        Map<String, String> expectedHeaders = new HashMap<>();
        expectedHeaders.put("content", "application/x-www-form-urlencoded");
        expectedHeaders.put("accept", "application/json");
        expectedHeaders.put("connection", "close");

        assertEquals(expectedHeaders, requestModel.getHeaders());
    }

    @Test
    public void testSubmit_withRequestModel_shouldCallInjectDefaultHeaders() throws InterruptedException {
        RequestManager managerSpy = spy(manager);
        managerSpy.submit(requestModel, null);

        runnableFactoryLatch.await();

        verify(managerSpy).injectDefaultHeaders(requestModel);
    }

    @Test
    public void testSubmit_shouldAddRequestModelToQueue() throws InterruptedException {
        manager.submit(requestModel, null);

        runnableFactoryLatch.await();

        verify(requestRepository).add(requestModel);
    }

    @Test
    public void testSubmit_withRequestModel_shouldInvokeRunOnTheWorker() throws InterruptedException {
        Worker worker = mock(Worker.class);
        manager.worker = worker;

        manager.submit(requestModel, null);

        runnableFactoryLatch.await();

        verify(worker).run();
    }

    @Test
    public void testSubmit_requestModelHeadersShouldBeExtendedWithDefaultHeaders() throws InterruptedException {
        Map<String, String> expectedHeaders = new HashMap<>();
        expectedHeaders.put("content", "application/x-www-form-urlencoded");
        expectedHeaders.put("accept", "application/json");
        expectedHeaders.put("connection", "close");

        Map<String, String> defaultHeaders = new HashMap<>();
        defaultHeaders.put("connection", "close");

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        manager.setDefaultHeaders(defaultHeaders);
        manager.submit(requestModel, null);

        runnableFactoryLatch.await();

        verify(requestRepository).add(captor.capture());
        RequestModel capturedRequestModel = captor.getValue();

        assertEquals(expectedHeaders, capturedRequestModel.getHeaders());
    }

    @Test
    public void testSubmit_withRequestModel_executesRunnableOn_CoreSDKHandlerThread() throws InterruptedException {
        FakeRunnableFactory fakeRunnableFactory = new FakeRunnableFactory(runnableFactoryLatch, true);
        manager.runnableFactory = fakeRunnableFactory;

        manager.submit(requestModel, null);

        runnableFactoryLatch.await();

        assertEquals(1, fakeRunnableFactory.executionCount);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testSubmit_requestModelShouldNotBeNull() {
        manager.submit((RequestModel) null, null);
    }


    @Test
    @SuppressWarnings("unchecked")
    public void testSubmit_withRequestModel_Success() throws Exception {
        when(connectionWatchDog.isConnected()).thenReturn(true, false);
        when(requestRepository.isEmpty()).thenReturn(false, false, true);
        when(requestRepository.query(any(QueryLatestRequestModel.class))).thenReturn(
                Collections.singletonList(requestModel),
                Collections.<RequestModel>emptyList()
        );

        manager.submit(requestModel, null);

        completionHandlerLatch.await();
        assertEquals(requestModel.getId(), handler.getSuccessId());
        assertEquals(1, handler.getOnSuccessCount());
        assertEquals(0, handler.getOnErrorCount());
    }

    @Test
    public void testSubmit_withRequestModel_shouldRegisterCallbackToRegistry() throws InterruptedException {
        CompletionListener completionListener = mock(CompletionListener.class);

        manager.submit(requestModel, completionListener);
        runnableFactoryLatch.await();

        verify(callbackRegistry).register(requestModel, completionListener);
    }

    @Test
    public void testSubmit_withRequestModel_shouldRegister_null_ToRegistryAsWell() throws InterruptedException {
        manager.submit(requestModel, null);
        runnableFactoryLatch.await();

        verify(callbackRegistry).register(requestModel, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubmitNow_requestModel_mustNotBeNull() {
        manager.submitNow(null, handler);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubmitNow_completionHandler_mustNotBeNull() {
        manager.submitNow(requestModel, null);
    }

    @Test
    public void testSubmitNow_shouldCallRestClientsExecuteWithGivenParameters() {
        manager.submitNow(requestModel, handler);

        verify(restClientMock).execute(requestModel, handler);
    }

    @Test
    public void testSubmitNow_shouldCallRestClient_withDefaultHandler() {
        manager.submitNow(requestModel);

        verify(restClientMock).execute(requestModel, mockDefaultHandler);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testError_callbackWithResponseContainsRequestModel() throws Exception {
        requestModel = new RequestModel.Builder(timestampProvider, uuidProvider).url(customResponse(405)).method(RequestMethod.GET).build();

        when(connectionWatchDog.isConnected()).thenReturn(true, false);
        when(requestRepository.isEmpty()).thenReturn(false, false, true);
        when(requestRepository.query(any(QueryLatestRequestModel.class))).thenReturn(
                Collections.singletonList(requestModel),
                Collections.<RequestModel>emptyList()
        );

        manager.submit(requestModel, null);

        completionHandlerLatch.await();

        assertEquals(requestModel.getId(), handler.getErrorId());
        assertEquals(0, handler.getOnSuccessCount());
        assertEquals(1, handler.getOnErrorCount());
        assertEquals(405, handler.getFailureResponseModel().getStatusCode());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testError_withRequestModel_callbackWithException() throws Exception {
        requestModel = new RequestModel.Builder(timestampProvider, uuidProvider).url("https://www.nosuchwebsite.emarsys.com").method(RequestMethod.GET).build();

        when(connectionWatchDog.isConnected()).thenReturn(true, false);
        when(requestRepository.isEmpty()).thenReturn(false, false, true);
        when(requestRepository.query(any(QueryLatestRequestModel.class))).thenReturn(
                Collections.singletonList(requestModel),
                Collections.<RequestModel>emptyList()
        );

        manager.submit(requestModel, null);

        completionHandlerLatch.await();

        assertEquals(requestModel.getId(), handler.getErrorId());
        assertEquals(0, handler.getOnSuccessCount());
        assertEquals(1, handler.getOnErrorCount());
        assertEquals(((Exception) new UnknownHostException()).getClass(), handler.getException().getClass());
    }

    @Test(expected = IllegalArgumentException.class)
    @SuppressWarnings("ConstantConditions")
    public void testSubmit_shardModelShouldNotBeNull() {
        manager.submit((ShardModel) null);
    }

    @Test
    public void testSubmit_shouldAddShardModelToDatabase() throws InterruptedException {
        manager.submit(shardModel);

        runnableFactoryLatch.await();

        verify(shardRepository).add(shardModel);
    }


    @Test
    public void testSubmit_withShardModel_executesRunnableOn_CoreSDKHandlerThread() throws InterruptedException {
        FakeRunnableFactory fakeRunnableFactory = new FakeRunnableFactory(runnableFactoryLatch, true);
        manager.runnableFactory = fakeRunnableFactory;

        manager.submit(shardModel);

        runnableFactoryLatch.await();

        assertEquals(1, fakeRunnableFactory.executionCount);
    }

}