package com.emarsys.core.storage;

public interface Storage<T> {

    void set(T value);

    T get();

    void remove();

}
