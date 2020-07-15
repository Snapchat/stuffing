package com.snap.stuffing.sample.first;

import com.snap.stuffing.bindings.DynamicAppModule;
import dagger.Component;
import dagger.android.AndroidInjectionModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {
        DynamicAppModule.class,
        FirstActivityModule.class,
        AndroidInjectionModule.class
})
public interface FirstApplicationComponent {

    void inject(FirstApplication application);
}
