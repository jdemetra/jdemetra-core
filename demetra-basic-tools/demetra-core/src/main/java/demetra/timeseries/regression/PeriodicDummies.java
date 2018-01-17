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
package demetra.timeseries.regression;

import demetra.data.DataBlock;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

/**
 * The periodic contrasts are defined as follows:
 * 
 * The contrasting period is by design the last period of the year. 
 * The regression variables generated that way are linearly independent.
 *
 * @author Jean Palate
 */
public class PeriodicDummies implements ITsVariable<TsDomain> {
    
    private final int period;
    private final LocalDateTime ref;
    private final String name;

    public PeriodicDummies(final int period) {
        this.period=period;
        this.ref=EPOCH;
        this.name="seas#" + (period);
    }

    public PeriodicDummies(final int period, final LocalDateTime ref) {
        this.period=period;
        this.ref=ref;
        this.name="seas#" + (period);
    }

    public PeriodicDummies(final int period, final LocalDateTime ref, final String name) {
        this.period=period;
        this.ref=ref;
        this.name=name;
    }

    @Override
    public void data(TsDomain domain, List<DataBlock> data) {
        TsPeriod refPeriod=domain.getStartPeriod().withDate(ref);
        long del=domain.getStartPeriod().getId()-refPeriod.getId();
        int pstart =(int) del%period;
        for (int i = 0; i < period; i++) {
            DataBlock x = data.get(i);
            int jstart = i - pstart;
            if (jstart < 0) {
                jstart += period;
            }
            DataBlock m = x.extract(jstart, -1, period);
            m.set(1);
        }
    }

    /**
     *
     * @return
     */
    @Override
    public String getDescription(TsDomain context) {
        StringBuilder builder = new StringBuilder();
        builder.append("Periodic dummies");
        return builder.toString();
    }

    /**
     *
     * @return
     */
    @Override
    public int getDim() {
        return period;
    }

    /**
     *
     * @param idx
     * @return
     */
    @Override
    public String getItemDescription(int idx, TsDomain context) {
        StringBuilder builder = new StringBuilder();
        builder.append("Period dummy [").append(idx + 1).append(']');
        return builder.toString();
    }

    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public PeriodicDummies rename(String nname){
        return new PeriodicDummies(period, ref, nname);
    }

}
