package com.emarsys.mobileengage.iam.model.requestRepositoryProxy;

import android.content.Context;

import com.emarsys.core.database.helper.CoreDbHelper;
import com.emarsys.core.database.helper.DbHelper;
import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.database.repository.specification.Everything;
import com.emarsys.core.database.trigger.TriggerKey;
import com.emarsys.core.endpoint.ServiceEndpointProvider;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.provider.uuid.UUIDProvider;
import com.emarsys.core.request.model.CompositeRequestModel;
import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.request.model.RequestModelRepository;
import com.emarsys.core.request.model.specification.QueryLatestRequestModel;
import com.emarsys.core.util.TimestampUtils;
import com.emarsys.mobileengage.MobileEngageRequestContext;
import com.emarsys.mobileengage.iam.InAppEventHandlerInternal;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClickedRepository;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIamRepository;
import com.emarsys.mobileengage.util.RequestPayloadUtils;
import com.emarsys.testUtil.DatabaseTestUtils;
import com.emarsys.testUtil.InstrumentationRegistry;
import com.emarsys.testUtil.RandomTestUtils;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class RequestRepositoryProxyTest {

    private static final long TIMESTAMP = 80_000L;
    private static final String REQUEST_ID = "REQUEST_ID";
    private static final String APPLICATION_CODE = "applicationCode";
    private static final String EVENT_HOST = "https://mobile-events.eservice.emarsys.net/v3";
    private static final String EVENT_HOST_V4 = "https://mobile-events.eservice.emarsys.net/v4";

    private MobileEngageRequestContext mockRequestContext;

    private Repository<RequestModel, SqlSpecification> mockRequestModelRepository;
    private Repository<DisplayedIam, SqlSpecification> mockDisplayedIamRepository;
    private Repository<ButtonClicked, SqlSpecification> mockButtonClickedRepository;

    private Repository<RequestModel, SqlSpecification> requestModelRepository;
    private Repository<DisplayedIam, SqlSpecification> displayedIamRepository;
    private Repository<ButtonClicked, SqlSpecification> buttonClickedRepository;

    private TimestampProvider timestampProvider;
    private InAppEventHandlerInternal inAppEventHandlerInternal;

    private RequestRepositoryProxy compositeRepository;
    private UUIDProvider uuidProvider;
    private ServiceEndpointProvider mockEventServiceProvider;
    private ServiceEndpointProvider mockEventServiceV4Provider;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    @SuppressWarnings("unchecked")
    public void init() {
        DatabaseTestUtils.deleteCoreDatabase();

        Context context = InstrumentationRegistry.getTargetContext();

        mockRequestContext = mock(MobileEngageRequestContext.class);

        mockRequestModelRepository = mock(Repository.class);
        mockDisplayedIamRepository = mock(Repository.class);
        mockButtonClickedRepository = mock(Repository.class);

        when(mockRequestContext.getApplicationCode()).thenReturn(APPLICATION_CODE);

        DbHelper dbHelper = new CoreDbHelper(context, new HashMap<TriggerKey, List<Runnable>>());

        requestModelRepository = new RequestModelRepository(dbHelper);
        displayedIamRepository = new DisplayedIamRepository(dbHelper);
        buttonClickedRepository = new ButtonClickedRepository(dbHelper);

        timestampProvider = mock(TimestampProvider.class);
        when(timestampProvider.provideTimestamp()).thenReturn(TIMESTAMP);

        uuidProvider = mock(UUIDProvider.class);
        when(uuidProvider.provideId()).thenReturn(REQUEST_ID);

        inAppEventHandlerInternal = mock(InAppEventHandlerInternal.class);

        mockEventServiceProvider = mock(ServiceEndpointProvider.class);
        when(mockEventServiceProvider.provideEndpointHost()).thenReturn(EVENT_HOST);

        mockEventServiceV4Provider = mock(ServiceEndpointProvider.class);
        when(mockEventServiceV4Provider.provideEndpointHost()).thenReturn(EVENT_HOST_V4);

        compositeRepository = new RequestRepositoryProxy(
                mockRequestModelRepository,
                mockDisplayedIamRepository,
                mockButtonClickedRepository,
                timestampProvider,
                uuidProvider,
                inAppEventHandlerInternal,
                mockEventServiceProvider,
                mockEventServiceV4Provider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_requestRepository_mustNotBeNull() {
        new RequestRepositoryProxy(null, mockDisplayedIamRepository, mockButtonClickedRepository, timestampProvider, uuidProvider, inAppEventHandlerInternal, mockEventServiceProvider, mockEventServiceV4Provider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_displayedIamRepository_mustNotBeNull() {
        new RequestRepositoryProxy(mockRequestModelRepository, null, mockButtonClickedRepository, timestampProvider, uuidProvider, inAppEventHandlerInternal, mockEventServiceProvider, mockEventServiceV4Provider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_buttonClickedRepository_mustNotBeNull() {
        new RequestRepositoryProxy(mockRequestModelRepository, mockDisplayedIamRepository, null, timestampProvider, uuidProvider, inAppEventHandlerInternal, mockEventServiceProvider, mockEventServiceV4Provider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_timestampProvider_mustNotBeNull() {
        new RequestRepositoryProxy(mockRequestModelRepository, mockDisplayedIamRepository, buttonClickedRepository, null, uuidProvider, inAppEventHandlerInternal, mockEventServiceProvider, mockEventServiceV4Provider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_inAppInternal_mustNotBeNull() {
        new RequestRepositoryProxy(mockRequestModelRepository, mockDisplayedIamRepository, buttonClickedRepository, timestampProvider, uuidProvider, null, mockEventServiceProvider, mockEventServiceV4Provider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_uuidProvider_mustNotBeNull() {
        new RequestRepositoryProxy(mockRequestModelRepository, mockDisplayedIamRepository, buttonClickedRepository, timestampProvider, null, inAppEventHandlerInternal, mockEventServiceProvider, mockEventServiceV4Provider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_eventServiceProvider_mustNotBeNull() {
        new RequestRepositoryProxy(mockRequestModelRepository, mockDisplayedIamRepository, buttonClickedRepository, timestampProvider, uuidProvider, inAppEventHandlerInternal, null, mockEventServiceV4Provider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_eventServiceV4Provider_mustNotBeNull() {
        new RequestRepositoryProxy(mockRequestModelRepository, mockDisplayedIamRepository, buttonClickedRepository, timestampProvider, uuidProvider, inAppEventHandlerInternal, mockEventServiceProvider, null);
    }

    @Test
    public void testAdd_shouldDelegate_toRequestModelRepository() {
        RequestModel requestModel = mock(RequestModel.class);

        compositeRepository.add(requestModel);

        verify(mockRequestModelRepository).add(requestModel);
    }

    @Test
    public void testAdd_shouldNotStoreCompositeRequestModels() {
        CompositeRequestModel requestModel = mock(CompositeRequestModel.class);

        compositeRepository.add(requestModel);

        verifyZeroInteractions(mockRequestModelRepository);
    }

    @Test
    public void testRemove_shouldDelegate_toRequestModelRepository() {
        SqlSpecification spec = mock(SqlSpecification.class);

        compositeRepository.remove(spec);

        verify(mockRequestModelRepository).remove(spec);
    }

    @Test
    public void testIsEmpty_whenEmpty_shouldDelegate_toRequestModelRepository() {
        when(mockRequestModelRepository.isEmpty()).thenReturn(true);

        assertTrue(compositeRepository.isEmpty());
        verify(mockRequestModelRepository).isEmpty();
    }

    @Test
    public void testIsEmpty_whenNotEmpty_shouldDelegate_toRequestModelRepository() {
        when(mockRequestModelRepository.isEmpty()).thenReturn(false);

        assertFalse(compositeRepository.isEmpty());
        verify(mockRequestModelRepository).isEmpty();
    }

    @Test
    public void testQuery_shouldReturnOriginalQuery_whenThereAreNoCustomEvents() {
        compositeRepository = compositeRepositoryWithRealRepositories();

        RequestModel firstRequestModel = requestModel();
        requestModelRepository.add(firstRequestModel);
        requestModelRepository.add(requestModel());
        requestModelRepository.add(requestModel());

        List<RequestModel> expected = Collections.singletonList(firstRequestModel);

        assertEquals(expected, compositeRepository.query(new QueryLatestRequestModel()));
    }

    @Test
    public void testQuery_resultShouldContainCompositeRequestModel_whenResultContainsCustomEvent() {
        compositeRepository = compositeRepositoryWithRealRepositories();

        RequestModel request1 = requestModel();
        RequestModel request2 = requestModel();
        RequestModel request3 = requestModel();

        final RequestModel customEvent1 = customEvent_V3(900, "event1");

        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("key1", "value1");
        attributes.put("key2", "value2");
        final RequestModel customEvent2 = customEvent_V3(1000, "event2", attributes);
        final RequestModel customEvent3 = customEvent_V3(1200, "event3");

        requestModelRepository.add(request1);
        requestModelRepository.add(request2);
        requestModelRepository.add(customEvent1);
        requestModelRepository.add(customEvent2);
        requestModelRepository.add(request3);
        requestModelRepository.add(customEvent3);

        Map<String, Object> event1 = new HashMap<>();
        event1.put("type", "custom");
        event1.put("name", "event1");
        event1.put("timestamp", TimestampUtils.formatTimestampWithUTC(900));

        Map<String, Object> event2 = new HashMap<>();
        event2.put("type", "custom");
        event2.put("name", "event2");
        event2.put("timestamp", TimestampUtils.formatTimestampWithUTC(1000));
        event2.put("attributes", new HashMap<String, String>() {{
            put("key1", "value1");
            put("key2", "value2");
        }});

        Map<String, Object> event3 = new HashMap<>();
        event3.put("type", "custom");
        event3.put("name", "event3");
        event3.put("timestamp", TimestampUtils.formatTimestampWithUTC(1200));

        Map<String, Object> payload = RequestPayloadUtils.createCompositeRequestModelPayload(
                Arrays.asList(event1, event2, event3),
                Collections.<DisplayedIam>emptyList(),
                Collections.<ButtonClicked>emptyList(),
                false);

        RequestModel expectedComposite = new CompositeRequestModel(
                REQUEST_ID,
                "https://mobile-events.eservice.emarsys.net/v3/apps/" + APPLICATION_CODE + "/client/events",
                RequestMethod.POST,
                payload,
                customEvent1.getHeaders(),
                TIMESTAMP,
                Long.MAX_VALUE,
                new String[]{customEvent1.getId(), customEvent2.getId(), customEvent3.getId()}
        );

        List<RequestModel> expected = Arrays.asList(
                request1,
                request2,
                expectedComposite,
                request3);

        assertEquals(expected, compositeRepository.query(new Everything()));
    }

    @Test
    public void testQuery_resultShouldContainClicksAndDisplays_withSingleCustomEvent() {
        compositeRepository = compositeRepositoryWithRealRepositories();

        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("key1", "value1");
        attributes.put("key2", "value2");
        final RequestModel customEvent1 = customEvent_V3(1000, "event1", attributes);

        requestModelRepository.add(customEvent1);

        ButtonClicked buttonClicked1 = new ButtonClicked("campaign1", "button1", 200);
        ButtonClicked buttonClicked2 = new ButtonClicked("campaign1", "button2", 300);
        ButtonClicked buttonClicked3 = new ButtonClicked("campaign2", "button1", 2000);

        buttonClickedRepository.add(buttonClicked1);
        buttonClickedRepository.add(buttonClicked2);
        buttonClickedRepository.add(buttonClicked3);

        DisplayedIam displayedIam1 = new DisplayedIam("campaign1", 100);
        DisplayedIam displayedIam2 = new DisplayedIam("campaign2", 1500);
        DisplayedIam displayedIam3 = new DisplayedIam("campaign3", 30000);

        displayedIamRepository.add(displayedIam1);
        displayedIamRepository.add(displayedIam2);
        displayedIamRepository.add(displayedIam3);

        HashMap<String, String> eventAttributes = new HashMap<>();
        eventAttributes.put("key1", "value1");
        eventAttributes.put("key2", "value2");

        Map<String, Object> event1 = new HashMap<>();
        event1.put("type", "custom");
        event1.put("name", "event1");
        event1.put("timestamp", TimestampUtils.formatTimestampWithUTC(1000));
        event1.put("attributes", eventAttributes);

        Map<String, Object> payload = RequestPayloadUtils.createCompositeRequestModelPayload(
                Collections.singletonList(event1),
                Arrays.asList(
                        new DisplayedIam("campaign1", 100),
                        new DisplayedIam("campaign2", 1500),
                        new DisplayedIam("campaign3", 30000)
                ),
                Arrays.asList(
                        new ButtonClicked("campaign1", "button1", 200),
                        new ButtonClicked("campaign1", "button2", 300),
                        new ButtonClicked("campaign2", "button1", 2000)
                ),
                false);

        RequestModel expectedComposite = new CompositeRequestModel(
                REQUEST_ID,
                "https://mobile-events.eservice.emarsys.net/v3/apps/" + APPLICATION_CODE + "/client/events",
                RequestMethod.POST,
                payload,
                customEvent1.getHeaders(),
                TIMESTAMP,
                Long.MAX_VALUE,
                new String[]{customEvent1.getId()}
        );

        List<RequestModel> expected = Collections.singletonList(expectedComposite);

        assertEquals(expected, compositeRepository.query(new Everything()));

    }

    @Test
    public void testQuery_resultShouldContainClicksAndDisplays_withMultipleRequests() {
        compositeRepository = compositeRepositoryWithRealRepositories();

        RequestModel request1 = requestModel();
        RequestModel request2 = requestModel();
        RequestModel request3 = requestModel();

        final RequestModel customEvent1 = customEvent_V3(900, "event1");

        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("key1", "value1");
        attributes.put("key2", "value2");
        final RequestModel customEvent2 = customEvent_V3(1000, "event2", attributes);
        final RequestModel customEvent3 = customEvent_V3(1200, "event3");

        requestModelRepository.add(request1);
        requestModelRepository.add(request2);
        requestModelRepository.add(customEvent1);
        requestModelRepository.add(customEvent2);
        requestModelRepository.add(request3);
        requestModelRepository.add(customEvent3);

        ButtonClicked buttonClicked1 = new ButtonClicked("campaign1", "button1", 200);
        ButtonClicked buttonClicked2 = new ButtonClicked("campaign1", "button2", 300);
        ButtonClicked buttonClicked3 = new ButtonClicked("campaign2", "button1", 2000);

        buttonClickedRepository.add(buttonClicked1);
        buttonClickedRepository.add(buttonClicked2);
        buttonClickedRepository.add(buttonClicked3);

        DisplayedIam displayedIam1 = new DisplayedIam("campaign1", 100);
        DisplayedIam displayedIam2 = new DisplayedIam("campaign2", 1500);
        DisplayedIam displayedIam3 = new DisplayedIam("campaign3", 30000);

        displayedIamRepository.add(displayedIam1);
        displayedIamRepository.add(displayedIam2);
        displayedIamRepository.add(displayedIam3);

        Map<String, Object> event1 = new HashMap<>();
        event1.put("type", "custom");
        event1.put("name", "event1");
        event1.put("timestamp", TimestampUtils.formatTimestampWithUTC(900));

        Map<String, Object> event2 = new HashMap<>();
        event2.put("type", "custom");
        event2.put("name", "event2");
        event2.put("timestamp", TimestampUtils.formatTimestampWithUTC(1000));
        event2.put("attributes", new HashMap<String, String>() {{
            put("key1", "value1");
            put("key2", "value2");
        }});

        Map<String, Object> event3 = new HashMap<>();
        event3.put("type", "custom");
        event3.put("name", "event3");
        event3.put("timestamp", TimestampUtils.formatTimestampWithUTC(1200));

        Map<String, Object> payload = RequestPayloadUtils.createCompositeRequestModelPayload(
                Arrays.asList(event1, event2, event3),
                Arrays.asList(
                        new DisplayedIam("campaign1", 100),
                        new DisplayedIam("campaign2", 1500),
                        new DisplayedIam("campaign3", 30000)
                ),
                Arrays.asList(
                        new ButtonClicked("campaign1", "button1", 200),
                        new ButtonClicked("campaign1", "button2", 300),
                        new ButtonClicked("campaign2", "button1", 2000)
                ),
                false);

        RequestModel expectedComposite = new CompositeRequestModel(
                REQUEST_ID,
                "https://mobile-events.eservice.emarsys.net/v3/apps/" + APPLICATION_CODE + "/client/events",
                RequestMethod.POST,
                payload,
                customEvent1.getHeaders(),
                TIMESTAMP,
                Long.MAX_VALUE,
                new String[]{customEvent1.getId(), customEvent2.getId(), customEvent3.getId()}
        );

        List<RequestModel> expected = Arrays.asList(
                request1,
                request2,
                expectedComposite,
                request3);

        assertEquals(expected, compositeRepository.query(new Everything()));
    }

    @Test
    public void testQuery_resultPayloadShouldContainDoNotDisturbWithTrue_whenDoNotDisturbIsOn() {
        when(inAppEventHandlerInternal.isPaused()).thenReturn(true);
        compositeRepository = compositeRepositoryWithRealRepositories();

        final RequestModel customEvent1 = customEvent_V3(900, "event1");
        requestModelRepository.add(customEvent1);

        List<RequestModel> result = compositeRepository.query(new Everything());
        Map<String, Object> payload = result.get(0).getPayload();

        assertTrue((Boolean) payload.get("dnd"));
    }

    @Test
    public void testQuery_resultPayloadShouldNotContainDoNotDisturb_whenDoNotDisturbIsOff() {
        when(inAppEventHandlerInternal.isPaused()).thenReturn(false);
        compositeRepository = compositeRepositoryWithRealRepositories();

        final RequestModel customEvent1 = customEvent_V3(900, "event1");
        requestModelRepository.add(customEvent1);

        List<RequestModel> result = compositeRepository.query(new Everything());
        Map<String, Object> payload = result.get(0).getPayload();

        assertNull(payload.get("dnd"));
    }

    private RequestRepositoryProxy compositeRepositoryWithRealRepositories() {
        return new RequestRepositoryProxy(
                requestModelRepository,
                displayedIamRepository,
                buttonClickedRepository,
                timestampProvider,
                uuidProvider,
                inAppEventHandlerInternal,
                mockEventServiceProvider,
                mockEventServiceV4Provider
        );
    }

    private RequestModel customEvent_V3(long timestamp, String eventName) {
        return customEvent_V3(timestamp, eventName, null);
    }

    private RequestModel customEvent_V3(long timestamp, final String eventName, final Map<String, Object> attributes) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", "custom");
        event.put("name", eventName);
        event.put("timestamp", TimestampUtils.formatTimestampWithUTC(timestamp));
        if (attributes != null && !attributes.isEmpty()) {
            event.put("attributes", attributes);
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("clicks", new ArrayList<>());
        payload.put("viewed_messages", new ArrayList<>());
        payload.put("events", Collections.singletonList(event));
        payload.put("hardware_id", "dummy_hardware_id");

        Map<String, String> headers = new HashMap<>();
        headers.put("custom_event_header1", "custom_event_value1");
        headers.put("custom_event_header2", "custom_event_value2");

        return new RequestModel(
                "https://mobile-events.eservice.emarsys.net/v3/apps/" + APPLICATION_CODE + "/client/events",
                RequestMethod.POST,
                payload,
                headers,
                System.currentTimeMillis(),
                999,
                uuidProvider.provideId()
        );
    }

    private RequestModel requestModel() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("key", RandomTestUtils.randomString());

        Map<String, String> headers = new HashMap<>();
        headers.put("header1", "value1");
        headers.put("header2", "value2");

        return new RequestModel.Builder(timestampProvider, uuidProvider)
                .url("https://emarsys.com")
                .payload(payload)
                .headers(headers)
                .build();
    }

}