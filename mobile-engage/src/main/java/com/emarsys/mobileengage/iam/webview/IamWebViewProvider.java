package com.emarsys.mobileengage.iam.webview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.webkit.WebView;

import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.MobileEngage;
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridge;

public class IamWebViewProvider {

    static WebView webView;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void loadMessageAsync(final String html, final IamJsBridge jsBridge, final MessageLoadedListener messageLoadedListener) {
        Assert.notNull(html, "Html must not be null!");
        Assert.notNull(messageLoadedListener, "MessageLoadedListener must not be null!");
        Assert.notNull(jsBridge, "JsBridge must not be null!");

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @SuppressLint({"JavascriptInterface", "AddJavascriptInterface"})
            @Override
            public void run() {
                Context context = MobileEngage.getConfig().getApplication();
                webView = new WebView(context);

                jsBridge.setWebView(webView);

                webView.getSettings().setJavaScriptEnabled(true);
                webView.addJavascriptInterface(jsBridge, "Android");
                webView.setBackgroundColor(Color.TRANSPARENT);
                webView.setWebViewClient(new IamWebViewClient(messageLoadedListener));

                webView.loadData(html, "text/html", "UTF-8");
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public WebView provideWebView() {
        return webView;
    }
}
