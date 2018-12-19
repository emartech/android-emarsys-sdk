package com.emarsys.core.util.predicate;

import java.util.List;

public class ListSizeAtLeast<T> implements Predicate<List<T>> {
    private final int count;

    public ListSizeAtLeast(int count) {
        this.count = count;
    }

    @Override
    public boolean evaluate(List<T> input) {
        return input.size() >= count;
    }
}
