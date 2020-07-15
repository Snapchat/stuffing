package com.snap.stuffing.sample.second;

import javax.inject.Singleton;

import com.snap.stuffing.bindings.DynamicAppModule;
import dagger.Component;
import dagger.android.AndroidInjectionModule;

@Singleton
@Component(modules = {
        DynamicAppModule.class,
        SecondActivityModule.class,
        AndroidInjectionModule.class
})
public interface SecondApplicationComponent {

    void inject(SecondApplication application);
}
