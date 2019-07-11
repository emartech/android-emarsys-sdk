package com.emarsys.mobileengage;

import com.emarsys.core.api.result.CompletionListener;

public interface MobileEngageInternal {

    void setContact(String contactFieldValue, CompletionListener completionListener);

    void clearContact(CompletionListener completionListener);
}
