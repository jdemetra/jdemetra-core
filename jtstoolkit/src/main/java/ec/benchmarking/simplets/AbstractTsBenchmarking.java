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

import ec.tstoolkit.data.AbsMeanNormalizer;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.TsException;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsPeriod;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public abstract class AbstractTsBenchmarking {

    private TsAggregationType type_ = TsAggregationType.Sum;
    private double eps_ = 1e-9;

    /**
     *
     * @param s
     * @param constraints
     * @return
     */
    protected abstract TsData benchmark(TsData s, TsData constraints);

    /**
     * 
     * @param s
     * @param constraints
     * @return
     */
    protected boolean checkConstraints(TsData s, TsData constraints) {
        TsData del = constraints.minus(s.changeFrequency(constraints.getFrequency(), type_, true));

        DescriptiveStatistics stats = new DescriptiveStatistics(del.getValues());
        if (stats.isZero(eps_ * stats.getStdev())) {
            return true;
        }
        return false;

    }

    /**
     * 
     * @param s
     * @param constraints
     * @return
     */
    protected TsData checkFrequencies(TsData s, TsData constraints) {
        int lfreq = constraints.getFrequency().intValue(), hfreq = s.getFrequency().intValue();
        if (hfreq == lfreq) {
            return constraints;
        }
        if (hfreq % lfreq == 0) {
            return null;
        } else {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }
    }

    /**
     * 
     * @param d
     * @param agg
     * @return
     */
    public static double[] expand(TsDomain d, TsData agg) {
        int hfreq = d.getFrequency().intValue(), lfreq = agg.getFrequency().intValue();
        int c = hfreq / lfreq;
        // expand the data;
        double[] y = new double[d.getLength()];
        for (int i = 0; i < y.length; ++i) {
            y[i] = Double.NaN;
        }
        // search the first non missing value
        TsPeriod aggstart = agg.getStart();
        TsPeriod first = new TsPeriod(d.getFrequency(), aggstart.getYear(),
                aggstart.getPosition() * c + c - 1);
        int pos = d.search(first);
        if (pos < 0) {
            return null;
        }
        int p = 0;
        while (p < agg.getLength()) {
            y[pos] = agg.get(p++);
            pos += c;
        }
        return y;
    }

    /**
     * 
     * @return
     */
    public TsAggregationType getAggregationType() {
        return type_;
    }

    /**
     * 
     * @return
     */
    public double getEpsilon() {
        return eps_;
    }

    /**
     * 
     * @param s
     * @param constraints
     * @return
     */
    public TsData process(TsData s, TsData constraints) {
        TsData r = checkFrequencies(s, constraints);
        if (r != null) {
            return null;
        }
        if (checkConstraints(s, constraints)) {
            return s;
        }
        // normalize the data
        AbsMeanNormalizer normalizer = new AbsMeanNormalizer();
        if (normalizer.process(s.getValues())) {
            TsData tmp = new TsData(s.getStart(), normalizer.getNormalizedData(), false);
            TsData btmp = benchmark(tmp, constraints.times(normalizer.getFactor()));
            if (btmp != null) {
                btmp.getValues().div(normalizer.getFactor());
            }
            return btmp;
        } else {
            return benchmark(s, constraints);
        }
    }

    /**
     * 
     * @param value
     */
    public void setAggregationType(TsAggregationType value) {
        if (value != TsAggregationType.Sum
                && value != TsAggregationType.Average) {
            throw new TsException("Unsupported benchmarking");
        }
        type_ = value;
    }

    /**
     * 
     * @param value
     */
    public void setEpsilon(double value) {
        eps_ = value;
    }
}
