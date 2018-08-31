package com.emarsys.result;

import android.support.annotation.Nullable;

public interface CompletionListener {
    void onCompleted(@Nullable Throwable cause);
}