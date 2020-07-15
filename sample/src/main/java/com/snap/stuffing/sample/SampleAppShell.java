package com.snap.stuffing.sample;

import android.app.Activity;

import com.snap.stuffing.api.exopackage.ExopackageApplication;

import dagger.android.AndroidInjector;
import dagger.android.HasActivityInjector;

public class SampleAppShell extends ExopackageApplication implements HasActivityInjector {

    public SampleAppShell() {
        super("com.snap.stuffing.sample.SampleDelegatingApplicationLike", BuildConfig.EXOPACKAGE_FLAGS);
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return ((HasActivityInjector) getDelegateIfPresent()).activityInjector();
    }
}
