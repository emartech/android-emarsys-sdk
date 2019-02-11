package com.emarsys.mobileengage.storage;

public interface Storage<T> {

    void set(T item);

    T get();

    void remove();

}
