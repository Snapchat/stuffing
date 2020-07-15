package com.snap.stuffing.bindings;

import com.snap.stuffing.api.AppSwitchHook;
import com.snap.stuffing.api.DynamicAppConfig;
import com.snap.stuffing.api.DynamicAppManager;
import com.snap.stuffing.lib.MultiDynamicAppManager;
import com.snap.stuffing.lib.SingleDynamicAppManager;
import dagger.Module;
import dagger.Provides;

import android.app.Application;
import android.support.annotation.NonNull;

import javax.inject.Singleton;

/**
 * A Dagger {@link Module} that can be used to install the {@link DynamicAppManager} into the application object graph.
 * Typically the {@link DynamicAppManager} is created before the Dagger graph.
 */
@Module
public class DynamicAppModule {
    private final DynamicAppManager dynamicAppManager;

    private DynamicAppModule(@NonNull DynamicAppManager dynamicAppManager) {
        this.dynamicAppManager = dynamicAppManager;
    }

    @Singleton
    @Provides
    public DynamicAppManager dynamicAppManager() {
        return dynamicAppManager;
    }

    @NonNull
    public static DynamicAppModule makeSingleAppModule(@NonNull Application app,
                                                       @NonNull String defaultActivityClassName,
                                                       @NonNull String defaultAppFamily) {
        return new DynamicAppModule(new SingleDynamicAppManager(app, defaultActivityClassName,
                defaultAppFamily));
    }

    @NonNull
    public static DynamicAppModule makeMultiAppModule(
            @NonNull Application app,
            @NonNull String defaultAppFamily,
            @NonNull DynamicAppConfig dynamicAppConfig,
            @NonNull AppSwitchHook appSwitchHook) {
        return new DynamicAppModule(new MultiDynamicAppManager(app, defaultAppFamily, dynamicAppConfig,
                appSwitchHook));
    }

}
