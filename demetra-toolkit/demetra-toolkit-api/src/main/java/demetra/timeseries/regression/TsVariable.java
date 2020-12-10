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
package demetra.timeseries.regression;

import demetra.timeseries.TimeSeriesDomain;
import nbbrd.design.Development;
import demetra.timeseries.TsData;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public abstract class TsVariable implements ITsVariable {

    protected static TsData data(String id, ModellingContext context) {
        TsDataSupplier supplier = context.getTsVariable(id);
        if (supplier == null) {
            return null;
        } else {
            return supplier.get();
        }
    }

    private final String id;
    private final TsData data;
    private String desc;

    protected TsVariable(final String id, final TsData data, final String desc) {
        this.id = id;
        this.data = data;
        this.desc=desc;
    }

    @Override
    public <D extends TimeSeriesDomain<?>> String description(D context) {
        return desc == null ? id : desc;
    }

    public String getId() {
        return id;
    }

    public TsData getData() {
        return data;
    }

    protected boolean equals(TsVariable obj) {
        return id.equals(obj.id);
    }

    protected int hash() {
        return id.hashCode();
    }

    @Override
    public int dim() {
        return 1;
    }
}
