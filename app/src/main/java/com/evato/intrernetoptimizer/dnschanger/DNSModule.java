package com.evato.intrernetoptimizer.dnschanger;

import com.evato.intrernetoptimizer.di.scope.ActivityScope;

import dagger.Module;
import dagger.Provides;

@Module
@ActivityScope
public class DNSModule {

    private IDNSView idnsView;

    public DNSModule(IDNSView idnsView) {
        this.idnsView = idnsView;
    }

    @Provides
    @ActivityScope
    IDNSView idnsView() {
        return idnsView;
    }

}
