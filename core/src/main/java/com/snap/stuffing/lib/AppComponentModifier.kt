package com.snap.stuffing.lib

import android.content.ComponentName
import android.content.Context
import android.content.pm.ComponentInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.app.Application
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Arrays

private val TAG = "AppComponentModifier"

/**
 * Responsible for enabling and disabling [Application] components based on which 'appFamily' is enabled. Components
 * in the application manifest can be tagged with an 'appFamilies' meta-data attribute used to inform which appFamilies
 * the component should be enabled for.
 */
internal class AppComponentModifier(private val context: Context) {
    private val packageManager: PackageManager by lazy { context.packageManager }

    private val queryDisableFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        PackageManager.MATCH_DISABLED_COMPONENTS
    } else {
        PackageManager.GET_DISABLED_COMPONENTS
    }

    /**
     * Gets the default [Activity] class name for the [appFamily] as specified in the application's manifest.
     *
     * Returns null if none is specified in the manifest.
     */
    fun getDefaultActivityClassNameForAppFamily(appFamily: String): String? {
        return getMainActivityForAppFamily(appFamily)?.name
    }

    /**
     * Gets an [Intent] to launch the default activity for the specified [appFamily].
     *
     * Returns null if no default activity is specified in the manifest for this appFamily.
     */
    fun getLaunchIntentForAppFamily(appFamily: String): Intent? {
        val launchActivity = getMainActivityForAppFamily(appFamily) ?: return null

        return Intent.makeMainActivity(launchActivity.componentName()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }

    /**
     * Determines if the app's component list was ever modified by checking the state of the [StateTrackingService]
     * component.
     *
     * @return True if this component was ever modified, false otherwise.
     */
    fun checkComponentListModifiedState(): Boolean {
        return packageManager.getComponentEnabledSetting(
                ComponentName(context.packageName, StateTrackingService::class.java.name)) !=
                    PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
    }

    private fun getMainActivityForAppFamily(appFamily: String): ComponentInfo? {
        return listActivities().findLast {
            it.metaData?.getString("mainForAppFamilies")?.toLowerCase().equals(appFamily.toLowerCase())
        }
    }

    /**
     * Switch the specified app family, enabling [Application] components that belong to that family and disabling those
     * that don't. Components with no listed appFamilies meta-data attribute will be enabled for any appFamily.
     *
     * @return True if any component was modified as a result of this call, false otherwise.
     */
    fun switchToAppFamily(appFamily: String, tag: String): Boolean {
        Log.d(tag, "STUFFING -- Switching to app family: $appFamily...")

        var componentsChanged = false

        listComponents().forEach {
            val appFamilies =
                    it.metaData?.getString("appFamilies")?.split(',')?.map { f -> f.trim().toLowerCase() } ?: listOf()

            if (!appFamilies.isEmpty()) {
                if (appFamilies.contains(appFamily.toLowerCase())) {
                    Log.d(tag, "STUFFING -- Enabling component: ${it.name} with appFamilies $appFamilies")
                    if (it.enable()) componentsChanged = true
                } else {
                    Log.d(tag, "STUFFING -- Disabling component: ${it.name} with appFamilies $appFamilies")
                    if (it.disable()) componentsChanged = true
                }
            } else {
                if (it.applyDefaultEnabledState()) componentsChanged = true
            }
        }

        Log.d(tag, "STUFFING -- Switching to app family: $appFamily complete. componentsChanged=$componentsChanged")
        return componentsChanged

        //printComponentEnabledStates(tag)
    }

    /**
     * Enable all [Application] components that belong to any family.
     * @return True if any component was modified as a result of this call, false otherwise.
     */
    fun resetAllComponents(tag: String): Boolean {
        Log.d(tag, "STUFFING -- Enabling all components...")

        var componentsChanged = false

        listComponents().forEach {
            Log.d(tag, "STUFFING -- Enabling component to default state: ${it.name}")
            if (it.applyDefaultEnabledState()) componentsChanged = true
        }

        Log.d(tag, "STUFFING -- Enabling all complete. componentsChanged=$componentsChanged")
        return componentsChanged

        //printComponentEnabledStates(tag)
    }

    /**
     * Logs the components in the manifest, stating whether they are enabled or disabled.
     */
    fun printComponentEnabledStates(tag: String = TAG) {
        val componentGroups = listComponents().groupBy { it.getEnabledSetting() }
        for (entry in componentGroups) {
            Log.d(tag, "STUFFING -- Enabled (${convertEnabledSettingToString(entry.key)}):\n"
                    + (entry.value.map { it.name }).joinToString("\n"))
        }
    }

    private fun convertEnabledSettingToString(componentEnabledSetting: Int): String {
        return when (componentEnabledSetting) {
            PackageManager.COMPONENT_ENABLED_STATE_DEFAULT -> "DEFAULT"
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED -> "DISABLED"
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED -> "DISABLED_UNTIL_USED"
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER -> "DISABLED_USER"
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> "ENABLED"
            else -> "UNKNOWN"
        }
    }

    private fun listComponents(): List<ComponentInfo> {
        val packageInfo = getPackageInfo(PackageManager.GET_RECEIVERS or
                PackageManager.GET_SERVICES or
                PackageManager.GET_PROVIDERS or
                PackageManager.GET_META_DATA or
                PackageManager.GET_ACTIVITIES)

        val componentInfos = mutableListOf<ComponentInfo>()
        packageInfo.services?.let { componentInfos.addAll(Arrays.asList(*packageInfo.services)) }
        packageInfo.receivers?.let { componentInfos.addAll(Arrays.asList(*packageInfo.receivers)) }
        packageInfo.providers?.let { componentInfos.addAll(Arrays.asList(*packageInfo.providers)) }
        packageInfo.activities?.let { componentInfos.addAll(Arrays.asList(*packageInfo.activities)) }
        return componentInfos
    }

    private fun listActivities(): List<ComponentInfo> {
        val packageInfo = getPackageInfo(PackageManager.GET_ACTIVITIES or PackageManager.GET_META_DATA)
        val componentInfos = mutableListOf<ComponentInfo>()
        componentInfos.addAll(Arrays.asList(*packageInfo.activities))
        return componentInfos
    }

    private fun getPackageInfo(flags: Int): PackageInfo {
        val packageInfo: PackageInfo
        try {
            packageInfo = packageManager.getPackageInfo(context.packageName, flags or queryDisableFlag)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "Couldn't query app components with flags $flags", e)
            throw RuntimeException(e)
        }
        return packageInfo
    }

    private fun ComponentInfo.getEnabledSetting(): Int {
        return packageManager.getComponentEnabledSetting(componentName())
    }

    private fun ComponentInfo.disable(): Boolean {
        return setComponentEnabledSetting(PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
    }

    private fun ComponentInfo.enable(): Boolean {
        return setComponentEnabledSetting(PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
    }

    private fun ComponentInfo.applyDefaultEnabledState(): Boolean {
        return setComponentEnabledSetting(PackageManager.COMPONENT_ENABLED_STATE_DEFAULT)
    }

    private fun ComponentInfo.setComponentEnabledSetting(value: Int): Boolean {
        val componentName = componentName()
        val previousValue = packageManager.getComponentEnabledSetting(componentName)

        if (previousValue != value) {
            packageManager.setComponentEnabledSetting(
                    componentName,
                    value,
                    PackageManager.DONT_KILL_APP)

            Log.d(TAG, "STUFFING -- Setting component $name state from ${convertEnabledSettingToString(previousValue)} "
                    + "to ${convertEnabledSettingToString(value)}")
            return true
        }

        return false
    }

    private fun ComponentInfo.componentName(): ComponentName {
        return ComponentName(packageName, name)
    }
}

