package com.emarsys.mobileengage.responsehandler;

import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.mobileengage.endpoint.Endpoint;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam;
import com.emarsys.mobileengage.iam.model.specification.FilterByCampaignId;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.net.URL;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InAppCleanUpResponseHandlerTest {

    private InAppCleanUpResponseHandler handler;
    private Repository<DisplayedIam, SqlSpecification> mockDisplayedIamRepository;
    private Repository<ButtonClicked, SqlSpecification> mockButtonClickRepository;

    private RequestModel mockRequestModel;
    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    @SuppressWarnings("unchecked")
    public void init() throws Exception {
        mockRequestModel = mock(RequestModel.class);

        when(mockRequestModel.getUrl()).thenReturn(new URL(Endpoint.ME_V3_EVENT_BASE));

        mockDisplayedIamRepository = mock(Repository.class);
        mockButtonClickRepository = mock(Repository.class);

        handler = new InAppCleanUpResponseHandler(mockDisplayedIamRepository, mockButtonClickRepository);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_inappRepository_shouldNotBeNull() {
        new InAppCleanUpResponseHandler(null, mockButtonClickRepository);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_buttonClickedRepository_shouldNotBeNull() {
        new InAppCleanUpResponseHandler(mockDisplayedIamRepository, null);
    }

    @Test
    public void testShouldHandleResponse_shouldReturnFalse_parsedJsonIsNull() {
        ResponseModel response = buildResponseModel("html", mockRequestModel);
        assertFalse(handler.shouldHandleResponse(response));
    }

    @Test
    public void testShouldHandleResponse_shouldReturnFalse_responseHasNotOldMessages() {
        ResponseModel response = buildResponseModel("{}", mockRequestModel);
        assertFalse(handler.shouldHandleResponse(response));
    }

    @Test
    public void testShouldHandleResponse_shouldReturnTrueWhen_responseHasOldMessages() {
        ResponseModel response = buildResponseModel("{'oldCampaigns': ['123', '456', '78910']}", mockRequestModel);
        assertTrue(handler.shouldHandleResponse(response));
    }

    @Test
    public void testShouldHandleResponse_shouldReturnFalseWhen_oldMessagesIsEmpty() {
        ResponseModel response = buildResponseModel("{'oldCampaigns': []}", mockRequestModel);
        assertFalse(handler.shouldHandleResponse(response));
    }

    @Test
    public void testShouldHandleResponse_shouldReturnFalseWhen_UrlIsNotCustomEventUrl() throws Exception {
        RequestModel requestModel = mock(RequestModel.class);
        when(requestModel.getUrl()).thenReturn(new URL("https://www.emarsys.com"));
        ResponseModel response = buildResponseModel("{'oldCampaigns': ['123', '456', '78910']}", requestModel);
        assertFalse(handler.shouldHandleResponse(response));
    }

    @Test
    public void testShouldHandleResponse_shouldReturnTrueWhen_UrlIsCustomEventUrl() {
        ResponseModel response = buildResponseModel("{'oldCampaigns': ['123', '456', '78910']}", mockRequestModel);
        assertTrue(handler.shouldHandleResponse(response));
    }

    @Test
    public void testHandleResponse_shouldDelete_oldInApp() {
        ResponseModel response = buildResponseModel("{'oldCampaigns': ['123']}", mockRequestModel);
        handler.handleResponse(response);
        verify(mockDisplayedIamRepository).remove(new FilterByCampaignId("123"));
    }

    @Test
    public void testHandleResponse_shouldDelete_multiple_oldInApps() {
        ResponseModel response = buildResponseModel("{'oldCampaigns': ['123', '456', '78910']}", mockRequestModel);
        handler.handleResponse(response);
        verify(mockDisplayedIamRepository).remove(new FilterByCampaignId("123", "456", "78910"));
    }

    @Test
    public void testHandleResponse_shouldDelete_oldButtonClick() {
        ResponseModel response = buildResponseModel("{'oldCampaigns': ['123']}", mockRequestModel);
        handler.handleResponse(response);
        verify(mockButtonClickRepository).remove(new FilterByCampaignId("123"));
    }

    @Test
    public void testHandleResponse_shouldDelete_multiple_oldButtonClicks() {
        ResponseModel response = buildResponseModel("{'oldCampaigns': ['123', '456', '78910']}", mockRequestModel);
        handler.handleResponse(response);
        verify(mockButtonClickRepository).remove(new FilterByCampaignId("123", "456", "78910"));
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