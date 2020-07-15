package com.snap.stuffing.lib

import android.content.Context
import android.content.Intent
import android.util.Log
import com.snap.stuffing.api.DynamicAppManager

private const val TAG = "SingleDynamicAppManager"
/**
 * A [DynamicAppManager] implementation for application that are not actually dynamic, and contain only a single
 * application.
 */
internal class SingleDynamicAppManager constructor(appContext: Context,
                                                   private val defaultActivityClassName: String,
                                                   private val defaultAppFamily: String
): BaseDynamicAppManager(appContext, TAG) {

    override val active: Boolean = false

    override val applicationFamily = defaultAppFamily

    override fun getDefaultActivityClassName(): String? = defaultActivityClassName

    override fun initialize() {
        val currentAppFamily = preferences.getString(DynamicAppManagerPrefs.appFamilyKey, "")
        failedToggleAttemptCount = preferences.getInt(DynamicAppManagerPrefs.failedToggleAttemptCountKey, 0)

        Log.d(TAG, "STUFFING -- Initializing with state: " +
                "currentAppFamily=$currentAppFamily, " +
                "expectedAppFamily=$applicationFamily, " +
                "failedToggleAttemptCount=$failedToggleAttemptCount")

        if (currentAppFamily != applicationFamily && failedToggleAttemptCount < MAX_FAILED_ATTEMPT_COUNT) {
            preferences.edit()
                    .putInt(DynamicAppManagerPrefs.failedToggleAttemptCountKey, failedToggleAttemptCount + 1)
                    .commit()

            appComponentModifier.resetAllComponents(TAG)

            preferences.edit()
                    .putString(DynamicAppManagerPrefs.appFamilyKey, applicationFamily)
                    .putString(DynamicAppManagerPrefs.expectedAppFamilyKey, applicationFamily)
                    .putInt(DynamicAppManagerPrefs.previousVersionKey, appVersionCode)
                    .putInt(DynamicAppManagerPrefs.failedToggleAttemptCountKey, 0)
                    .putString(DynamicAppManagerPrefs.previousAppFamilyKey, currentAppFamily)
                    .commit()
        }
    }

    override fun onEvent(eventName: String) { }

    override fun returnToDefaultFamily() { }

    override fun switchToAppFamily(appFamily: String, useSwitchActivity: Boolean, launchIntent: Intent?) { }

    override fun hasPendingAppFamilyChangeSignal() = false

    override fun consumePendingAppFamilyChangeSignal() { }
}
