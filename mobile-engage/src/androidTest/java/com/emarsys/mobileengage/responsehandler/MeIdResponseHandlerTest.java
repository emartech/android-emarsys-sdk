package com.emarsys.mobileengage.responsehandler;

import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.mobileengage.storage.MeIdSignatureStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MeIdResponseHandlerTest {

    private String meId;
    private String meIdSignature;
    private MeIdResponseHandler handler;
    private MeIdStorage meIdStorage;
    private MeIdSignatureStorage meIdSignatureStorage;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();
    private ResponseModel responseModelWithMeIdAndSignature;

    @Before
    public void init() {
        meId = "123";
        meIdSignature = "d5be8583137bd5be8593137b";
        meIdStorage = mock(MeIdStorage.class);
        meIdSignatureStorage = mock(MeIdSignatureStorage.class);
        handler = new MeIdResponseHandler(meIdStorage, meIdSignatureStorage);

        responseModelWithMeIdAndSignature = new ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .body(String.format("{ 'api_me_id': '%s', 'me_id_signature': '%s'}", meId, meIdSignature))
                .requestModel(mock(RequestModel.class))
                .build();
    }

    @Test
    public void testConstructor_initializesFields() {
        meIdStorage = mock(MeIdStorage.class);
        meIdSignatureStorage = mock(MeIdSignatureStorage.class);
        handler = new MeIdResponseHandler(meIdStorage, meIdSignatureStorage);

        assertEquals(meIdStorage, handler.meIdStorage);
        assertEquals(meIdSignatureStorage, handler.meIdSignatureStorage);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_meIdStorage_mustNotBeNull() {
        new MeIdResponseHandler(null, meIdSignatureStorage);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_meIdSignatureStorage_mustNotBeNull() {
        new MeIdResponseHandler(meIdStorage, null);
    }

    @Test
    public void testShouldHandleResponse_shouldReturnTrue_whenResponseBodyIncludes_meIdAndSignature() {
        boolean result = handler.shouldHandleResponse(responseModelWithMeIdAndSignature);

        Assert.assertTrue(result);
    }

    @Test
    public void testShouldHandleResponse_shouldReturnTrue_whenResponseBodyLacks_meId() {
        ResponseModel responseModel = new ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .body("{ 'me_id_signature': '12aa34' }")
                .requestModel(mock(RequestModel.class))
                .build();

        boolean result = handler.shouldHandleResponse(responseModel);

        Assert.assertFalse(result);
    }

    @Test
    public void testShouldHandleResponse_shouldReturnTrue_whenResponseBodyLacks_meIdSignature() {
        ResponseModel responseModel = new ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .body("{ 'api_me_id': 'meid123' }")
                .requestModel(mock(RequestModel.class))
                .build();

        boolean result = handler.shouldHandleResponse(responseModel);

        Assert.assertFalse(result);
    }

    @Test
    public void testHandleResponse_shouldStoreMeId() {
        handler.handleResponse(responseModelWithMeIdAndSignature);

        verify(meIdStorage).set(meId);
    }

    @Test
    public void testHandleResponse_shouldStoreMeIdSignature() {
        handler.handleResponse(responseModelWithMeIdAndSignature);

        verify(meIdSignatureStorage).set(meIdSignature);
    }

}