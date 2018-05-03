/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package demetra.modelling.regression;

import demetra.data.DataBlock;
import demetra.design.BuilderPattern;
import demetra.design.Development;
import demetra.modelling.ComponentType;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDataSupplier;
import demetra.timeseries.TsDomain;
import java.util.List;
import javax.annotation.Nonnull;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class TsVariable implements ITsVariable<TsDomain> {

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(TsVariable.class)
    public static class Builder {

        private String name;
        private String desc;
        private TsDataSupplier supplier;
        
        public Builder data(TsDataSupplier supplier){
            this.supplier=supplier;
            return this;
        }
        
        public Builder name(String name){
            this.name=name;
            return this;
        }
        
        public Builder description(String desc){
            this.desc=desc;
            return this;
        }

        public TsVariable build(){
            if (supplier == null || name == null)
                throw new IllegalArgumentException("Incomplete information");
            return new TsVariable(supplier.get(), name, desc == null ? name : desc);
        }
    }

    private final String name;
    private final String desc;
    private final TsData tsdata;

    /**
     *
     * @param tsdata
     * @param name
     * @param desc
     */
    public TsVariable(@Nonnull TsData tsdata, @Nonnull String name, @Nonnull String desc) {
        this.tsdata = tsdata;
        this.name = name;
        this.desc = desc;
    }

    /**
     *
     * @param data
     */
    @Override
    public void data(TsDomain domain, List<DataBlock> data) {
        if (tsdata == null) {
            // should data be cleared?
            return;
        }
        DataBlock buffer = data.get(0);
        TsDomain curdom = tsdata.getDomain();
        // position of the first data (in m_ts)
        int istart = curdom.getStartPeriod().until(domain.getStartPeriod());
        // position of the last data (excluded)
        int iend = istart + domain.getLength();

        // indexes in data
        int jstart = 0, jend = domain.getLength();
        // not enough data at the beginning
        if (istart < 0) {
            buffer.range(0, -istart).set(0);
            jstart = -istart;
            istart = 0;
        }
        // not enough data at the end
        int n = domain.getLength();
        if (iend > n) {
            buffer.range(jend - iend + n, jend).set(0);
            jend = jend - iend + n;
            iend = n;
        }
        buffer.range(jstart, jend).copy(tsdata.getValues().range(istart, iend));
    }

    /**
     *
     * @return
     */
    public TsData getTsData() {
        return tsdata;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public TsVariable rename(String newname) {
        return new TsVariable(tsdata, newname, desc.equals(name) ? newname : desc); 
    }

    @Override
    public String getDescription(TsDomain context) {
        return desc;
    }

}
