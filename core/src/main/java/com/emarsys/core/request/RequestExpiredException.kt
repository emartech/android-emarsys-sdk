package com.emarsys.core.request;

public class RequestExpiredException extends Exception {
    private final String endpoint;

    public RequestExpiredException(String message, String endpoint) {
        super(message);
        this.endpoint = endpoint;
    }

    public String getEndpoint() {
        return endpoint;
    }
}
