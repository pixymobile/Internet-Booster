package com.evato.intrernetoptimizer.utils.event;

import com.evato.intrernetoptimizer.model.DNSModel;

public class ServiceInfo {
    private DNSModel model;

    public ServiceInfo(DNSModel model) {
        this.model = model;
    }

    public DNSModel getModel() {
        return model;
    }

    public void setModel(DNSModel model) {
        this.model = model;
    }
}
