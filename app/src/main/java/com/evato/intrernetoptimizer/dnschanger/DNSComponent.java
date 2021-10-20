package com.evato.intrernetoptimizer.dnschanger;

import com.evato.intrernetoptimizer.di.component.ApplicationComponent;
import com.evato.intrernetoptimizer.di.scope.ActivityScope;

import dagger.Component;

@Component(modules = {DNSModule.class}, dependencies = {ApplicationComponent.class})
@ActivityScope
public interface DNSComponent {

    IDNSView view();

    void inject(MainActivity activity);
}
