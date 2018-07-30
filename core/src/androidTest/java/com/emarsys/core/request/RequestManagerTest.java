package com.emarsys.core.request;

import android.content.Context;
import android.os.Handler;
import android.support.test.InstrumentationRegistry;

import com.emarsys.core.concurrency.CoreSdkHandlerProvider;
import com.emarsys.core.connection.ConnectionWatchDog;
import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.fake.FakeCompletionHandler;
import com.emarsys.core.fake.FakeRunnableFactory;
import com.emarsys.core.provider.uuid.UUIDProvider;
import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.request.model.specification.QueryNewestRequestModel;
import com.emarsys.core.testUtil.ConnectionTestUtils;
import com.emarsys.core.testUtil.DatabaseTestUtils;
import com.emarsys.core.testUtil.TimeoutUtils;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.worker.DefaultWorker;
import com.emarsys.core.worker.Worker;

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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RequestManagerTest {
    private static final String GOOGLE_URL = "https://www.google.com";

    private RequestManager manager;
    private RequestModel model;
    private FakeCompletionHandler handler;
    private CountDownLatch completionHandlerLatch;
    private CountDownLatch runnableFactoryLatch;
    private ConnectionWatchDog connectionWatchDog;
    private CoreSdkHandlerProvider coreSdkHandlerProvider;
    private Handler coreSdkHandler;
    private Repository<RequestModel, SqlSpecification> requestRepository;
    private Worker worker;
    private TimestampProvider timestampProvider;
    private com.emarsys.core.provider.uuid.UUIDProvider UUIDProvider;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    @SuppressWarnings("unchecked")
    public void init() {
        DatabaseTestUtils.INSTANCE.deleteCoreDatabase();

        Context context = InstrumentationRegistry.getTargetContext();
        ConnectionTestUtils.checkConnection(context);

        coreSdkHandlerProvider = new CoreSdkHandlerProvider();
        coreSdkHandler = coreSdkHandlerProvider.provideHandler();

        connectionWatchDog = mock(ConnectionWatchDog.class);
        requestRepository = mock(Repository.class);
        when(requestRepository.isEmpty()).thenReturn(true);

        completionHandlerLatch = new CountDownLatch(1);
        handler = new FakeCompletionHandler(completionHandlerLatch);
        RestClient restClient = new RestClient(mock(Repository.class), mock(TimestampProvider.class));
        worker = new DefaultWorker(requestRepository, connectionWatchDog, coreSdkHandler, handler, restClient);
        manager = new RequestManager(coreSdkHandler, requestRepository, worker);

        timestampProvider = new TimestampProvider();
        UUIDProvider = new UUIDProvider();
        runnableFactoryLatch = new CountDownLatch(1);
        manager.runnableFactory = new FakeRunnableFactory(runnableFactoryLatch);

        Map<String, String> headers = new HashMap<>();
        headers.put("accept", "application/json");
        headers.put("content", "application/x-www-form-urlencoded");

        model = new RequestModel.Builder(timestampProvider, UUIDProvider)
                .url(GOOGLE_URL)
                .method(RequestMethod.GET)
                .headers(headers)
                .build();
    }

    @After
    public void tearDown() {
        coreSdkHandler.getLooper().quit();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_coreSdkHandlerShouldNotBeNull() {
        new RequestManager(null, requestRepository, worker);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_requestRepositoryShouldNotBeNull() {
        new RequestManager(coreSdkHandler, null, worker);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_workerShouldNotBeNull() {
        new RequestManager(coreSdkHandler, requestRepository, null);
    }

    @Test
    public void testInjectDefaultHeaders_withDefaultHeadersSet() {
        Map<String, String> defaultHeaders = new HashMap<>();
        defaultHeaders.put("connection", "close");

        manager.setDefaultHeaders(defaultHeaders);
        manager.injectDefaultHeaders(model);

        Map<String, String> expectedHeaders = new HashMap<>();
        expectedHeaders.put("content", "application/x-www-form-urlencoded");
        expectedHeaders.put("accept", "application/json");
        expectedHeaders.put("connection", "close");

        assertEquals(expectedHeaders, model.getHeaders());
    }

    @Test
    public void testInjectDefaultHeaders_withEmptyDefaultHeaders() {
        manager.injectDefaultHeaders(model);

        Map<String, String> expectedHeaders = new HashMap<>();
        expectedHeaders.put("content", "application/x-www-form-urlencoded");
        expectedHeaders.put("accept", "application/json");

        assertEquals(expectedHeaders, model.getHeaders());
    }

    @Test
    public void testInjectDefaultHeaders_defaultHeadersShouldNotOverrideModelHeaders() {
        Map<String, String> defaultHeaders = new HashMap<>();
        defaultHeaders.put("connection", "close");
        defaultHeaders.put("accept", "magic");

        manager.setDefaultHeaders(defaultHeaders);
        manager.injectDefaultHeaders(model);

        Map<String, String> expectedHeaders = new HashMap<>();
        expectedHeaders.put("content", "application/x-www-form-urlencoded");
        expectedHeaders.put("accept", "application/json");
        expectedHeaders.put("connection", "close");

        assertEquals(expectedHeaders, model.getHeaders());
    }

    @Test
    public void testSubmit_shouldCallInjectDefaultHeaders() throws InterruptedException {
        RequestManager managerSpy = spy(manager);
        managerSpy.submit(model);

        runnableFactoryLatch.await();

        verify(managerSpy).injectDefaultHeaders(model);
    }

    @Test
    public void testSubmit_shouldAddModelToQueue() throws InterruptedException {
        manager.submit(model);

        runnableFactoryLatch.await();

        verify(requestRepository).add(model);
    }

    @Test
    public void testSubmit_shouldInvokeRunOnTheWorker() throws InterruptedException {
        Worker worker = mock(Worker.class);
        manager.worker = worker;

        manager.submit(model);

        runnableFactoryLatch.await();

        verify(worker).run();
    }

    @Test
    public void testSubmit_modelHeadersShouldBeExtendedWithDefaultHeaders() throws InterruptedException {
        Map<String, String> expectedHeaders = new HashMap<>();
        expectedHeaders.put("content", "application/x-www-form-urlencoded");
        expectedHeaders.put("accept", "application/json");
        expectedHeaders.put("connection", "close");

        Map<String, String> defaultHeaders = new HashMap<>();
        defaultHeaders.put("connection", "close");

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        manager.setDefaultHeaders(defaultHeaders);
        manager.submit(model);

        runnableFactoryLatch.await();

        verify(requestRepository).add(captor.capture());
        RequestModel capturedRequestModel = captor.getValue();

        assertEquals(expectedHeaders, capturedRequestModel.getHeaders());
    }

    @Test
    public void testSubmit_executesRunnableOn_CoreSDKHandlerThread() throws InterruptedException {
        FakeRunnableFactory fakeRunnableFactory = new FakeRunnableFactory(runnableFactoryLatch, true);
        manager.runnableFactory = fakeRunnableFactory;

        manager.submit(model);

        runnableFactoryLatch.await();

        assertEquals(1, fakeRunnableFactory.executionCount);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testSubmit_requestModelShouldNotBeNull() {
        manager.submit(null);
    }


    @Test
    @SuppressWarnings("unchecked")
    public void testSuccess() throws Exception {
        when(connectionWatchDog.isConnected()).thenReturn(true, false);
        when(requestRepository.isEmpty()).thenReturn(false, false, true);
        when(requestRepository.query(any(QueryNewestRequestModel.class))).thenReturn(
                Collections.singletonList(model),
                Collections.<RequestModel>emptyList()
        );

        manager.submit(model);

        completionHandlerLatch.await();
        assertEquals(model.getId(), handler.getSuccessId());
        assertEquals(1, handler.getOnSuccessCount());
        assertEquals(0, handler.getOnErrorCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testError_callbackWithResponseModel() throws Exception {
        model = new RequestModel.Builder(timestampProvider, UUIDProvider).url(GOOGLE_URL).method(RequestMethod.POST).build();

        when(connectionWatchDog.isConnected()).thenReturn(true, false);
        when(requestRepository.isEmpty()).thenReturn(false, false, true);
        when(requestRepository.query(any(QueryNewestRequestModel.class))).thenReturn(
                Collections.singletonList(model),
                Collections.<RequestModel>emptyList()
        );

        manager.submit(model);

        completionHandlerLatch.await();

        assertEquals(model.getId(), handler.getErrorId());
        assertEquals(0, handler.getOnSuccessCount());
        assertEquals(1, handler.getOnErrorCount());
        assertEquals(405, handler.getFailureResponseModel().getStatusCode());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testError_callbackWithException() throws Exception {
        model = new RequestModel.Builder(timestampProvider, UUIDProvider).url("https://www.nosuchwebsite.emarsys.com").method(RequestMethod.GET).build();

        when(connectionWatchDog.isConnected()).thenReturn(true, false);
        when(requestRepository.isEmpty()).thenReturn(false, false, true);
        when(requestRepository.query(any(QueryNewestRequestModel.class))).thenReturn(
                Collections.singletonList(model),
                Collections.<RequestModel>emptyList()
        );

        manager.submit(model);

        completionHandlerLatch.await();

        assertEquals(model.getId(), handler.getErrorId());
        assertEquals(0, handler.getOnSuccessCount());
        assertEquals(1, handler.getOnErrorCount());
        assertEquals(((Exception) new UnknownHostException()).getClass(), handler.getException().getClass());
    }
}