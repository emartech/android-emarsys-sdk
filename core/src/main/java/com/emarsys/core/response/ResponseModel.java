package com.emarsys.core.response;

import com.emarsys.core.provider.timestamp.TimestampProvider;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.util.Assert;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResponseModel {

    private final int statusCode;
    private final String message;
    private final Map<String, String> headers;
    private final Map<String, HttpCookie> cookies;
    private final String body;
    private final long timestamp;
    private final RequestModel requestModel;

    public ResponseModel(
            int statusCode,
            String message,
            Map<String, String> headers,
            Map<String, HttpCookie> cookies,
            String body,
            long timestamp,
            RequestModel requestModel) {

        checkStatusCode(statusCode);
        Assert.notNull(message, "Message must not be null!");
        Assert.notNull(headers, "Headers must not be null!");
        Assert.notNull(cookies, "Cookies must not be null!");
        Assert.notNull(requestModel, "RequestModel must not be null!");
        this.statusCode = statusCode;
        this.message = message;
        this.headers = headers;
        this.cookies = cookies;
        this.body = body;
        this.timestamp = timestamp;
        this.requestModel = requestModel;
    }

    private void checkStatusCode(int statusCode) {
        if (statusCode < 200 || statusCode >= 600) {
            throw new IllegalArgumentException("Status code must be between 2xx and 5xx!");
        }
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, HttpCookie> getCookies() {
        return cookies;
    }

    public String getBody() {
        return body;
    }

    public JSONObject getParsedBody() {
        JSONObject result = null;

        if (body != null) {
            try {
                result = new JSONObject(body);
            } catch (JSONException ignored) {
            }
        }

        return result;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public RequestModel getRequestModel() {
        return requestModel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResponseModel that = (ResponseModel) o;

        if (statusCode != that.statusCode) return false;
        if (timestamp != that.timestamp) return false;
        if (message != null ? !message.equals(that.message) : that.message != null) return false;
        if (headers != null ? !headers.equals(that.headers) : that.headers != null) return false;
        if (cookies != null ? !cookies.equals(that.cookies) : that.cookies != null) return false;
        if (body != null ? !body.equals(that.body) : that.body != null) return false;
        return requestModel != null ? requestModel.equals(that.requestModel) : that.requestModel == null;
    }

    @Override
    public int hashCode() {
        int result = statusCode;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (headers != null ? headers.hashCode() : 0);
        result = 31 * result + (cookies != null ? cookies.hashCode() : 0);
        result = 31 * result + (body != null ? body.hashCode() : 0);
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (requestModel != null ? requestModel.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ResponseModel{" +
                "statusCode=" + statusCode +
                ", message='" + message + '\'' +
                ", headers=" + headers +
                ", cookies=" + cookies +
                ", body='" + body + '\'' +
                ", timestamp=" + timestamp +
                ", requestModel=" + requestModel +
                '}';
    }

    public static class Builder {
        private int statusCode;
        private String message;
        private Map<String, String> headers;
        private Map<String, HttpCookie> cookies;
        private String body;
        private RequestModel requestModel;
        private TimestampProvider timestampProvider;

        public Builder() {
            this(new TimestampProvider());
        }

        public Builder(TimestampProvider timestampProvider) {
            this.headers = new HashMap<>();
            this.cookies = new HashMap<>();
            this.timestampProvider = timestampProvider;
        }

        public Builder statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder headers(Map<String, List<String>> headers) {
            this.headers = convertHeaders(headers);
            this.cookies = extractCookies(headers);
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Builder requestModel(RequestModel requestModel) {
            this.requestModel = requestModel;
            return this;
        }

        public ResponseModel build() {
            return new ResponseModel(
                    statusCode,
                    message,
                    headers,
                    cookies,
                    body,
                    timestampProvider.provideTimestamp(),
                    requestModel);
        }

        Map<String, String> convertHeaders(Map<String, List<String>> headers) {
            Map<String, String> result = new HashMap<>();
            Set<Map.Entry<String, List<String>>> entries = headers.entrySet();

            for (Map.Entry<String, List<String>> entry : entries) {
                result.put(entry.getKey(), join(", ", entry.getValue()));
            }
            return result;
        }

        String join(String delimiter, List<String> strings) {
            StringBuilder stringBuilder = new StringBuilder();
            Iterator<String> iterator = strings.iterator();

            if (iterator.hasNext()) {
                stringBuilder.append(iterator.next());
                while (iterator.hasNext()) {
                    stringBuilder.append(delimiter);
                    stringBuilder.append(iterator.next());
                }
            }
            return stringBuilder.toString();
        }

        private Map<String, HttpCookie> extractCookies(Map<String, List<String>> headers) {
            Map<String, HttpCookie> result = new HashMap<>();

            final String cookieKey = "set-cookie";
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                if (entry.getKey()!=null && entry.getKey().toLowerCase().equals(cookieKey)) {

                    List<String> rawCookies = entry.getValue();
                    if (rawCookies != null) {
                        for (String rawCookie : rawCookies) {
                            List<HttpCookie> cookies = HttpCookie.parse(rawCookie);
                            for (HttpCookie cookie : cookies) {
                                result.put(cookie.getName(), cookie);
                            }
                        }
                    }

                }
            }

            return result;
        }

    }
}
