package com.snap.stuffing.sample.second;

import android.app.Activity;
import android.app.Application;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import com.snap.stuffing.api.exopackage.DefaultApplicationLike;
import com.snap.stuffing.bindings.ApplicationComponentOwner;
import com.snap.stuffing.bindings.DynamicAppModule;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;

public class SecondApplication extends DefaultApplicationLike implements HasActivityInjector, ApplicationComponentOwner {
    private final Application app;

    private SecondApplicationComponent appComponent;
    private DynamicAppModule dynamicAppModule;

    @Inject DispatchingAndroidInjector<Activity> dispatchingActivityInjector;

    public SecondApplication(Application app) {
        this.app = app;
    }

    @Override
    public void onCreate() {
        appComponent = DaggerSecondApplicationComponent
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
