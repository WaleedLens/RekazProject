package org.example.core;

import com.google.inject.AbstractModule;

/**
 * This class is responsible for configuring the application's dependencies.
 * It extends the AbstractModule class from Google Guice, which allows for dependency injection.
 */
public class ApplicationModule extends AbstractModule {
    /**
     * Configures the application's dependencies.
     * Binds the RouteManager class as an eager singleton, which means an instance will be created as soon as the application starts.
     */
    @Override
    protected void configure() {
        bind(RouteManager.class).asEagerSingleton();
    }
}