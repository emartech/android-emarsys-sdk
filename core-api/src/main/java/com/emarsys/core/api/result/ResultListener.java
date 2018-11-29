package com.emarsys.core.api.result;

import androidx.annotation.NonNull;

public interface ResultListener<T> {
    void onResult(@NonNull T result);
}