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
package ec.tstoolkit.timeseries.regression;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class ChangeOfRegime implements ITsModifier {

    private final ChangeOfRegimeType regime;
    private final Day day;
    private ITsVariable var;

    /**
     *
     * @param var
     * @param regime
     * @param day
     */
    public ChangeOfRegime(final ITsVariable var,
            final ChangeOfRegimeType regime, final Day day) {
        this.var = var;
        this.regime = regime;
        this.day = day;
    }

    @Override
    @Deprecated
    public void data(TsDomain domain, List<DataBlock> data, int start) {
        data(domain, data.subList(start, start+getDim()));
    }

    @Override
    public void data(TsDomain domain, List<DataBlock> data) {
        TsPeriod pos = new TsPeriod(domain.getFrequency());
        pos.set(day);
        int ipos = pos.minus(domain.getStart());
        // all data must have the same length!!!
        int n = domain.getLength();
        int m = var.getDim();
        if (regime == ChangeOfRegimeType.ZeroEnded) {
            if (ipos >= n) {
                var.data(domain, data);
            } else if (ipos < 0) {
                for (int i = 0; i < var.getDim(); ++i) {
                    data.get(i).set(0);
                }
            } else {
                ArrayList<DataBlock> tmp = new ArrayList<>();
                for (int i = 0; i < m; ++i) {
                    DataBlock rc = data.get(i);
                    tmp.add(rc.range(0, ipos));
                    rc.range(ipos, n).set(0);
                }
                var.data(domain.drop(0, n - ipos), tmp);
            }
        } else if (ipos >= n) {
            for (int i = 0; i < m; ++i) {
                data.get(i).set(0);
            }
        } else if (ipos < 0) {
            var.data(domain, data);
        } else {
            ArrayList<DataBlock> tmp = new ArrayList<>();
            for (int i = 0; i < m; ++i) {
                DataBlock rc = data.get(i);
                tmp.add(rc.range(ipos, n));
                rc.range(0, ipos).set(0);
            }
            var.data(domain.drop(ipos, 0), tmp);
        }
    }

    private String description(String desc) {
        StringBuilder builder = new StringBuilder();
        builder.append(desc);
        if (regime == ChangeOfRegimeType.ZeroEnded) {
            builder.append("(/").append(day.toString()).append("//)");
        } else {
            builder.append("(//").append(day.toString()).append("/)");
        }
        return builder.toString();
    }

    /**
     *
     * @return
     */
    public Day getDay() {
        return day;
    }

    @Override
    public TsDomain getDefinitionDomain() {
        return var.getDefinitionDomain();
    }

    @Override
    public TsFrequency getDefinitionFrequency() {
        return var.getDefinitionFrequency();
    }

    @Override
    public String getDescription() {
        return description(var.getDescription());
    }

    @Override
    public int getDim() {
        return var.getDim();
    }

    @Override
    public String getItemDescription(int idx) {
        return description(var.getItemDescription(idx));
    }

    /**
     *
     * @return
     */
    public ChangeOfRegimeType getRegime() {
        return regime;
    }

    @Override
    public ITsVariable getVariable() {
        return var;
    }

    // Should be checked carefully !!!
    @Override
    public boolean isSignificant(TsDomain domain) {
        TsPeriod p = new TsPeriod(domain.getFrequency());
        p.set(day);
        if (regime == ChangeOfRegimeType.ZeroEnded) {
            if (p.isNotAfter(domain.getStart())) {
                return false;
            }
            if (p.isAfter(domain.getLast())) {
                return var.isSignificant(domain);
            } else {
                int pos = domain.search(p);
                TsDomain domc = domain.drop(0, domain.getLength() - pos);
                return var.isSignificant(domc);
            }
        } else {
            if (p.isNotAfter(domain.getStart())) {
                return var.isSignificant(domain);
            }
            if (p.isAfter(domain.getLast())) {
                return false;
            } else {
                int pos = domain.search(p);
                TsDomain domc = domain.drop(pos, 0);
                return var.isSignificant(domc);
            }
        }
    }
}
