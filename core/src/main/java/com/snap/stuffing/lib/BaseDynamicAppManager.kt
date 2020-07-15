package com.snap.stuffing.lib

import android.content.Context
import android.content.SharedPreferences
import com.snap.stuffing.api.DynamicAppManager

/**
 * The maximum number of failed toggle attempt to retry.
 */
internal const val MAX_FAILED_ATTEMPT_COUNT = 3

/**
 * Base for all dynamic app managers
 */
abstract class BaseDynamicAppManager(private val appContext: Context, private val tag: String): DynamicAppManager {

    internal val appComponentModifier = AppComponentModifier(appContext)

    internal val preferences: SharedPreferences by lazy {
        appContext.getSharedPreferences(DynamicAppManagerPrefs.dynamicAppConfig, Context.MODE_PRIVATE)
    }

    internal val appVersionCode: Int by lazy {
        appContext.packageManager.getPackageInfo(appContext.packageName, 0).versionCode
    }

    internal var failedToggleAttemptCount = 0

}