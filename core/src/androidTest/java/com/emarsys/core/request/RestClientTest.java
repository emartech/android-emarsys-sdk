package com.emarsys.core.request;

import android.support.test.InstrumentationRegistry;

import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.fake.FakeCompletionHandler;
import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.testUtil.ConnectionTestUtils;
import com.emarsys.core.testUtil.RequestModelTestUtils;
import com.emarsys.core.testUtil.TimeoutUtils;
import com.emarsys.core.timestamp.TimestampProvider;

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

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();
    private Repository<Map<String, Object>, SqlSpecification> logRepository;
    private TimestampProvider timestampProvider;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        ConnectionTestUtils.checkConnection(InstrumentationRegistry.getContext());

        logRepository = mock(Repository.class);
        timestampProvider = mock(TimestampProvider.class);

        client = new RestClient(logRepository, timestampProvider);
        latch = new CountDownLatch(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_logRepositoryMustNotBeNull() {
        new RestClient(null, timestampProvider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_timestampProviderMustNotBeNull() {
        new RestClient(logRepository, null);
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
        RequestIdProvider requestIdProvider = new RequestIdProvider();
        RequestModel model = new RequestModel.Builder(timestampProvider, requestIdProvider).url("https://www.nosuchwebsite.emarsys.com").method(RequestMethod.GET).build();

        client.execute(model, handler);

        latch.await();
        assertEquals(model.getId(), handler.getErrorId());
        assertEquals(0, handler.getOnSuccessCount());
        assertEquals(1, handler.getOnErrorCount());
        assertEquals(((Exception) new UnknownHostException()).getClass(), handler.getException().getClass());
    }

}