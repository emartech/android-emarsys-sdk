package com.emarsys.mobileengage.responsehandler;

import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam;
import com.emarsys.mobileengage.iam.model.specification.FilterByCampaignId;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;
import com.emarsys.mobileengage.util.RequestUrlUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InAppCleanUpResponseHandlerTest {

    InAppCleanUpResponseHandler handler;
    Repository<DisplayedIam, SqlSpecification> displayedIamRepository;
    Repository<ButtonClicked, SqlSpecification> buttonClickRepository;

    RequestModel customEventRequestModel;
    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    @SuppressWarnings("unchecked")
    public void init() throws Exception {
        customEventRequestModel = mock(RequestModel.class);
        when(customEventRequestModel.getUrl()).thenReturn(new URL(RequestUrlUtils.createEventUrl_V3("yolo")));

        displayedIamRepository = mock(Repository.class);
        buttonClickRepository = mock(Repository.class);

        handler = new InAppCleanUpResponseHandler(displayedIamRepository, buttonClickRepository);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_inappRepository_shouldNotBeNull() {
        new InAppCleanUpResponseHandler(null, buttonClickRepository);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_buttonClickedRepository_shouldNotBeNull() {
        new InAppCleanUpResponseHandler(displayedIamRepository, null);
    }

    @Test
    public void testShouldHandleResponse_shouldReturnFalse_parsedJsonIsNull() {
        ResponseModel response = buildResponseModel("html", customEventRequestModel);
        assertFalse(handler.shouldHandleResponse(response));
    }

    @Test
    public void testShouldHandleResponse_shouldReturnFalse_responseHasNotOldMessages() {
        ResponseModel response = buildResponseModel("{}", customEventRequestModel);
        assertFalse(handler.shouldHandleResponse(response));
    }

    @Test
    public void testShouldHandleResponse_shouldReturnTrueWhen_responseHasOldMessages() {
        ResponseModel response = buildResponseModel("{'old_messages': ['123', '456', '78910']}", customEventRequestModel);
        assertTrue(handler.shouldHandleResponse(response));
    }

    @Test
    public void testShouldHandleResponse_shouldReturnFalseWhen_oldMessagesIsEmpty() {
        ResponseModel response = buildResponseModel("{'old_messages': []}", customEventRequestModel);
        assertFalse(handler.shouldHandleResponse(response));
    }

    @Test
    public void testShouldHandleResponse_shouldReturnFalseWhen_UrlIsNotCustomEventUrl() throws Exception {
        RequestModel requestModel = mock(RequestModel.class);
        when(requestModel.getUrl()).thenReturn(new URL("https://www.emarsys.com"));
        ResponseModel response = buildResponseModel("{'old_messages': ['123', '456', '78910']}", requestModel);
        assertFalse(handler.shouldHandleResponse(response));
    }

    @Test
    public void testShouldHandleResponse_shouldReturnTrueWhen_UrlIsCustomEventUrl() throws Exception {
        RequestModel requestModel = mock(RequestModel.class);
        when(requestModel.getUrl()).thenReturn(new URL(RequestUrlUtils.createEventUrl_V3("yolo")));
        ResponseModel response = buildResponseModel("{'old_messages': ['123', '456', '78910']}", requestModel);
        assertTrue(handler.shouldHandleResponse(response));
    }

    @Test
    public void testHandleResponse_shouldDelete_oldInApp() {
        ResponseModel response = buildResponseModel("{'old_messages': ['123']}", customEventRequestModel);
        handler.handleResponse(response);
        verify(displayedIamRepository).remove(new FilterByCampaignId("123"));
    }

    @Test
    public void testHandleResponse_shouldDelete_multiple_oldInApps() {
        ResponseModel response = buildResponseModel("{'old_messages': ['123', '456', '78910']}", customEventRequestModel);
        handler.handleResponse(response);
        verify(displayedIamRepository).remove(new FilterByCampaignId("123", "456", "78910"));
    }

    @Test
    public void testHandleResponse_shouldDelete_oldButtonClick() {
        ResponseModel response = buildResponseModel("{'old_messages': ['123']}", customEventRequestModel);
        handler.handleResponse(response);
        verify(buttonClickRepository).remove(new FilterByCampaignId("123"));
    }

    @Test
    public void testHandleResponse_shouldDelete_multiple_oldButtonClicks() {
        ResponseModel response = buildResponseModel("{'old_messages': ['123', '456', '78910']}", customEventRequestModel);
        handler.handleResponse(response);
        verify(buttonClickRepository).remove(new FilterByCampaignId("123", "456", "78910"));
    }

    private ResponseModel buildResponseModel(String responseBody, RequestModel requestModel) {
        return new ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .body(responseBody)
                .requestModel(requestModel)
                .build();
    }

}