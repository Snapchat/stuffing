package com.snap.stuffing.api.exopackage;

import android.app.Application;
import android.content.res.Configuration;

/**
 * This interface is used to delegate calls from main Application object.
 *
 * <p>Implementations of this interface must have a one-argument constructor that takes an argument
 * of type {@link Application}.
 *
 * Copy of https://github.com/facebook/buck/blob/master/android/com/facebook/buck/android/support/exopackage/ApplicationLike.java
 */
public interface ApplicationLike {

    /** Same as {@link Application#onCreate()}. */
    void onCreate();

    /** Same as {@link Application#onLowMemory()}. */
    void onLowMemory();

    /**
     * Same as {@link Application#onTrimMemory(int level)}.
     *
     * @param level
     */
    void onTrimMemory(int level);

    /** Same as {@link Application#onTerminate()}. */
    void onTerminate();

    /** Same as {@link Application#onConfigurationChanged(Configuration newconfig)}. */
    void onConfigurationChanged(Configuration newConfig);

    /** Same as {@link Application#getSystemService(String name)}. */
    Object getSystemService(String name);
}