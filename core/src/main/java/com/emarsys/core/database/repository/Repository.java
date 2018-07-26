package com.emarsys.core.database.repository;

import java.util.List;

public interface Repository<T, S> {

    void add(T item);

    void remove(S specification);

    List<T> query(S specification);

    boolean isEmpty();

}
