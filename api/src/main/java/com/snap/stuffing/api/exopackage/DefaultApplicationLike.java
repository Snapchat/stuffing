package com.snap.stuffing.api.exopackage;

import android.app.Application;
import android.content.res.Configuration;

/**
 * Empty implementation of {@link ApplicationLike}.
 *
 * Copy of https://github.com/facebook/buck/blob/master/android/com/facebook/buck/android/support/exopackage/DefaultApplicationLike.java
 */
public class DefaultApplicationLike implements ApplicationLike {
    public DefaultApplicationLike() {}

    @SuppressWarnings("unused")
    public DefaultApplicationLike(Application application) {}

    @Override
    public void onCreate() {}

    @Override
    public void onLowMemory() {}

    @Override
    public void onTrimMemory(int level) {}

    @Override
    public void onTerminate() {}

    @Override
    public void onConfigurationChanged(Configuration newConfig) {}

    @Override
    public Object getSystemService(String name) {
        return null;
    }
}