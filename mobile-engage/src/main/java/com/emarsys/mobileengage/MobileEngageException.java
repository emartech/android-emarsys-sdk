package com.emarsys.mobileengage;

import com.emarsys.core.response.ResponseModel;

public class MobileEngageException extends Exception {
    private final int statusCode;
    private final String statusMessage;
    private final String body;

    public MobileEngageException(int statusCode, String statusMessage, String body) {
        super(statusMessage);
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.body = body;
    }

    public MobileEngageException(ResponseModel requestModel) {
        this(requestModel.getStatusCode(), requestModel.getMessage(), requestModel.getBody());
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public String getBody() {
        return body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MobileEngageException that = (MobileEngageException) o;

        if (statusCode != that.statusCode) return false;
        if (statusMessage != null ? !statusMessage.equals(that.statusMessage) : that.statusMessage != null)
            return false;
        return body != null ? body.equals(that.body) : that.body == null;
    }

    @Override
    public int hashCode() {
        int result = statusCode;
        result = 31 * result + (statusMessage != null ? statusMessage.hashCode() : 0);
        result = 31 * result + (body != null ? body.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MobileEngageException{" +
                "statusCode=" + statusCode +
                ", statusMessage='" + statusMessage + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
