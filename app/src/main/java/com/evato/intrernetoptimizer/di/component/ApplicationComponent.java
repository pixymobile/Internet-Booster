package com.evato.intrernetoptimizer.di.component;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.evato.intrernetoptimizer.DNSChangerApp;
import com.evato.intrernetoptimizer.di.module.ApplicationModule;
import com.evato.intrernetoptimizer.dnschanger.DNSService;
import com.evato.intrernetoptimizer.utils.RxBus;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ApplicationModule.class})
public interface ApplicationComponent {

    DNSChangerApp dnsChangerApp();

    RxBus rxBus();

    Context appContext();

    SharedPreferences pref();

    Gson gson();

    void inject(DNSService service);

}
