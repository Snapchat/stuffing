package com.snap.stuffing.bindings;

import android.support.annotation.NonNull;

/**
 * An entity that owns a Dagger {@link dagger.Component} for an {@link android.app.Application}.
 */
public interface ApplicationComponentOwner {

    /**
     * Attaches a {@link DynamicAppModule} to this ApplicationComponentOwner such that it can be included in the
     * application's top-level Dagger component and bound to the DI graph.
     */
    void attachDynamicAppModule(@NonNull DynamicAppModule dynamicAppModule);
}
