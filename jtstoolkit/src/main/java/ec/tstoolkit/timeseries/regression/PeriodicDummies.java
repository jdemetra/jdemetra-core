/*
 * Copyright 2016 National Bank of Belgium
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
package ec.tstoolkit.timeseries.regression;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.Month;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class PeriodicDummies implements ITsVariable {

    private final int period;
    private final Day start;

    public PeriodicDummies(int period) {
        this.period = period;
        start = new Day(1970, Month.January, 0);
    }

    public PeriodicDummies(int period, Day start) {
        this.period = period;
        this.start = start;
    }

    @Override
    public void data(TsDomain domain, List<DataBlock> data) {
        TsPeriod dstart = domain.getStart();
        TsPeriod rstart = new TsPeriod(dstart.getFrequency(), start);
        int pos = dstart.minus(rstart) % period;
        for (int i = 0; i < period; ++i) {
            int cur = i + pos;
            if (cur >= period) {
                cur -= period;
            }
            data.get(i).extract(cur, -1, period).set(1);
        }
    }

    @Override
    public TsDomain getDefinitionDomain() {
        return null;
    }

    @Override
    public TsFrequency getDefinitionFrequency() {
        return TsFrequency.Undefined;
    }

    @Override
    public String getDescription(TsFrequency context) {
        return "Periodic dummies";
    }

    @Override
    public int getDim() {
        return period;
    }

    @Override
    public String getItemDescription(int idx, TsFrequency context) {
        StringBuilder builder = new StringBuilder();
        builder.append("Periodic dummy [").append(idx + 1).append(']');
        return builder.toString();
    }

    @Override
    public boolean isSignificant(TsDomain domain) {
        return true;
    }

    @Override
    public String getName() {
        return "periodic#" + getDim();
    }
}
