package com.emarsys.mobileengage.di;

public class DependencyInjection {

    private static DependencyContainer container;

    public static void setup(DependencyContainer container) {
        if (DependencyInjection.container == null) {
            DependencyInjection.container = container;
        }
    }

    public static void tearDown() {
        container = null;
    }

    public static DependencyContainer getContainer() {
        if (container == null) {
            throw new IllegalStateException("DependencyInjection must be setup before accessing container!");
        }

        return container;
    }

}
