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

import ec.benchmarking.ssf.SsfDenton;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.ssf.DisturbanceSmoother;
import ec.tstoolkit.ssf.SmoothingResults;
import ec.tstoolkit.ssf.SsfData;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
@Deprecated
public class TsDenton extends AbstractTsBenchmarking {

    private boolean mul_ = true;

    /**
     * 
     */
    public TsDenton() {
    }

    private TsData adenton(TsData series, TsData aggregationConstraints) {
        int lfreq = aggregationConstraints.getFrequency().intValue(), hfreq = series.getFrequency().intValue();
        int c = hfreq / lfreq;

        TsData del = aggregationConstraints.minus(series.changeFrequency(
                aggregationConstraints.getFrequency(), getAggregationType(),
                true));
        if (getAggregationType() == TsAggregationType.Average) {
            del.apply(x->x*c);
        }

        double[] y = expand(series.getDomain(), del, getAggregationType());

        SsfDenton denton = new SsfDenton(c, null);
//        WeightedSsfDisaggregation<SsfRw> denton=new WeightedSsfDisaggregation<SsfRw>(c, null, new SsfRw());
        DisturbanceSmoother dsmoother = new DisturbanceSmoother();
        dsmoother.setSsf(denton);
        dsmoother.process(new SsfData(y, null));
        SmoothingResults drslts = dsmoother.calcSmoothedStates();
//        Smoother dsmoother = new Smoother();
//        dsmoother.setSsf(denton);
//        SmoothingResults drslts = new SmoothingResults();
//        dsmoother.process(new SsfData(y, null), drslts);

        double[] b = new double[series.getLength()];
        for (int i = 0; i < b.length; ++i) {
            b[i] = series.get(i) + (drslts.A(i).get(1));
        }
        return new TsData(series.getStart(), b, false);
    }

    /**
     *
     * @param series
     * @param aggregationConstaints
     * @return
     */
    @Override
    protected TsData benchmark(TsData series, TsData aggregationConstaints) {
        if (mul_) {
            return mdenton(series, aggregationConstaints);
        } else {
            return adenton(series, aggregationConstaints);
        }
    }

    /**
     * 
     * @return
     */
    public boolean isMultiplicative() {
        return mul_;
    }

    private TsData mdenton(TsData series, TsData aggregationConstraints) {
        int lfreq = aggregationConstraints.getFrequency().intValue(), hfreq = series.getFrequency().intValue();
        int c = hfreq / lfreq;

        TsData obj = aggregationConstraints;
        if (getAggregationType() == TsAggregationType.Average) {
            obj = obj.times(c);
        }

        double[] y = expand(series.getDomain(), obj, getAggregationType());

        SsfDenton denton = new SsfDenton(c, series.internalStorage());
//        WeightedSsfDisaggregation<SsfRw> denton=new WeightedSsfDisaggregation<SsfRw>(c, series.getValues().internalStorage(), new SsfRw());
        DisturbanceSmoother dsmoother = new DisturbanceSmoother();
        dsmoother.setSsf(denton);
        dsmoother.process(new SsfData(y, null));
        SmoothingResults drslts = dsmoother.calcSmoothedStates();

        double[] b = new double[series.getLength()];
        for (int i = 0; i < b.length; ++i) {
            b[i] = series.get(i) * (drslts.A(i).get(1));
        }
        return new TsData(series.getStart(), b, false);
    }

    /**
     * 
     * @param value
     */
    public void setMultiplicative(boolean value) {
        mul_ = value;
    }
}
