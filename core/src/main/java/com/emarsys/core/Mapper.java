package com.emarsys.core;

public interface Mapper<T, V> {

    V map(T value);

}
