package com.snap.stuffing.bindings;

import com.snap.stuffing.api.AppSwitchConfiguration;
import com.snap.stuffing.lib.AppSwitchActivity;
import dagger.BindsOptionalOf;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class AppSwitchActivityModule {

    @BindsOptionalOf
    abstract AppSwitchConfiguration appSwitchConfiguration();

    @ContributesAndroidInjector
    public abstract AppSwitchActivity contributeAppSwitchActivity();
}
