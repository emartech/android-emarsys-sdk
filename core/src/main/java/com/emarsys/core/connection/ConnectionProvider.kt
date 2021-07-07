package com.emarsys.core.connection;

import android.webkit.URLUtil;

import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.util.Assert;

import java.io.IOException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class ConnectionProvider {

    public HttpsURLConnection provideConnection(RequestModel requestModel) throws IOException {
        Assert.notNull(requestModel, "RequestModel must not be null!");

        URL url = requestModel.getUrl();

        if (!URLUtil.isHttpsUrl(url.toString())) {
            throw new IllegalArgumentException("Expected HTTPS request model, but got: " + url.getProtocol().toUpperCase());
        }

        return (HttpsURLConnection) requestModel.getUrl().openConnection();
    }
}
