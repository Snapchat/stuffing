package com.snap.stuffing.lib

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.app.Activity
import android.support.v4.app.FragmentActivity
import android.util.Log
import com.snap.stuffing.api.AppSwitchConfiguration
import dagger.android.AndroidInjection
import javax.inject.Inject
import javax.inject.Provider

private const val TAG = "AppToggleActivity"

/**
 * 11 second timeout. The PACKAGE_CHANGED intent should be received within 10s based on the constant in PackageManager,
 * so if it isn't received in 11s, then start the next activity.
 */
private const val TOGGLE_TIMEOUT_MILLISECONDS = 11 * 1000L

/**
 * An [Activity] responsible for managing the application switch process. It will run in a separate process, and wait
 * for to receive a [Intent.ACTION_PACKAGE_CHANGED] before allowing the transition to continue. This [Intent] signals
 * that the package manager has finished updating with the app switch changes.
 *
 * If we don't wait for this signal to be received before switching to the new application, the OS might close that
 * application once it receives that signal since it thinks the app has changed.
 *
 * This [Activity] implementation works around that by waiting for this signal, and then kicking off the launch of the
 * new intended [Activity] once it has been processed.
 *
 * This [Activity] should be launched with an [Intent] containing an extra property of type [Intent] keyed by
 * [AppSwitcher.KEY_RESTART_INTENT]. This [Intent] should be the intent that signals how to launch the new application
 * once this [Activity] is finished.
 *
 * Since the [Intent.ACTION_PACKAGE_CHANGED] is sometimes unreliable, this [Activity] also maintains a timer to time
 * out of the process after 11 seconds, which is one second longer than it usually takes to receive the
 * [Intent.ACTION_PACKAGE_CHANGED] Intent.
 */
class AppSwitchActivity : FragmentActivity() {

    private val handler = Handler(Looper.getMainLooper())

    private lateinit var relaunchIntent: Intent
    private var shouldRelaunch = false

    @Inject lateinit var configuration: Provider<AppSwitchConfiguration>

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "STUFFING -- Activity intent received: $intent")
            shouldRelaunch = true
        }
    }

    private val finishRunnable = Runnable {
        Log.d(TAG, "STUFFING -- timer expired")
        shouldRelaunch = true
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        super.onCreate(savedInstanceState)

        relaunchIntent = intent.getParcelableExtra(AppSwitcher.KEY_RESTART_INTENT)

        Log.d(TAG, "STUFFING -- activity created")

        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED)
        intentFilter.addDataScheme("package");

        applicationContext.registerReceiver(broadcastReceiver, intentFilter)
        handler.postDelayed(finishRunnable, TOGGLE_TIMEOUT_MILLISECONDS)

        configuration.get()?.let {
            setContentView(it.appSwitchActivityResId)
            configuration.get().startAppWarmUp()
        }
    }

    override fun onPause() {
        super.onPause()

        finish()
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d(TAG, "STUFFING -- activity onDestroy shouldRelaunch=$shouldRelaunch relaunchIntent=$relaunchIntent")

        handler.removeCallbacks(finishRunnable)
        applicationContext.unregisterReceiver(broadcastReceiver)

        if (shouldRelaunch) {
            AppSwitcher.endIntentToSwitchApp(applicationContext, relaunchIntent)
        } else {
            AppSwitcher.abortIntentToSwitchApp()
        }
    }
}
