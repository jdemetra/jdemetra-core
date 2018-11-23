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
import demetra.maths.matrices.Matrix;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * The seasonal dummies are in fact seasonal contrasts. The contrasting period
 * is by design the last period of the year. The regression variables generated
 * that way are linearly independent.
 *
 * @author Gianluca Caporello
 */
public class SeasonalDummies implements ITsVariable<TsDomain> {

    private final int period;
    private final LocalDateTime ref;
    private final String name;

    public SeasonalDummies(final int period) {
        this.period = period;
        this.ref = TsPeriod.DEFAULT_EPOCH;
        this.name = "seas#" + (period);
    }

    public SeasonalDummies(final int period, final LocalDateTime ref) {
        this.period = period;
        this.ref = ref;
        this.name = "seas#" + (period);
    }

    public SeasonalDummies(final int period, final LocalDateTime ref, final String name) {
        this.period = period;
        this.ref = ref;
        this.name = name;
    }

    public Matrix matrix(int length, int start) {
        Matrix M = Matrix.make(length, period-1);
        int pstart = start % period;
        int lstart = period - pstart - 1;
        if (lstart < 0) {
            lstart += period;
        }
        for (int i = 0; i < period - 1; i++) {
            DataBlock x = M.column(i);
            int jstart = i - pstart;
            if (jstart < 0) {
                jstart += period;
            }
            DataBlock m = x.extract(jstart, -1, period);
            m.set(1);
            DataBlock q = x.extract(lstart, -1, period);
            q.set(-1);
        }
        return M;
    }

    @Override
    public void data(TsDomain domain, List<DataBlock> data) {
        TsPeriod refPeriod = domain.getStartPeriod().withDate(ref);
        long del = domain.getStartPeriod().getId() - refPeriod.getId();
        int pstart = (int) del % period;
        int lstart = period - pstart - 1;
        if (lstart < 0) {
            lstart += period;
        }
        for (int i = 0; i < period - 1; i++) {
            DataBlock x = data.get(i);
            int jstart = i - pstart;
            if (jstart < 0) {
                jstart += period;
            }
            DataBlock m = x.extract(jstart, -1, period);
            m.set(1);
            DataBlock q = x.extract(lstart, -1, period);
            q.set(-1);
        }
    }

    /**
     *
     * @return
     */
    @Override
    public String getDescription(TsDomain context) {
        StringBuilder builder = new StringBuilder();
        builder.append("Seasonal dummies");
        return builder.toString();
    }

    /**
     *
     * @return
     */
    @Override
    public int getDim() {
        return period-1;
    }

    /**
     *
     * @param idx
     * @return
     */
    @Override
    public String getItemDescription(int idx, TsDomain context) {
        StringBuilder builder = new StringBuilder();
        builder.append("Seasonal dummy [").append(idx + 1).append(']');
        return builder.toString();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PeriodicDummies rename(String nname) {
        return new PeriodicDummies(period, ref, nname);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other instanceof SeasonalDummies) {
            SeasonalDummies x = (SeasonalDummies) other;
            return x.period == period && x.ref.equals(ref);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + this.period;
        hash = 47 * hash + Objects.hashCode(this.ref);
        return hash;
    }


}
