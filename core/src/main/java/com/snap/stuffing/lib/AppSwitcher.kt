package com.snap.stuffing.lib

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK

/**
 * Provides an entry point for invoking the flow to switch applications.
 *
 * [beginIntentToSwitchApp] should be invoked from the initial Application, and [endIntentToSwitchApp] will be invoked
 * by the [AppSwitchActivity] when it determines that the OS has completed the necessary updates to switch applications.
 */
internal class AppSwitcher {

    companion object {
        const val KEY_RESTART_INTENT = "RESTART_INTENTS"

        /**
         * Invoke from the initial application to start the app switch flow. The calling process will be killed, and
         * a [AppSwitchActivity] will be launched in a new process to manage the transition to the new activity.
         */
        fun beginIntentToSwitchApp(context: Context, intent: Intent? = null) {
            val switcherIntent = Intent(context, AppSwitchActivity::class.java)

            switcherIntent.addFlags(FLAG_ACTIVITY_NEW_TASK)
            intent?.let {
                switcherIntent.putExtra(KEY_RESTART_INTENT, it)
            }

            context.startActivity(switcherIntent)
            Runtime.getRuntime().exit(0)
        }

        /**
         * Invoked from the [AppSwitchActivity] process to complete the transition to the new Application.
         */
        fun endIntentToSwitchApp(context: Context, intent: Intent) {
            context.startActivity(intent)
            Runtime.getRuntime().exit(0)
        }

        /**
         * Aborts the app switch intent. The app will still have switched, but it will not be launched automatically
         * after this point.
         */
        fun abortIntentToSwitchApp() {
            Runtime.getRuntime().exit(0)
        }
    }
}