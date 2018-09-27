package com.emarsys.core.api.result;

import android.support.annotation.Nullable;

public interface CompletionListener {
    void onCompleted(@Nullable Throwable errorCause);
}