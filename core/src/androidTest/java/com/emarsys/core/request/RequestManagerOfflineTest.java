package com.emarsys.core.request;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.emarsys.core.concurrency.CoreSdkHandlerProvider;
import com.emarsys.core.connection.ConnectionState;
import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.fake.FakeCompletionHandler;
import com.emarsys.core.fake.FakeConnectionWatchDog;
import com.emarsys.core.fake.FakeRestClient;
import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.request.model.RequestModelRepository;
import com.emarsys.core.request.model.specification.FilterByRequestId;
import com.emarsys.core.testUtil.ConnectionTestUtils;
import com.emarsys.core.testUtil.DatabaseTestUtils;
import com.emarsys.core.testUtil.TimeoutUtils;
import com.emarsys.core.timestamp.TimestampProvider;
import com.emarsys.core.worker.DefaultWorker;
import com.emarsys.core.worker.Worker;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(AndroidJUnit4.class)
public class RequestManagerOfflineTest {

    public static final String URL = "https://www.google.com";

    private Handler handler;

    private Boolean[] connectionStates;
    private Object[] requestResults;
    private RequestModel[] requestModels;
    private int watchDogCountDown;
    private int completionHandlerCountDown;

    private CountDownLatch watchDogLatch;
    private Context context;
    private FakeConnectionWatchDog watchDog;
    private Repository<RequestModel, SqlSpecification> requestRepository;
    private CountDownLatch completionLatch;
    private FakeCompletionHandler completionHandler;
    private RequestManager manager;
    private RestClient client;
    private CoreSdkHandlerProvider provider;
    private Handler coreSdkHandler;
    private Worker worker;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void setup() {
        DatabaseTestUtils.INSTANCE.deleteCoreDatabase();

        handler = new Handler(Looper.getMainLooper());
    }

    @After
    public void tearDown() {
        coreSdkHandler.getLooper().quit();
    }

    @Test
    public void test_online_offline_online() throws InterruptedException {
        connectionStates = new Boolean[]{true, false, true};
        requestResults = new Object[]{200, 200};
        requestModels = new RequestModel[]{normal(), normal()};
        watchDogCountDown = 2;
        completionHandlerCountDown = 1;

        prepareTestCaseAndWait();

        assertFalse(requestRepository.isEmpty());
        completionHandler.latch = new CountDownLatch(1);

        handler.post(new Runnable() {
            @Override
            public void run() {
                watchDog.connectionChangeListener.onConnectionChanged(ConnectionState.CONNECTED, true);
            }
        });

        completionHandler.latch.await();

        assertEquals(2, completionHandler.getOnSuccessCount());

        assertTrue(requestRepository.isEmpty());
    }

    @Test
    public void test_alwaysOnline() throws InterruptedException {
        connectionStates = new Boolean[]{true};
        requestResults = new Object[]{200, 200, 200};
        requestModels = new RequestModel[]{normal(), normal(), normal()};
        watchDogCountDown = 3;
        completionHandlerCountDown = 3;

        prepareTestCaseAndWait();

        assertEquals(3, completionHandler.getOnSuccessCount());
        assertTrue(requestRepository.isEmpty());
    }

    @Test
    public void test_alwaysOnline_withExpiredRequests() throws InterruptedException {
        connectionStates = new Boolean[]{true};
        requestResults = new Object[]{200, 200, 200};
        requestModels = new RequestModel[]{
                normal(),
                expired(),
                normal(),
                expired(),
                expired(),
                expired(),
                normal()
        };
        watchDogCountDown = 3;
        completionHandlerCountDown = 7;

        prepareTestCaseAndWait();

        assertEquals(3, completionHandler.getOnSuccessCount());
        assertEquals(4, completionHandler.getOnErrorCount());
        assertTrue(requestRepository.isEmpty());
    }

    @Test
    public void test_alwaysOffline() throws InterruptedException {
        connectionStates = new Boolean[]{false};
        requestResults = new Object[]{200, 300, 200};

        RequestModel normal1 = normal();
        RequestModel normal2 = normal();
        RequestModel normal3 = normal();
        requestModels = new RequestModel[]{normal1, normal2, normal3};
        watchDogCountDown = 1;
        completionHandlerCountDown = 0;

        prepareTestCaseAndWait();

        assertEquals(0, completionHandler.getOnSuccessCount());
        assertEquals(0, completionHandler.getOnErrorCount());
        assertTrue(!requestRepository.isEmpty());

        for (int i = 0; i < requestModels.length; ++i) {
            requestRepository.remove(new FilterByRequestId(normal1));
            requestRepository.remove(new FilterByRequestId(normal2));
            requestRepository.remove(new FilterByRequestId(normal3));
        }
        assertTrue(requestRepository.isEmpty());
    }

    @Test
    public void test_4xx_doesNotStopQueue() throws InterruptedException {
        connectionStates = new Boolean[]{true};
        requestResults = new Object[]{200, 400, 200};
        requestModels = new RequestModel[]{normal(), normal(), normal()};
        watchDogCountDown = 3;
        completionHandlerCountDown = 3;

        prepareTestCaseAndWait();

        assertEquals(2, completionHandler.getOnSuccessCount());
        assertEquals(1, completionHandler.getOnErrorCount());
        assertTrue(requestRepository.isEmpty());
    }

    @Test
    public void test_408_stopsQueue() throws InterruptedException {
        connectionStates = new Boolean[]{true};
        requestResults = new Object[]{200, 408, 200};
        requestModels = new RequestModel[]{normal(), normal(), normal()};
        watchDogCountDown = 2;
        completionHandlerCountDown = 1;

        prepareTestCaseAndWait();

        assertEquals(1, completionHandler.getOnSuccessCount());
        assertEquals(0, completionHandler.getOnErrorCount());
        assertTrue(!requestRepository.isEmpty());
    }

    @Test
    public void test_5xx_stopsQueue() throws InterruptedException {
        connectionStates = new Boolean[]{true};
        requestResults = new Object[]{200, 500, 200};
        requestModels = new RequestModel[]{normal(), normal(), normal()};
        watchDogCountDown = 2;
        completionHandlerCountDown = 1;

        prepareTestCaseAndWait();

        assertEquals(1, completionHandler.getOnSuccessCount());
        assertEquals(0, completionHandler.getOnErrorCount());
        assertTrue(!requestRepository.isEmpty());
    }

    @Test
    public void test_exception_stopsQueue() throws InterruptedException {
        connectionStates = new Boolean[]{true};
        requestResults = new Object[]{200, 300, 200, new IOException()};
        RequestModel lastNormal = normal();
        requestModels = new RequestModel[]{normal(), normal(), normal(), lastNormal};
        watchDogCountDown = 4;
        completionHandlerCountDown = 3;

        prepareTestCaseAndWait();

        assertEquals(3, completionHandler.getOnSuccessCount());
        assertEquals(0, completionHandler.getOnErrorCount());
        assertTrue(!requestRepository.isEmpty());
        requestRepository.remove(new FilterByRequestId(lastNormal));
        assertTrue(requestRepository.isEmpty());
    }

    private void prepareTestCaseAndWait() throws InterruptedException {
        watchDogLatch = new CountDownLatch(watchDogCountDown);
        context = ConnectionTestUtils.getContextMock_withAppContext_withConnectivityManager(false, 0);
        watchDog = new FakeConnectionWatchDog(watchDogLatch, connectionStates);

        requestRepository = new RequestModelRepository(InstrumentationRegistry.getTargetContext());

        completionLatch = new CountDownLatch(completionHandlerCountDown);
        completionHandler = new FakeCompletionHandler(completionLatch);

        client = new FakeRestClient(requestResults);

        provider = new CoreSdkHandlerProvider();
        coreSdkHandler = provider.provideHandler();

        RestClient restClient = new RestClient(mock(Repository.class), mock(TimestampProvider.class));
        worker = new DefaultWorker(requestRepository, watchDog, coreSdkHandler, completionHandler, restClient);

        manager = new RequestManager(coreSdkHandler, requestRepository, worker);
        manager.worker = new DefaultWorker(requestRepository, watchDog, manager.coreSDKHandler, completionHandler, client);

        handler.post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < requestModels.length; ++i) {
                    manager.submit(requestModels[i]);
                }
            }
        });

        watchDogLatch.await();
        completionLatch.await();
    }

    private RequestModel normal() {
        TimestampProvider timestampProvider = new TimestampProvider();
        RequestIdProvider requestIdProvider = new RequestIdProvider();
        return new RequestModel.Builder(timestampProvider, requestIdProvider).url(URL).method(RequestMethod.GET).ttl(60_000).build();
    }

    private RequestModel expired() {
        return new RequestModel(
                URL,
                RequestMethod.GET,
                new HashMap<String, Object>(),
                new HashMap<String, String>(),
                System.currentTimeMillis() - 5000,
                100,
                new RequestIdProvider().provideId());
    }
}
