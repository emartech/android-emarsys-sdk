package com.emarsys.mobileengage.inbox;

public interface InboxResultListener<T> {
    void onSuccess(T result);

    void onError(Exception cause);
}
