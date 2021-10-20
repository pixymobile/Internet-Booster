package com.evato.intrernetoptimizer.dnschanger;

import com.evato.intrernetoptimizer.model.DNSModel;

public interface IDNSView {
    void changeStatus(int serviceStatus);

    void setServiceInfo(DNSModel model);
}
