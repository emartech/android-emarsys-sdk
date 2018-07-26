package com.emarsys.mobileengage;

public interface MobileEngageStatusListener {
    void onError(String id, Exception cause);

    void onStatusLog(String id, String log);
}
