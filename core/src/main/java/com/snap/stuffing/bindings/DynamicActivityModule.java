package com.snap.stuffing.bindings;

import com.snap.stuffing.lib.DynamicLaunchActivity;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class DynamicActivityModule {
    @ContributesAndroidInjector
    public abstract DynamicLaunchActivity contributeDynamicLaunchActivity();
}
