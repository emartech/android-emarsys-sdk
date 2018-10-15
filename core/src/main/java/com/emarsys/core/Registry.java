package com.emarsys.core;

public interface Registry<K, V> {

    void register(K key, V value);

}
