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
package ec.benchmarking.simplets;

import ec.benchmarking.denton.DentonMethod;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class TsDenton2 extends AbstractTsBenchmarking {

    private boolean mul = true;
    private boolean modified = true;
    private int diff = 1;
    private TsFrequency defFreq = TsFrequency.Quarterly;

    /**
     *
     */
    public TsDenton2() {
    }

    /**
     *
     * @param series
     * @param aggregationConstaints
     * @return
     */
    @Override
    protected TsData benchmark(TsData series, TsData aggregationConstaints) {
        if (aggregationConstaints == null) {
            return null;
        }
        DentonMethod denton = new DentonMethod();
        denton.setAggregationType(getAggregationType());
        denton.setDifferencingOrder(diff);
        denton.setMultiplicative(mul);
        denton.setModifiedDenton(modified);
        int yfreq = aggregationConstaints.getFrequency().intValue();
        int qfreq = series != null ? series.getFrequency().intValue() : defFreq.intValue();
        if (qfreq % yfreq != 0) {
            return null;
        }
        denton.setConversionFactor(qfreq / yfreq);
        TsData tr;
        if (series != null) {
            // Y is limited to q !
            TsPeriodSelector qsel = new TsPeriodSelector();
            qsel.between(series.getStart().firstday(), series.getLastPeriod().lastday());
            aggregationConstaints = aggregationConstaints.select(qsel);
            TsPeriod q0 = series.getStart(), yq0 = new TsPeriod(q0.getFrequency());
            yq0.set(aggregationConstaints.getStart().firstday());
            denton.setOffset(yq0.minus(q0));
            double[] r = denton.process(series, aggregationConstaints);
            return new TsData(series.getStart(), r, false);
        } else {
            TsPeriod qstart = aggregationConstaints.getStart().firstPeriod(defFreq);
            double[] r = denton.process(aggregationConstaints);
            return new TsData(qstart, r, false);
        }
    }

    /**
     *
     * @return
     */
    public boolean isMultiplicative() {
        return mul;
    }

    /**
     *
     * @param value
     */
    public void setMultiplicative(boolean value) {
        mul = value;
    }

    /**
     * @return the modified
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * @param modified the modified to set
     */
    public void setModified(boolean modified) {
        this.modified = modified;
    }

    /**
     * @return the diff
     */
    public int getDifferencingOrder() {
        return diff;
    }

    /**
     * @param diff the diff to set
     */
    public void setDifferencingOrder(int diff) {
        this.diff = diff;
    }

    /**
     * @return the defFreq
     */
    public TsFrequency getDefaultFrequency() {
        return defFreq;
    }

    /**
     * @param defFreq the defFreq to set
     */
    public void setDefaultFrequency(TsFrequency defFreq) {
        this.defFreq = defFreq;
    }
}
