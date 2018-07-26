package com.emarsys.core.worker;

public interface Worker extends Lockable {
    void run();
}
