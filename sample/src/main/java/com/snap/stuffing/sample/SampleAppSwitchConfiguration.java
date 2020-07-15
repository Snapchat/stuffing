package com.snap.stuffing.sample;

import com.snap.stuffing.api.AppSwitchConfiguration;

import android.support.annotation.NonNull;

public class SampleAppSwitchConfiguration implements AppSwitchConfiguration {

    @NonNull private final String name;

    public SampleAppSwitchConfiguration(@NonNull String name) {
        this.name = name;
    }

    @Override
    public int getAppSwitchActivityResId() {
        return R.layout.activity_app_switch;
    }

    @Override
    public void startAppWarmUp() {
    }
}
