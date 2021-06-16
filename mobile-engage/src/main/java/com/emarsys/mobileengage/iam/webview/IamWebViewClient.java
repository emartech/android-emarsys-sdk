package com.emarsys.mobileengage.iam.webview;

import android.os.Handler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.emarsys.core.util.Assert;

public class IamWebViewClient extends WebViewClient {

    private MessageLoadedListener listener;
    private Handler uiHandler;

    public IamWebViewClient(MessageLoadedListener listener, Handler uiHandler) {
        Assert.notNull(listener, "Listener must not be null!");
        Assert.notNull(uiHandler, "UiHandler must not be null!");
        this.listener = listener;
        this.uiHandler = uiHandler;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                listener.onMessageLoaded();
            }
        });
    }
}
