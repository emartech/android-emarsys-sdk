package com.emarsys.mobileengage.iam.webview;

import android.os.Handler;
import android.os.Looper;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.emarsys.core.util.Assert;

public class IamWebViewClient extends WebViewClient {

    private MessageLoadedListener listener;
    private Handler handler;

    public IamWebViewClient(MessageLoadedListener listener) {
        Assert.notNull(listener, "Listener must not be null!");
        this.listener = listener;
        this.handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        handler.post(new Runnable() {
            @Override
            public void run() {
                listener.onMessageLoaded();
            }
        });
    }
}
