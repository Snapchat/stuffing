package com.snap.stuffing.sample.first;

import android.app.Activity;
import android.app.Application;
import android.support.annotation.NonNull;

import com.snap.stuffing.api.exopackage.DefaultApplicationLike;
import com.snap.stuffing.bindings.ApplicationComponentOwner;
import com.snap.stuffing.bindings.DynamicAppModule;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;

import javax.inject.Inject;

public class FirstApplication extends DefaultApplicationLike implements HasActivityInjector, ApplicationComponentOwner {
    private final Application app;

    private FirstApplicationComponent appComponent;
    private DynamicAppModule dynamicAppModule;

    @Inject DispatchingAndroidInjector<Activity> dispatchingActivityInjector;

    public FirstApplication(Application app) {
        this.app = app;
    }

    @Override
    public void onCreate() {
        appComponent = DaggerFirstApplicationComponent
                .builder()
                .dynamicAppModule(dynamicAppModule)
                .build();
        appComponent.inject(this);
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchingActivityInjector;
    }

    @Override
    public void attachDynamicAppModule(@NonNull DynamicAppModule dynamicAppModule) {
        this.dynamicAppModule = dynamicAppModule;
    }
}
