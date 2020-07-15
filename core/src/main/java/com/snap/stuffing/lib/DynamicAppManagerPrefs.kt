package com.snap.stuffing.lib

/**
 * Prefs for the dynamic app manager
 */
internal object DynamicAppManagerPrefs {
    val dynamicAppConfig = "dynamicAppConfig"
    val systemVersionKey = "systemVersion"
    val appFamilyChangeSignalKey = "appFamilyChangeSignal"
    val appFamilyKey = "appFamily"
    val expectedAppFamilyKey = "expectedAppFamily"
    val previousAppFamilyKey = "previousAppFamily"
    val previousVersionKey = "previousAppVersion"
    val failedToggleAttemptCountKey = "failedToggleAttemptCount"
}