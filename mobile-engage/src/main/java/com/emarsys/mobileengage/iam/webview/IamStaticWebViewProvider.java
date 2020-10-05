package com.emarsys.mobileengage.iam.webview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.webkit.WebView;

import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridge;

public class IamStaticWebViewProvider {

    static WebView webView;

    private final Context context;

    public IamStaticWebViewProvider(Context context) {
        Assert.notNull(context, "Context must not be null!");
        this.context = context;
    }

    public void loadMessageAsync(final String html, final IamJsBridge jsBridge, final MessageLoadedListener messageLoadedListener) {
        Assert.notNull(html, "Html must not be null!");
        Assert.notNull(messageLoadedListener, "MessageLoadedListener must not be null!");
        Assert.notNull(jsBridge, "JsBridge must not be null!");

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @SuppressLint({"JavascriptInterface", "AddJavascriptInterface"})
            @Override
            public void run() {
                webView = new WebView(context);

                jsBridge.setWebView(webView);

                webView.getSettings().setJavaScriptEnabled(true);
                webView.addJavascriptInterface(jsBridge, "Android");
                webView.setBackgroundColor(Color.TRANSPARENT);
                webView.setWebViewClient(new IamWebViewClient(messageLoadedListener));

                webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
            }
        });
    }

    public WebView provideWebView() {
        return webView;
    }
}