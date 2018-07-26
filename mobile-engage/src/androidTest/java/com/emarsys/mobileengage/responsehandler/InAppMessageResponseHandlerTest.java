package com.emarsys.mobileengage.responsehandler;

import android.os.Build;
import android.os.Handler;
import android.support.test.filters.SdkSuppress;

import com.emarsys.core.database.repository.log.LogRepository;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.timestamp.TimestampProvider;
import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.iam.InAppPresenter;
import com.emarsys.mobileengage.iam.dialog.IamDialog;
import com.emarsys.mobileengage.iam.dialog.IamDialogProvider;
import com.emarsys.mobileengage.iam.dialog.action.OnDialogShownAction;
import com.emarsys.mobileengage.iam.dialog.action.SaveDisplayedIamAction;
import com.emarsys.mobileengage.iam.dialog.action.SendDisplayedIamAction;
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridge;
import com.emarsys.mobileengage.iam.jsbridge.InAppMessageHandlerProvider;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClickedRepository;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIamRepository;
import com.emarsys.mobileengage.iam.webview.DefaultMessageLoadedListener;
import com.emarsys.mobileengage.iam.webview.IamWebViewProvider;
import com.emarsys.mobileengage.iam.webview.MessageLoadedListener;
import com.emarsys.mobileengage.testUtil.CollectionTestUtils;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static android.os.Build.VERSION_CODES.KITKAT;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InAppMessageResponseHandlerTest {

    static {
        mock(Handler.class);
    }

    private InAppMessageResponseHandler handler;
    private InAppPresenter presenter;
    private IamWebViewProvider webViewProvider;
    private IamDialog dialog;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        webViewProvider = mock(IamWebViewProvider.class);

        dialog = mock(IamDialog.class);
        IamDialogProvider dialogProvider = mock(IamDialogProvider.class);
        when(dialogProvider.provideDialog(any(String.class))).thenReturn(dialog);

        presenter = new InAppPresenter(
                mock(Handler.class),
                webViewProvider,
                mock(InAppMessageHandlerProvider.class),
                dialogProvider,
                mock(ButtonClickedRepository.class),
                mock(DisplayedIamRepository.class),
                mock(TimestampProvider.class),
                mock(MobileEngageInternal.class));

        handler = new InAppMessageResponseHandler(presenter,
                mock(LogRepository.class),
                mock(TimestampProvider.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_inAppPresenter_shouldNotBeNull() {
        new InAppMessageResponseHandler(
                null,
                mock(LogRepository.class),
                mock(TimestampProvider.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_logRepository_shouldNotBeNull() {
        new InAppMessageResponseHandler(
                mock(InAppPresenter.class),
                null,
                mock(TimestampProvider.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_timestampProvider_shouldNotBeNull() {
        new InAppMessageResponseHandler(
                mock(InAppPresenter.class),
                mock(LogRepository.class),
                null);
    }

    @Test
    public void testShouldHandleResponse_shouldHandleOnly_kitkatAndAbove() {
        ResponseModel validResponse = buildResponseModel("{'message': {'html':'some html'}}");
        boolean shouldHandle = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        assertEquals(shouldHandle, handler.shouldHandleResponse(validResponse));
    }

    @Test
    @SdkSuppress(minSdkVersion = KITKAT)
    public void testShouldHandleResponse_shouldReturnTrueWhenTheResponseHasHtmlAttribute() {
        ResponseModel response = buildResponseModel("{'message': {'html':'some html'}}");
        assertTrue(handler.shouldHandleResponse(response));
    }

    @Test
    @SdkSuppress(minSdkVersion = KITKAT)
    public void testShouldHandleResponse_shouldReturnFalseWhenTheResponseHasANonJsonBody() {
        ResponseModel response = buildResponseModel("Created");
        assertFalse(handler.shouldHandleResponse(response));
    }

    @Test
    @SdkSuppress(minSdkVersion = KITKAT)
    public void testShouldHandleResponse_shouldReturnFalseWhenTheResponseHasNoMessageAttribute() {
        ResponseModel response = buildResponseModel("{'not_a_message': {'html':'some html'}}");
        assertFalse(handler.shouldHandleResponse(response));
    }

    @Test
    @SdkSuppress(minSdkVersion = KITKAT)
    public void testShouldHandleResponse_shouldReturnFalseWhenTheResponseHasNoHtmlAttribute() {
        ResponseModel response = buildResponseModel("{'message': {'not_html':'some html'}}");
        assertFalse(handler.shouldHandleResponse(response));
    }

    @Test
    @SdkSuppress(minSdkVersion = KITKAT)
    public void testHandleResponse_shouldCallLoadMessageAsync_withCorrectArguments() {
        String html = "<p>hello</p>";
        String responseBody = String.format("{'message': {'html':'%s', 'id': '123'} }", html);
        ResponseModel response = buildResponseModel(responseBody);

        handler.handleResponse(response);

        verify(webViewProvider).loadMessageAsync(eq(html), any(IamJsBridge.class), any(MessageLoadedListener.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    @SdkSuppress(minSdkVersion = KITKAT)
    public void testHandleResponse_setsSaveDisplayIamAction_onDialog() {
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

        String html = "<p>hello</p>";
        String responseBody = String.format("{'message': {'html':'%s', 'id': '123'} }", html);
        ResponseModel response = buildResponseModel(responseBody);

        handler.handleResponse(response);

        verify(dialog).setActions(captor.capture());
        List<OnDialogShownAction> actions = captor.getValue();

        assertEquals(1, CollectionTestUtils.numberOfElementsIn(actions, SaveDisplayedIamAction.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    @SdkSuppress(minSdkVersion = KITKAT)
    public void testHandleResponse_setsSendDisplayIamAction_onDialog() {
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

        String html = "<p>hello</p>";
        String responseBody = String.format("{'message': {'html':'%s', 'id': '123'} }", html);
        ResponseModel response = buildResponseModel(responseBody);

        handler.handleResponse(response);

        verify(dialog).setActions(captor.capture());
        List<OnDialogShownAction> actions = captor.getValue();

        assertEquals(1, CollectionTestUtils.numberOfElementsIn(actions, SendDisplayedIamAction.class));
    }

    private ResponseModel buildResponseModel(String responseBody) {
        return new ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .body(responseBody)
                .requestModel(mock(RequestModel.class))
                .build();
    }
}