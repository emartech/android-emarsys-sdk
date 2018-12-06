package com.emarsys.core.request;

import android.os.Handler;
import android.os.Looper;

import com.emarsys.core.Registry;
import com.emarsys.core.concurrency.CoreSdkHandlerProvider;
import com.emarsys.core.connection.ConnectionState;
import com.emarsys.core.database.helper.CoreDbHelper;
import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.database.repository.specification.Everything;
import com.emarsys.core.database.trigger.TriggerKey;
import com.emarsys.core.fake.FakeCompletionHandler;
import com.emarsys.core.fake.FakeConnectionWatchDog;
import com.emarsys.core.fake.FakeRestClient;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.provider.uuid.UUIDProvider;
import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.request.model.RequestModelRepository;
import com.emarsys.core.request.model.specification.FilterByRequestId;
import com.emarsys.core.shard.ShardModel;
import com.emarsys.core.shard.ShardModelRepository;
import com.emarsys.core.worker.DefaultWorker;
import com.emarsys.core.worker.Worker;
import com.emarsys.testUtil.DatabaseTestUtils;
import com.emarsys.testUtil.InstrumentationRegistry;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.mockito.Mockito.mock;

@RunWith(AndroidJUnit4.class)
public class RequestManagerOfflineTest {

    public static final String URL = "https://www.host.com/";

    private Boolean[] connectionStates;
    private Object[] requestResults;
    private RequestModel[] requestModels;
    private int watchDogCountDown;
    private int completionHandlerCountDown;

    private CountDownLatch watchDogLatch;
    private FakeConnectionWatchDog watchDog;
    private Repository<RequestModel, SqlSpecification> requestRepository;
    private Repository<ShardModel, SqlSpecification> shardRepository;
    private CountDownLatch completionLatch;
    private FakeCompletionHandler completionHandler;
    private RequestManager manager;
    private RestClient fakeRestClient;
    private CoreSdkHandlerProvider provider;
    private Handler coreSdkHandler;
    private Handler uiHandler;
    private Worker worker;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void setup() {
        DatabaseTestUtils.deleteCoreDatabase();

        uiHandler = new Handler(Looper.getMainLooper());
    }

    @After
    public void tearDown() {
        coreSdkHandler.getLooper().quit();
        DatabaseTestUtils.deleteCoreDatabase();
    }

    @Test
    public void test_online_offline_online() throws InterruptedException {
        connectionStates = new Boolean[]{true, false, true};
        requestResults = new Object[]{200, 200};
        requestModels = new RequestModel[]{normal(1), normal(2)};
        watchDogCountDown = 2;
        completionHandlerCountDown = 1;

        prepareTestCaseAndWait();

        assertFalse(requestRepository.isEmpty());
        completionHandler.latch = new CountDownLatch(1);

        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                watchDog.connectionChangeListener.onConnectionChanged(ConnectionState.CONNECTED, true);
            }
        });

        completionHandler.latch.await();

        assertEquals(2, completionHandler.getOnSuccessCount());
        assertRequestTableEmpty();
    }

    @Test
    public void test_alwaysOnline() throws InterruptedException {
        connectionStates = new Boolean[]{true};
        requestResults = new Object[]{200, 200, 200};
        requestModels = new RequestModel[]{normal(1), normal(2), normal(3)};
        watchDogCountDown = 3;
        completionHandlerCountDown = 3;

        prepareTestCaseAndWait();

        assertEquals(3, completionHandler.getOnSuccessCount());
        assertRequestTableEmpty();
    }

    @Test
    public void test_alwaysOnline_withExpiredRequests() throws InterruptedException {
        connectionStates = new Boolean[]{true};
        requestResults = new Object[]{200, 200, 200};
        requestModels = new RequestModel[]{
                normal(1),
                expired(1),
                normal(2),
                expired(2),
                expired(3),
                expired(4),
                normal(3)
        };
        watchDogCountDown = 3;
        completionHandlerCountDown = 7;

        prepareTestCaseAndWait();

        assertEquals(3, completionHandler.getOnSuccessCount());
        assertEquals(4, completionHandler.getOnErrorCount());
        assertRequestTableEmpty();
    }

    @Test
    public void test_alwaysOffline() throws InterruptedException {
        connectionStates = new Boolean[]{false};
        requestResults = new Object[]{200, 300, 200};

        RequestModel normal1 = normal(1);
        RequestModel normal2 = normal(2);
        RequestModel normal3 = normal(3);
        requestModels = new RequestModel[]{normal1, normal2, normal3};
        watchDogCountDown = 1;
        completionHandlerCountDown = 0;

        prepareTestCaseAndWait();

        assertEquals(0, completionHandler.getOnSuccessCount());
        assertEquals(0, completionHandler.getOnErrorCount());
        assertFalse(requestRepository.isEmpty());

        for (RequestModel model : requestModels) {
            requestRepository.remove(new FilterByRequestId(model));
        }
        assertRequestTableEmpty();
    }

    @Test
    public void test_4xx_doesNotStopQueue() throws InterruptedException {
        connectionStates = new Boolean[]{true};
        requestResults = new Object[]{200, 400, 200};
        requestModels = new RequestModel[]{normal(1), normal(2), normal(3)};
        watchDogCountDown = 3;
        completionHandlerCountDown = 3;

        prepareTestCaseAndWait();

        assertEquals(2, completionHandler.getOnSuccessCount());
        assertEquals(1, completionHandler.getOnErrorCount());
        assertRequestTableEmpty();
    }

    @Test
    public void test_408_stopsQueue() throws InterruptedException {
        connectionStates = new Boolean[]{true};
        requestResults = new Object[]{200, 408, 200};
        requestModels = new RequestModel[]{normal(1), normal(2), normal(3)};
        watchDogCountDown = 2;
        completionHandlerCountDown = 1;

        prepareTestCaseAndWait();

        assertEquals(1, completionHandler.getOnSuccessCount());
        assertEquals(0, completionHandler.getOnErrorCount());
        assertFalse(requestRepository.isEmpty());
    }

    @Test
    public void test_5xx_stopsQueue() throws InterruptedException {
        connectionStates = new Boolean[]{true};
        requestResults = new Object[]{200, 500, 200};
        requestModels = new RequestModel[]{normal(1), normal(2), normal(3)};
        watchDogCountDown = 2;
        completionHandlerCountDown = 1;

        prepareTestCaseAndWait();

        assertEquals(1, completionHandler.getOnSuccessCount());
        assertEquals(0, completionHandler.getOnErrorCount());
        assertFalse(requestRepository.isEmpty());
    }

    @Test
    public void test_exception_stopsQueue() throws InterruptedException {
        connectionStates = new Boolean[]{true};
        requestResults = new Object[]{200, 300, 200, new IOException()};
        RequestModel lastNormal = normal(4);
        requestModels = new RequestModel[]{normal(1), normal(2), normal(3), lastNormal};
        watchDogCountDown = 4;
        completionHandlerCountDown = 3;

        prepareTestCaseAndWait();

        assertEquals(3, completionHandler.getOnSuccessCount());
        assertEquals(0, completionHandler.getOnErrorCount());
        assertFalse(requestRepository.isEmpty());
        requestRepository.remove(new FilterByRequestId(lastNormal));
        assertRequestTableEmpty();
    }

    @SuppressWarnings("unchecked")
    private void prepareTestCaseAndWait() throws InterruptedException {
        watchDogLatch = new CountDownLatch(watchDogCountDown);
        watchDog = new FakeConnectionWatchDog(watchDogLatch, connectionStates);

        CoreDbHelper coreDbHelper = new CoreDbHelper(InstrumentationRegistry.getTargetContext(), new HashMap<TriggerKey, List<Runnable>>());
        requestRepository = new RequestModelRepository(coreDbHelper);
        shardRepository = new ShardModelRepository(coreDbHelper);

        completionLatch = new CountDownLatch(completionHandlerCountDown);
        completionHandler = new FakeCompletionHandler(completionLatch);

        fakeRestClient = new FakeRestClient(requestResults);

        provider = new CoreSdkHandlerProvider();
        coreSdkHandler = provider.provideHandler();

        worker = new DefaultWorker(requestRepository, watchDog, uiHandler, coreSdkHandler, completionHandler, fakeRestClient);

        manager = new RequestManager(coreSdkHandler, requestRepository, shardRepository, worker, fakeRestClient, mock(Registry.class));

        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < requestModels.length; ++i) {
                    manager.submit(requestModels[i], null);
                }
            }
        });

        watchDogLatch.await();
        completionLatch.await();
    }

    private RequestModel normal(int orderId) {
        TimestampProvider timestampProvider = new TimestampProvider();
        UUIDProvider uuidProvider = new UUIDProvider();
        return new RequestModel.Builder(timestampProvider, uuidProvider).url(URL + "normal/" + orderId).method(RequestMethod.GET).ttl(60_000).build();
    }

    private RequestModel expired(int orderId) {
        return new RequestModel(
                URL + "expired/" + orderId,
                RequestMethod.GET,
                new HashMap<String, Object>(),
                new HashMap<String, String>(),
                System.currentTimeMillis() - 5000,
                100,
                new UUIDProvider().provideId());
    }

    private void assertRequestTableEmpty() {
        assertEquals(new ArrayList<RequestModel>(), requestRepository.query(new Everything()));
    }
}
