/*
 * Copyright 2020 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.timeseries;

import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author PALATEJ
 */
public final class DynamicTsDataSupplier extends TsDataSupplier {

    private final TsMoniker moniker;
    private final AtomicReference<TsData> cache;

    public DynamicTsDataSupplier(TsMoniker moniker) {
        this.moniker = moniker;
        this.cache = new AtomicReference<>(null);
    }

    public DynamicTsDataSupplier(TsMoniker moniker, TsData current) {
        this.moniker = moniker;
        this.cache = new AtomicReference<>(current);
    }

    public TsMoniker getMoniker() {
        return moniker;
    }

    @Override
    public TsData get() {
        TsData cur = cache.get();
        if (cur != null) {
            return cur;
        }
        TsData load = load();
        this.cache.set(load);
        return load;
    }

    private TsData load() {
        // from the moniker.
        Ts ts = TsFactory.getDefault().makeTs(moniker, TsInformationType.Data);
        if (ts != null) {
            return ts.getData();
        } else {
            return null;
        }
    }

    public void refresh() {
        TsData ndata = load();
        if (ndata != null && (!ndata.isEmpty() || cache.get() == null)) {
            cache.set(load());
        }
    }

}
