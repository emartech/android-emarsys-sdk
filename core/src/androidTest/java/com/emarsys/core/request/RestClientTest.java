package com.emarsys.core.request;

import androidx.test.InstrumentationRegistry;

import com.emarsys.core.connection.ConnectionProvider;
import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.fake.FakeCompletionHandler;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.provider.uuid.UUIDProvider;
import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.testUtil.RequestModelTestUtils;
import com.emarsys.testUtil.ConnectionTestUtils;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class RestClientTest {

    private RestClient client;
    private CountDownLatch latch;

    private Repository<Map<String, Object>, SqlSpecification> logRepository;
    private TimestampProvider timestampProvider;
    private ConnectionProvider connectionProvider;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        ConnectionTestUtils.checkConnection(InstrumentationRegistry.getContext());

        logRepository = mock(Repository.class);
        timestampProvider = mock(TimestampProvider.class);
        connectionProvider = new ConnectionProvider();
        client = new RestClient(logRepository, connectionProvider, timestampProvider);
        latch = new CountDownLatch(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_logRepository_mustNotBeNull() {
        new RestClient(null,connectionProvider,  timestampProvider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_connectionProvider_mustNotBeNull() {
        new RestClient(logRepository,null,  timestampProvider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_timestampProvider_mustNotBeNull() {
        new RestClient(logRepository, connectionProvider,null);
    }

    @Test
    public void testSendRequest_requestDoneSuccessfully() throws Exception {
        FakeCompletionHandler handler = new FakeCompletionHandler(latch);
        RequestModel model = RequestModelTestUtils.createRequestModel(RequestMethod.GET);

        client.execute(model, handler);

        latch.await();
        assertEquals(model.getId(), handler.getSuccessId());
        assertEquals(1, handler.getOnSuccessCount());
        assertEquals(0, handler.getOnErrorCount());
    }

    @Test
    public void testSendRequest_callbackWithResponseModel() throws Exception {
        FakeCompletionHandler handler = new FakeCompletionHandler(latch);
        RequestModel model = RequestModelTestUtils.createRequestModel(RequestMethod.POST);

        client.execute(model, handler);

        latch.await();
        assertEquals(model.getId(), handler.getErrorId());
        assertEquals(0, handler.getOnSuccessCount());
        assertEquals(1, handler.getOnErrorCount());
        assertEquals(405, handler.getFailureResponseModel().getStatusCode());
    }

    @Test
    public void testSendRequest_callbackWithException() throws Exception {
        FakeCompletionHandler handler = new FakeCompletionHandler(latch);
        TimestampProvider timestampProvider = new TimestampProvider();
        RequestModel model = new RequestModel.Builder(timestampProvider, new UUIDProvider()).url("https://www.nosuchwebsite.emarsys.com").method(RequestMethod.GET).build();

        client.execute(model, handler);

        latch.await();
        assertEquals(model.getId(), handler.getErrorId());
        assertEquals(0, handler.getOnSuccessCount());
        assertEquals(1, handler.getOnErrorCount());
        assertEquals(((Exception) new UnknownHostException()).getClass(), handler.getException().getClass());
    }

}