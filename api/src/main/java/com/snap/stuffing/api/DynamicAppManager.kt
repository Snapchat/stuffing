package com.snap.stuffing.api

import android.app.Application
import android.content.Intent

/**
 * Manages multiple "[Application]s" within a single APK.
 */
interface DynamicAppManager {
    /**
     * Indicates is this dynamic app manager is active (managing multiple applications).
     */
    val active: Boolean

    /**
     * The currently active 'application family'. Can return an empty string if none is selected.
     */
    val applicationFamily: String

    /**
     * Initializes this manager, setting up internal state based on persisted values.
     */
    fun initialize()

    /**
     * Invoked when an event that might be of interest to the [DynamicAppManager] occurs.
     */
    fun onEvent(eventName: String)

    /**
     * Gets the class name of the default [Activity] for the current [applicationFamily].
     */
    fun getDefaultActivityClassName(): String?

    /**
     * Clears internal state and returns to the default app family. Note that the process should eb restarted after
     * calling this method.
     */
    fun returnToDefaultFamily()

    /**
     * Switch to the app family specified in [appFamily]. Note that the process should eb restarted after
     * calling this method.
     */
    fun switchToAppFamily(appFamily: String, useSwitchActivity: Boolean, launchIntent: Intent?)

    /**
     * Indicates that the current application session is the first application session following an app family change.
     */
    fun hasPendingAppFamilyChangeSignal(): Boolean

    /**
     * Consume a pending app change signal such that [hasPendingAppFamilyChangeSignal] will return false until the next
     * application change event.
     */
    fun consumePendingAppFamilyChangeSignal()
}