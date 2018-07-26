package com.emarsys.mobileengage.iam.webview;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.emarsys.mobileengage.MobileEngage;
import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.fake.FakeMessageLoadedListener;
import com.emarsys.mobileengage.iam.dialog.IamDialog;
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridge;
import com.emarsys.mobileengage.iam.jsbridge.InAppMessageHandlerProvider;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClickedRepository;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;

import static android.os.Build.VERSION_CODES.KITKAT;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TestJSInterface extends IamJsBridge {
    public TestJSInterface() {
        super(
                mock(InAppMessageHandlerProvider.class),
                mock(ButtonClickedRepository.class),
                "123",
                mock(Handler.class),
                mock(MobileEngageInternal.class));
    }

    @JavascriptInterface
    public void onPageLoaded(String json) {
    }
}

@SdkSuppress(minSdkVersion = KITKAT)
public class IamWebViewProviderTest {
    static {
        mock(IamDialog.class);
        mock(Handler.class);
    }

    private static final String BASIC_HTML = "<html><head></head><body>webview content</body></html>";

    private IamWebViewProvider provider;
    private MessageLoadedListener listener;
    private Handler handler;
    private CountDownLatch latch;
    private IamJsBridge dummyJsBridge;

    String html = String.format("<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "  <head>\n" +
            "    <script>\n" +
            "      window.onload = function() {\n" +
            "      };\n" +
            "        Android.%s(\"{success:true}\");\n" +
            "    </script>\n" +
            "  </head>\n" +
            "  <body style=\"background: transparent;\">\n" +
            "  </body>\n" +
            "</html>", "onPageLoaded");

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() throws NoSuchFieldException, IllegalAccessException {
        injectMobileEngageConfig();
        IamWebViewProvider.webView = null;

        provider = new IamWebViewProvider();
        listener = mock(MessageLoadedListener.class);

        handler = new Handler(Looper.getMainLooper());
        latch = new CountDownLatch(1);
        dummyJsBridge = new TestJSInterface();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadMessageAsync_htmlShouldNotBeNull() {
        provider.loadMessageAsync(null, dummyJsBridge, listener);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadMessageAsync_listenerShouldNotBeNull() {
        provider.loadMessageAsync(BASIC_HTML, dummyJsBridge, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadMessageAsync_jsBridgeShouldNotBeNull() {
        provider.loadMessageAsync(BASIC_HTML, null, listener);
    }

    @Test
    public void testLoadMessageAsync_shouldInvokeJsBridge_whenPageIsLoaded() throws InterruptedException {
        TestJSInterface jsInterface = mock(TestJSInterface.class);
        provider.loadMessageAsync(html, jsInterface, new FakeMessageLoadedListener(latch));
        latch.await();
        verify(jsInterface).onPageLoaded("{success:true}");
    }

    @Test
    public void testLoadMessageAsync_shouldEventuallySetWebViewOnJSBridge() throws InterruptedException {
        TestJSInterface jsInterface = mock(TestJSInterface.class);
        provider.loadMessageAsync(html, jsInterface, new FakeMessageLoadedListener(latch));
        latch.await();
        verify(jsInterface).setWebView(provider.provideWebView());
    }

    @Test
    public void testProvideWebView_shouldReturnTheStaticInstance() throws InterruptedException {
        handler.post(new Runnable() {
            @Override
            public void run() {
                IamWebViewProvider.webView = new WebView(InstrumentationRegistry.getContext());
                latch.countDown();
            }
        });

        latch.await();

        assertEquals(IamWebViewProvider.webView, provider.provideWebView());
    }

    private void injectMobileEngageConfig() throws NoSuchFieldException, IllegalAccessException {
        MobileEngageConfig config = new MobileEngageConfig.Builder()
                .application((Application) InstrumentationRegistry.getContext().getApplicationContext())
                .credentials("code", "pwd")
                .disableDefaultChannel()
                .build();

        Field configField = MobileEngage.class.getDeclaredField("config");
        configField.setAccessible(true);
        configField.set(null, config);
    }
}