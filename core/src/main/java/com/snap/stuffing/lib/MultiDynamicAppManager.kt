package com.snap.stuffing.lib

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import com.jakewharton.processphoenix.ProcessPhoenix
import com.snap.stuffing.api.AppSwitchHook
import com.snap.stuffing.api.DynamicAppConfig

private const val TAG = "MultiDynamicAppManager"

/**
 * Internal version code used to invalidate stored preferences for breaking changes.
 *
 * Version 1: Switched from using DynamicLaunchActivity to enabling/disabling activities.
 * Version 2: Add new metadata.
 * Version 1: Switched back to DynamicLaunchActivity.
 */
private const val SYSTEM_VERSION = 2

/**
 * Manages multiple "[Application]s" within a single APK.
 */
class MultiDynamicAppManager(
        private val appContext: Context,
        private val defaultAppFamily: String,
        private val config: DynamicAppConfig,
        private val appSwitchHook: AppSwitchHook): BaseDynamicAppManager(appContext, TAG) {

    private var hasAppFamilyChangeSignal = false

    override val active: Boolean = true

    override var applicationFamily: String = ""
        private set

    override fun initialize() {
        hasAppFamilyChangeSignal = preferences.getBoolean(DynamicAppManagerPrefs.appFamilyChangeSignalKey, false)
        val currentAppFamily = preferences.getString(DynamicAppManagerPrefs.appFamilyKey, null)
        val expectedAppFamily = preferences.getString(DynamicAppManagerPrefs.expectedAppFamilyKey, null)
        val previousAppVersionCode = preferences.getInt(DynamicAppManagerPrefs.previousVersionKey, 0)
        val systemVersion = preferences.getInt(DynamicAppManagerPrefs.systemVersionKey, 0)
        val hadComponentsModified = appComponentModifier.checkComponentListModifiedState()
        failedToggleAttemptCount = preferences.getInt(DynamicAppManagerPrefs.failedToggleAttemptCountKey, 0)

        Log.d(TAG, "STUFFING -- Initializing with state: " +
                "previousAppVersionCode=$previousAppVersionCode, appVersionCode=$appVersionCode, " +
                "hasAppFamilyChangeSignal=$hasAppFamilyChangeSignal currentAppFamily=$currentAppFamily, " +
                "expectedAppFamily=$expectedAppFamily, " +
                "systemVersion=$systemVersion, hadComponentsModified=$hadComponentsModified, " +
                "failedToggleAttemptCount=$failedToggleAttemptCount")

        if (systemVersion != SYSTEM_VERSION || currentAppFamily.isNullOrEmpty() ||
                (failedToggleAttemptCount in 1..MAX_FAILED_ATTEMPT_COUNT)) {

            val componentsModified = setCurrentAppFamily(defaultAppFamily, !hadComponentsModified)
            if (hadComponentsModified && componentsModified) {
                Log.d(TAG, "Rebooting the application since the appFamily changed some components")
                ProcessPhoenix.triggerRebirth(appContext)
            }

        } else {
            applicationFamily = currentAppFamily

            // When the app family hasn't changed, check if the app version was updated.
            // If it was, we might need to refresh the state of newly added or removed manifest components.
            if (appVersionCode != previousAppVersionCode) {
                Log.d(TAG, "STUFFING -- appVersionCode changed from $previousAppVersionCode to $appVersionCode, refreshing manifest components")
                refreshManifestComponentsOnInconsistency()
            } else if (currentAppFamily != expectedAppFamily) {
                Log.d(TAG, "STUFFING -- currentAppFamily $currentAppFamily does not match expectedAppFamily $expectedAppFamily, refreshing manifest components")
                refreshManifestComponentsOnInconsistency()
            }
        }

        // This method is costly, only enable it while debugging locally.
        //appComponentModifier.printComponentEnabledStates(TAG)
    }

    override fun onEvent(eventName: String) {
        config.events[eventName]?.run()
    }

    override fun getDefaultActivityClassName() = appComponentModifier.getDefaultActivityClassNameForAppFamily(applicationFamily)

    override fun returnToDefaultFamily() {
        Log.d(TAG, "STUFFING -- returnToDefaultFamily")

        setCurrentAppFamily(defaultAppFamily, false)
    }

    override fun switchToAppFamily(appFamily: String, useSwitchActivity: Boolean, launchIntent: Intent?) {
        Log.d(TAG, "STUFFING -- switchToAppFamily $appFamily")

        setCurrentAppFamily(appFamily, false)

        val relaunchIntent = appComponentModifier.getLaunchIntentForAppFamily(applicationFamily)?.apply {
            if (launchIntent != null && launchIntent.extras != null) {
                putExtras(launchIntent)
            }
        }

        if (useSwitchActivity) {
            if (relaunchIntent != null) {
                AppSwitcher.beginIntentToSwitchApp(appContext, relaunchIntent)
            } else {
                AppSwitcher.beginIntentToSwitchApp(appContext)
            }
        } else {
            if (relaunchIntent != null) {
                AppSwitcher.endIntentToSwitchApp(appContext, relaunchIntent)
            } else {
                ProcessPhoenix.triggerRebirth(appContext)
            }
        }
    }

    override fun hasPendingAppFamilyChangeSignal() = hasAppFamilyChangeSignal

    override fun consumePendingAppFamilyChangeSignal() {
        Log.d(TAG, "STUFFING -- Consuming pending appFamilyChangeSignal")

        hasAppFamilyChangeSignal = false
        preferences.edit().putBoolean(DynamicAppManagerPrefs.appFamilyChangeSignalKey, false).apply()
    }

    private fun setCurrentAppFamily(appFamily: String, isFirstTimeSettingAppFamily: Boolean): Boolean {
        // Already in this app family, return
        if (appFamily == this.applicationFamily) {
            Log.d(TAG, "STUFFING -- Already in ${this.applicationFamily}")
            return false
        }

        Log.d(TAG, "STUFFING -- Switching app families from ${this.applicationFamily} to $appFamily, appVersionCode=$appVersionCode")

        // Inform custom hook that we're about to switch
        appSwitchHook.preAppFamilySwitch(this.applicationFamily, appFamily)

        preferences.edit()
                .putInt(DynamicAppManagerPrefs.failedToggleAttemptCountKey, failedToggleAttemptCount + 1)
                .commit()

        // Update the manifest components for this app family
        val componentsModified = appComponentModifier.switchToAppFamily(appFamily, TAG)

        // Clear preferences and set all of the values to an initial state.
        preferences.edit()
                .clear()
                .putString(DynamicAppManagerPrefs.appFamilyKey, appFamily)
                .putString(DynamicAppManagerPrefs.expectedAppFamilyKey, appFamily)
                .putString(DynamicAppManagerPrefs.previousAppFamilyKey, this.applicationFamily)
                .putBoolean(DynamicAppManagerPrefs.appFamilyChangeSignalKey, !isFirstTimeSettingAppFamily)
                .putInt(DynamicAppManagerPrefs.previousVersionKey, appVersionCode)
                .putInt(DynamicAppManagerPrefs.systemVersionKey, SYSTEM_VERSION)
                .putInt(DynamicAppManagerPrefs.failedToggleAttemptCountKey, 0)
                .commit()

        applicationFamily = appFamily
        return componentsModified
    }

    /**
     * If the app version has changed, it's possible that manifest components were added that
     * don't belong to the current app family, and need to be updated.
     */
    internal fun refreshManifestComponentsOnInconsistency() {
        Log.d(TAG, "refreshManifestComponentsOnInconsistency: $applicationFamily")

        preferences.edit()
                .putInt(DynamicAppManagerPrefs.failedToggleAttemptCountKey, failedToggleAttemptCount + 1)
                .commit()

        appComponentModifier.switchToAppFamily(applicationFamily, TAG)

        preferences.edit()
                .putString(DynamicAppManagerPrefs.expectedAppFamilyKey, applicationFamily)
                .putInt(DynamicAppManagerPrefs.previousVersionKey, appVersionCode)
                .putInt(DynamicAppManagerPrefs.failedToggleAttemptCountKey, 0)
                .commit()
    }
}