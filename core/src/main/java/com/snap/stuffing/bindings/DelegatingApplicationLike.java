package com.snap.stuffing.bindings;

import com.snap.stuffing.api.exopackage.ApplicationLike;
import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.res.Configuration;
import android.support.annotation.NonNull;

import dagger.android.AndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasBroadcastReceiverInjector;
import dagger.android.HasContentProviderInjector;
import dagger.android.HasServiceInjector;

import java.lang.reflect.Constructor;

/**
 * An {@link ApplicationLike} implementation that allows delegation to difference underling applications. The
 * {@link ApplicationLike} types that this class delegates to must be configured for Dagger injection, and implement the
 * {@link HasServiceInjector}, {@link HasBroadcastReceiverInjector} and {@link HasActivityInjector} interfaces.
 */
public abstract class DelegatingApplicationLike implements
        ApplicationLike,
        HasActivityInjector,
        HasBroadcastReceiverInjector,
        HasServiceInjector,
        HasContentProviderInjector {

    private volatile ApplicationLike mApplicationLike = null;
    protected final Application mApplication;

    protected DelegatingApplicationLike(@NonNull Application application) {
        mApplication = application;
    }

    @Override
    public void onCreate() {
        getApplication().onCreate();
    }

    @Override
    public void onLowMemory() {
        getApplication().onLowMemory();
    }

    @Override
    public void onTrimMemory(int i) {
        getApplication().onTrimMemory(i);
    }

    @Override
    public void onTerminate() {
        getApplication().onTerminate();
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        getApplication().onConfigurationChanged(configuration);
    }

    @Override
    public Object getSystemService(String s) {
        return getApplication().getSystemService(s);
    }

    public synchronized ApplicationLike getApplication() {
        if (mApplicationLike == null) {
            mApplicationLike = createApplication();
        }
        return mApplicationLike;
    }

    @NonNull
    protected abstract ApplicationLike createApplication();

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return ((HasActivityInjector) getApplication()).activityInjector();
    }

    @Override
    public AndroidInjector<BroadcastReceiver> broadcastReceiverInjector() {
        return ((HasBroadcastReceiverInjector) getApplication()).broadcastReceiverInjector();
    }

    @Override
    public AndroidInjector<Service> serviceInjector() {
        return ((HasServiceInjector) getApplication()).serviceInjector();
    }

    @Override
    public AndroidInjector<ContentProvider> contentProviderInjector() {
        return ((HasContentProviderInjector) getApplication()).contentProviderInjector();
    }

    @NonNull
    protected ApplicationLike instantiateApplicationLikeClass(@NonNull String applicationClassName) {
        try {
            Class<?> implClass = Class.forName(applicationClassName);
            Constructor<?> constructor = implClass.getConstructor(new Class[]{ Application.class });
            return (ApplicationLike) constructor.newInstance(new Object[]{ mApplication });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
