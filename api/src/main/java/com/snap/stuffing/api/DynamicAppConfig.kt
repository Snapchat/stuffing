package com.snap.stuffing.api

/**
 * Provides configuration for a [DynamicAppManager].
 *
 * [events] specifies string-mapped runnables what should be executed when those events are invoked om the [DynamicAppManager].
 */
data class DynamicAppConfig(val events: Map<String, Runnable>)