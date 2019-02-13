package com.emarsys.mobileengage.storage;

public interface Storage<T> {

    void set(T value);

    T get();

    void remove();

}
