package com.emarsys.core.handler;

public interface Handler<T, U> {
    U handle(T item);
}
