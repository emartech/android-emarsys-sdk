package com.emarsys.mobileengage;

import com.emarsys.core.api.result.CompletionListener;

public interface RefreshTokenInternal {

    void refreshContactToken(CompletionListener completionListener);
}
