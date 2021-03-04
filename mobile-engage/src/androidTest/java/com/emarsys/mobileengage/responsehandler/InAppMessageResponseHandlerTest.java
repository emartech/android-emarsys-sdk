package com.emarsys.mobileengage.responsehandler;

import android.os.Handler;
import com.emarsys.core.handler.CoreSdkHandler;
import com.emarsys.core.provider.activity.CurrentActivityProvider;
import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.provider.uuid.UUIDProvider;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.mobileengage.iam.InAppInternal;
import com.emarsys.mobileengage.iam.OverlayInAppPresenter;
import com.emarsys.mobileengage.iam.dialog.IamDialog;
import com.emarsys.mobileengage.iam.dialog.IamDialogProvider;
import com.emarsys.mobileengage.iam.dialog.action.OnDialogShownAction;
import com.emarsys.mobileengage.iam.dialog.action.SaveDisplayedIamAction;
import com.emarsys.mobileengage.iam.dialog.action.SendDisplayedIamAction;
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridge;
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridgeFactory;
import com.emarsys.mobileengage.iam.jsbridge.JSCommandFactory;
import com.emarsys.mobileengage.iam.model.InAppMessage;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClickedRepository;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIamRepository;
import com.emarsys.mobileengage.iam.webview.IamStaticWebViewProvider;
import com.emarsys.mobileengage.iam.webview.MessageLoadedListener;
import com.emarsys.testUtil.CollectionTestUtils;
import com.emarsys.testUtil.TimeoutUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class InAppMessageResponseHandlerTest {

    static {
        mock(Handler.class);
    }

    private InAppMessageResponseHandler handler;
    private OverlayInAppPresenter presenter;
    private IamStaticWebViewProvider webViewProvider;
    private IamDialog dialog;
    private IamJsBridgeFactory mockJsBridgeFactory;
    private IamJsBridge mockJsBridge;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    @SuppressWarnings("unchecked")
    public void init() {
        webViewProvider = mock(IamStaticWebViewProvider.class);
        mockJsBridgeFactory = mock(IamJsBridgeFactory.class);
        mockJsBridge = mock(IamJsBridge.class);

        when(mockJsBridgeFactory.createJsBridge(any(JSCommandFactory.class),any(InAppMessage.class))).thenReturn(mockJsBridge);

        dialog = mock(IamDialog.class);
        IamDialogProvider dialogProvider = mock(IamDialogProvider.class);
        when(dialogProvider.provideDialog(any(String.class), (String) isNull(), (String) isNull(), any(String.class))).thenReturn(dialog);

        presenter = new OverlayInAppPresenter(
                mock(CoreSdkHandler.class),
                mock(Handler.class),
                webViewProvider,
                mock(InAppInternal.class),
                dialogProvider,
                mock(ButtonClickedRepository.class),
                mock(DisplayedIamRepository.class),
                mock(TimestampProvider.class),
                mock(CurrentActivityProvider.class),
                mockJsBridgeFactory);

        handler = new InAppMessageResponseHandler(
                presenter
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_inAppPresenter_shouldNotBeNull() {
        new InAppMessageResponseHandler(
                null
        );
    }

    @Test
    public void testShouldHandleResponse_shouldReturnTrueWhenTheResponseHasHtmlAttribute() {
        ResponseModel response = buildResponseModel("{'message': {'html':'some html'}}");
        assertTrue(handler.shouldHandleResponse(response));
    }

    @Test
    public void testShouldHandleResponse_shouldReturnFalseWhenTheResponseHasANonJsonBody() {
        ResponseModel response = buildResponseModel("Created");
        assertFalse(handler.shouldHandleResponse(response));
    }

    @Test
    public void testShouldHandleResponse_shouldReturnFalseWhenTheResponseHasNoMessageAttribute() {
        ResponseModel response = buildResponseModel("{'not_a_message': {'html':'some html'}}");
        assertFalse(handler.shouldHandleResponse(response));
    }

    @Test
    public void testShouldHandleResponse_shouldReturnFalseWhenTheResponseHasNoHtmlAttribute() {
        ResponseModel response = buildResponseModel("{'message': {'not_html':'some html'}}");
        assertFalse(handler.shouldHandleResponse(response));
    }

    @Test
    public void testHandleResponse_shouldCallLoadMessageAsync_withCorrectArguments() {
        String html = "<p>hello</p>";
        String responseBody = String.format("{'message': {'html':'%s', 'campaignId': '123'} }", html);
        ResponseModel response = buildResponseModel(responseBody);

        handler.handleResponse(response);

        verify(webViewProvider).loadMessageAsync(eq(html), any(IamJsBridge.class), any(MessageLoadedListener.class));
    }

    @Test
    public void testHandleResponse_setsSaveDisplayIamAction_onDialog() {
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

        String html = "<p>hello</p>";
        String responseBody = String.format("{'message': {'html':'%s', 'campaignId': '123'} }", html);
        ResponseModel response = buildResponseModel(responseBody);

        handler.handleResponse(response);

        verify(dialog).setActions(captor.capture());
        List<OnDialogShownAction> actions = captor.getValue();

        assertEquals(1, CollectionTestUtils.numberOfElementsIn(actions, SaveDisplayedIamAction.class));
    }

    @Test
    public void testHandleResponse_setsSendDisplayIamAction_onDialog() {
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

        String html = "<p>hello</p>";
        String responseBody = String.format("{'message': {'html':'%s', 'campaignId': '123'} }", html);
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
                .requestModel(new RequestModel.Builder(new TimestampProvider(), new UUIDProvider())
                        .url("https://emarsys.com")
                        .build())
                .build();
    }
}