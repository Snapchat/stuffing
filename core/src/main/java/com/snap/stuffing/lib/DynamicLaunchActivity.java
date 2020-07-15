package com.snap.stuffing.lib;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import javax.inject.Inject;

import com.snap.stuffing.api.DynamicAppManager;
import dagger.android.AndroidInjection;

/**
 * An {@link Activity} that will act as the primary activity of the application.
 * 
 * The Android launcher launches a specific Activity. Given that the specific Activity type is bound to its parent application, 
 * Stuffing provides a layer of indirection/routing when launching activities, which is this special activity.
 */
public class DynamicLaunchActivity extends Activity {
    private static final String TAG = "DynamicLaunchActivity";

    @Inject DynamicAppManager dynamicAppManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, String.format("onCreate %s", savedInstanceState));

        AndroidInjection.inject(this);

        final String defaultActivityClassName = dynamicAppManager.getDefaultActivityClassName();
        if (defaultActivityClassName == null) {
            throw new IllegalArgumentException(
                    "No default activity for appFamily " + dynamicAppManager.getApplicationFamily());
        } else {
            launchActivity(defaultActivityClassName);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, String.format("onNewIntent %s", intent));
    }

    private void launchActivity(@NonNull String activityClassName) {
        try {
            launchActivity(Class.forName(activityClassName));
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Failed to start activity " + activityClassName, e);
        }
    }

    private void launchActivity(@NonNull Class<?> activityClass) {
        final Intent intent;
        final Intent existingIntent = getIntent();
        if (existingIntent != null) {
            // If this activity was launched with a specific intent, remap it to the target activity class and
            // launch it.
            intent = (Intent) existingIntent.clone();
            intent.setComponent(new ComponentName(this, activityClass));
        } else {
            // If the activity wasn't launched with a specific intent, then just launch the target activity.
            intent = new Intent(this, activityClass);
        }
        Log.d(TAG, String.format("Dynamically launching activity class: %s with intent %s", activityClass, intent));
        startActivity(intent);
        finish();
    }
}
