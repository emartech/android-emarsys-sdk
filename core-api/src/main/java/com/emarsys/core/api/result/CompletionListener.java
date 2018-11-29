package com.emarsys.core.api.result;

import androidx.annotation.Nullable;

public interface CompletionListener {
    void onCompleted(@Nullable Throwable errorCause);
}