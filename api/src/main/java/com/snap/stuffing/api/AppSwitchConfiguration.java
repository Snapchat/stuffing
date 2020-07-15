package com.snap.stuffing.api;

import android.support.annotation.LayoutRes;

/**
 * An Optional configuration that indicates provides additional logic when switching applications. This configuration
 * applies to the Activity that is used when switching between two applications.
 */
public interface AppSwitchConfiguration {

    /**
     * The resource ID of a layout to apply to the Activity used when switching applications.
     */
    @LayoutRes
    int getAppSwitchActivityResId();

    /**
     * A hook to initialize the application that is being started. This method will be called from the interstitial
     * activity that handles application changes, and cannot invoke or display any UI.
     *
     * This is a good opportunity to run an app-specific initialization logic that might be relevant.
     */
    void startAppWarmUp();
}
