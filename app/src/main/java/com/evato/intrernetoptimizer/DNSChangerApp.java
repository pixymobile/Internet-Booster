package com.evato.intrernetoptimizer;

import android.app.Application;

import com.evato.intrernetoptimizer.di.component.ApplicationComponent;

import com.evato.intrernetoptimizer.di.component.DaggerApplicationComponent;
import com.evato.intrernetoptimizer.di.module.ApplicationModule;

import timber.log.Timber;

public class DNSChangerApp extends Application {
    private static ApplicationComponent applicationComponent;

    public static ApplicationComponent getApplicationComponent() {
        return applicationComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //di
        applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();


        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            //TODO: Add your release log tree
        }
    }
}
