package com.snap.stuffing.sample;

import com.snap.stuffing.api.DynamicAppConfig;
import com.snap.stuffing.api.exopackage.ApplicationLike;
import com.snap.stuffing.bindings.ApplicationComponentOwner;
import com.snap.stuffing.bindings.DelegatingApplicationLike;
import com.snap.stuffing.bindings.DynamicAppModule;
import com.snap.stuffing.sample.first.FirstApplication;
import com.snap.stuffing.sample.second.SecondApplication;

import android.app.Application;
import android.support.annotation.NonNull;

import java.util.Collections;

/**
 * A {@link DelegatingApplicationLike} implementation used to select between the two sample applications.
 */
public class SampleDelegatingApplicationLike extends DelegatingApplicationLike {

    public SampleDelegatingApplicationLike(@NonNull Application application) {
        super(application);
    }

    @NonNull
    @Override
    protected ApplicationLike createApplication() {
        final DynamicAppModule dynamicAppModule =
                DynamicAppModule.makeMultiAppModule(mApplication,
                        "first",
                        new DynamicAppConfig(Collections.emptyMap()),
                        (fromAppFamily, toAppFamily) -> {
                            // no-op
                        });

        dynamicAppModule.dynamicAppManager().initialize();

        final ApplicationLike applicationLike;
        if ("second".equals(dynamicAppModule.dynamicAppManager().getApplicationFamily())) {
            applicationLike = new SecondApplication(mApplication);
        } else {
            applicationLike = new FirstApplication(mApplication);
        }

        if (applicationLike instanceof ApplicationComponentOwner) {
            ((ApplicationComponentOwner) applicationLike).attachDynamicAppModule(dynamicAppModule);
        }

        return applicationLike;
    }
}
