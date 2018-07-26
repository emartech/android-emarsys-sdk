package com.emarsys.core.request.factory;

public class DefaultRunnableFactory implements RunnableFactory {
    @Override
    public Runnable runnableFrom(Runnable runnable) {
        return runnable;
    }
}
