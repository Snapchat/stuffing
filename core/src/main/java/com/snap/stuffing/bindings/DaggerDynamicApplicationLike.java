package com.snap.stuffing.bindings;

import com.snap.stuffing.api.exopackage.ApplicationLike;
import com.snap.stuffing.api.DynamicAppManager;
import dagger.android.HasActivityInjector;
import dagger.android.HasBroadcastReceiverInjector;
import dagger.android.HasServiceInjector;

import android.app.Application;
import android.content.res.Configuration;
import android.support.annotation.NonNull;

/**
 * A {@link ApplicationLike} implementation for applications that user Dagger. Requires a {@link DynamicAppModule} as
 * a parameter, which can be added to the main application {@link dagger.Component} to provider bindings for the
 * {@link DynamicAppManager} which is created VERY early in the process lifecycle.
 */
public abstract class DaggerDynamicApplicationLike implements
        ApplicationLike,
        ApplicationComponentOwner,
        HasActivityInjector,
        HasBroadcastReceiverInjector,
        HasServiceInjector {
    protected final Application application;
    protected DynamicAppModule dynamicAppModule;

    protected DaggerDynamicApplicationLike(@NonNull Application application) {
        this.application = application;
    }

    @Override
    public void attachDynamicAppModule(@NonNull DynamicAppModule dynamicAppModule) {
        this.dynamicAppModule = dynamicAppModule;
    }

    @Override
    public void onCreate() {}

    @Override
    public void onLowMemory() {}

    @Override
    public void onTrimMemory(int var1) {}

    @Override
    public void onTerminate() {}

    @Override
    public void onConfigurationChanged(Configuration var1) {}

    @Override
    public Object getSystemService(String var1) { return null; }
}
