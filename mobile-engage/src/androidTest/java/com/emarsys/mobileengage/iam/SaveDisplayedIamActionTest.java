package com.emarsys.mobileengage.iam;

import com.emarsys.core.concurrency.CoreSdkHandlerProvider;
import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.handler.CoreSdkHandler;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.mobileengage.iam.dialog.action.SaveDisplayedIamAction;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam;
import com.emarsys.testUtil.TimeoutUtils;
import com.emarsys.testUtil.mockito.ThreadSpy;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.mockito.Mockito.*;

public class SaveDisplayedIamActionTest {

    private static final String CAMPAIGN_ID = "123";
    private static final String SID = "testSid";
    private static final String URL = "https://www.emarsys.com";
    private static final long TIMESTAMP = 123;
    private static final DisplayedIam IAM = new DisplayedIam(CAMPAIGN_ID, TIMESTAMP);

    private SaveDisplayedIamAction action;
    private Repository<DisplayedIam, SqlSpecification> repository;
    private ThreadSpy threadSpy;
    private CoreSdkHandler handler;
    private TimestampProvider timestampProvider;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    @SuppressWarnings("unchecked")
    public void init() {
        threadSpy = new ThreadSpy();
        repository = mock(Repository.class);
        handler = new CoreSdkHandlerProvider().provideHandler();
        timestampProvider = mock(TimestampProvider.class);
        when(timestampProvider.provideTimestamp()).thenReturn(TIMESTAMP);

        doAnswer(threadSpy).when(repository).add(IAM);
        action = new SaveDisplayedIamAction(handler, repository, timestampProvider);
    }

    @After
    public void tearDown() {
        handler.getLooper().quit();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_handlerMustNotBeNull() {
        new SaveDisplayedIamAction(null, repository, timestampProvider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_repositoryMustNotBeNull() {
        new SaveDisplayedIamAction(handler, null, timestampProvider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_timestampProviderMustNotBeNull() {
        new SaveDisplayedIamAction(handler, repository, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExecute_campaignIdMustNotBeNull() {
        action.execute(null, SID, URL);
    }

    @Test
    public void testExecute_callsRepository() {
        action.execute(CAMPAIGN_ID, SID, URL);
        verify(repository, timeout(1000)).add(IAM);
    }

    @Test
    public void testExecute_callsRepository_onCoreSdkThread() throws InterruptedException {
        action.execute(CAMPAIGN_ID, SID, URL);
        threadSpy.verifyCalledOnCoreSdkThread();
    }

}