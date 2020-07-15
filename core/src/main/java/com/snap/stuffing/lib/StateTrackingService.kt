package com.snap.stuffing.lib

import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder

/**
 * An empty, no-op service that is simply used for help track whether the [AppComponentModifier] was ever used to
 * modify the state of components in the application.
 *
 * The service uses a dummy app family, so the first time [AppComponentModifier.switchToAppFamily] is called, the
 * user enabled state for this service will change from [PackageManager.COMPONENT_ENABLED_STATE_DEFAULT] to
 * [PackageManager.COMPONENT_ENABLED_STATE_DISABLED]. This is used as an indication that components have been modified.
 */
class StateTrackingService: Service() {

    override fun onBind(intent: Intent?): IBinder? {
        // Not needed -- this service is not actually used.
        return null
    }
}