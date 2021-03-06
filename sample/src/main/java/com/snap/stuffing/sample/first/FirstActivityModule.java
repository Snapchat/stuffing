package com.snap.stuffing.sample.first;

import com.snap.stuffing.api.AppSwitchConfiguration;
import com.snap.stuffing.bindings.AppSwitchActivityModule;
import com.snap.stuffing.bindings.DynamicActivityModule;
import com.snap.stuffing.sample.SampleAppSwitchConfiguration;
import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;

@Module(includes = { DynamicActivityModule.class, AppSwitchActivityModule.class})
public abstract class FirstActivityModule {
    @ContributesAndroidInjector
    public abstract FirstActivity contributeMainActivityInjector();

    @Provides
    public static AppSwitchConfiguration provideAppSwitchConfiguration() {
        return new SampleAppSwitchConfiguration("First");
    }
}
