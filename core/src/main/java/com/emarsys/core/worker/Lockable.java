package com.emarsys.core.worker;

public interface Lockable {
    void lock();

    void unlock();

    boolean isLocked();
}
