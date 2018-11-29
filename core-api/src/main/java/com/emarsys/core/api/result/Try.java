package com.emarsys.core.api.result;

import androidx.annotation.Nullable;

public class Try<T> {

    public static <T> Try success(T result) {
        return new Try<>(result, null);
    }

    public static <T> Try failure(Exception errorCause) {
        return new Try<>((T) null, errorCause);
    }

    private final T result;
    private final Throwable errorCause;

    public Try(T result, Throwable errorCause) {
        this.result = result;
        this.errorCause = errorCause;
    }

    @Nullable
    public T getResult() {
        return result;
    }

    @Nullable
    public Throwable getErrorCause() {
        return errorCause;
    }
}
