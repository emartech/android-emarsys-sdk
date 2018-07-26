package com.emarsys.mobileengage.storage;

public interface Storage<T> {

    String SHARED_PREFERENCES_NAMESPACE = "ems_me_sdk";

    void set(T item);

    T get();

    void remove();

}
