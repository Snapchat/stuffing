package com.snap.stuffing.api;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;

/**
 * Provides for an opportunity to execute code prior to an app family being changed.
 *
 * Unlike {@link AppSwitchConfiguration}, this is invoked prior to restarting the process.
 */
public interface AppSwitchHook {

    /**
     * Opportunity to run code prior to an app family switch.
     *
     * @param fromAppFamily from app-family
     * @param toAppFamily   to app-family
     */
    void preAppFamilySwitch(@NonNull String fromAppFamily, @NonNull String toAppFamily);
}
