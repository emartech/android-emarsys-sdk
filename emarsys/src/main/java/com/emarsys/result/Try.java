package com.emarsys.result;

import android.support.annotation.Nullable;

public class Try<T> {

    public static <T> Try success(T result) {
        return new Try<>(result, null);
    }

    public static <T> Try failure(Exception errorCause) {
        return new Try<>((T) null, errorCause);
    }

    private final T result;
    private final Throwable cause;

    public Try(T result, Throwable cause) {
        this.result = result;
        this.cause = cause;
    }

    @Nullable
    public T getResult() {
        return result;
    }

    @Nullable
    public Throwable getCause() {
        return cause;
    }
}
