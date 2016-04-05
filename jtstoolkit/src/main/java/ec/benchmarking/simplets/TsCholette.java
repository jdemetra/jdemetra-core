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

import ec.benchmarking.ssf.SsfCholette;
import ec.benchmarking.ssf.SsfDenton;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.ssf.DisturbanceSmoother;
import ec.tstoolkit.ssf.SmoothingResults;
import ec.tstoolkit.ssf.SsfData;
import ec.tstoolkit.ssf.WeightedSsf;
import ec.tstoolkit.ssf.arima.SsfAr1;
import ec.tstoolkit.ssf.arima.SsfRw;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataBlock;
import ec.tstoolkit.timeseries.simplets.TsFrequency;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class TsCholette extends AbstractTsBenchmarking {

    public static enum BiasCorrection {

        None, Additive, Multiplicative
    };
    private BiasCorrection bias_ = BiasCorrection.None;
    private double rho_ = .9, lambda_;

    private TsData correctBias(TsData s, TsData target) {
        if (bias_ == BiasCorrection.None) {
            return s;
        }
        TsData sy = s.changeFrequency(TsFrequency.Yearly, TsAggregationType.Sum, true);
        sy = sy.fittoDomain(target.getDomain());
        if (bias_ == BiasCorrection.Multiplicative) {
            return s.times(TsDataBlock.all(target).data.sum() / TsDataBlock.all(sy).data.sum());
        } else {
            double b = TsDataBlock.all(target).data.sum() - TsDataBlock.all(sy).data.sum();
            if (getAggregationType() != TsAggregationType.Sum) {
                return s.times(b * target.getLength());
            }

            int hfreq = s.getFrequency().intValue(), lfreq = target.getFrequency().intValue();
            return s.times(b * target.getLength() * hfreq / lfreq);
        }
    }

    /**
     *
     * @return
     */
    public double getRho() {
        return rho_;
    }

    /**
     *
     * @return
     */
    public BiasCorrection getBiasCorrection() {
        return bias_;
    }

    public void setBiasCorrection(BiasCorrection bias) {
        bias_ = bias;
    }

    /**
     *
     * @param s
     * @param constraints
     * @return
     */
    private TsData archolette(TsData s, TsData target) {
        int lfreq = target.getFrequency().intValue(), hfreq = s.getFrequency().intValue();
        int c = hfreq / lfreq;

        TsData obj = s.changeFrequency(target.getFrequency(), getAggregationType(), true).minus(target);
        if (getAggregationType() == TsAggregationType.Average) {
            obj = obj.times(c);
        }

        double[] y = expand(s.getDomain(), obj, getAggregationType());

        double[] w = null;
        if (lambda_ == 1) {
            w = s.internalStorage();
        } else {
            w = new double[s.getLength()];
            TsDataBlock.all(s).data.copyTo(w, 0);
            for (int i = 0; i < w.length; ++i) {
                w[i] = Math.pow(Math.abs(w[i]), lambda_);
            }
        }

        if (getAggregationType() == TsAggregationType.Average
                || getAggregationType() == TsAggregationType.Sum) {
            SsfCholette cholette = new SsfCholette(c, rho_, w);
//        WeightedSsfDisaggregation<SsfAr1> cholette=
//                new WeightedSsfDisaggregation(c, w, new SsfAr1(ro_));
            DisturbanceSmoother dsmoother = new DisturbanceSmoother();
            dsmoother.setSsf(cholette);
            dsmoother.process(new SsfData(y, null));
            SmoothingResults drslts = dsmoother.calcSmoothedStates();

            double[] b;
            if (w != null) {
                b = new double[s.getLength()];
                for (int i = 0; i < b.length; ++i) {
                    b[i] = w[i] * (drslts.A(i).get(1));
                }
            } else {
                b = drslts.component(1);
            }

            return s.minus(new TsData(s.getStart(), b, false));
        } else {
            WeightedSsf<SsfAr1> ssf = new WeightedSsf<>(w, new SsfAr1());
            DisturbanceSmoother dsmoother = new DisturbanceSmoother();
            dsmoother.setSsf(ssf);
            dsmoother.process(new SsfData(y, null));
            SmoothingResults drslts = dsmoother.calcSmoothedStates();

            double[] b = new double[s.getLength()];
            for (int i = 0; i < b.length; ++i) {
                b[i] = ssf.ZX(i, drslts.A(i));
            }

            return s.minus(new TsData(s.getStart(), b, false));

        }
    }

    /**
     *
     * @param s
     * @param constraints
     * @return
     */
    private TsData rwcholette(TsData s, TsData target) {
        int lfreq = target.getFrequency().intValue(), hfreq = s.getFrequency().intValue();
        int c = hfreq / lfreq;

        TsData obj = s.changeFrequency(target.getFrequency(), getAggregationType(), true).minus(target);
        if (getAggregationType() == TsAggregationType.Average) {
            obj = obj.times(c);
        }

        double[] y = expand(s.getDomain(), obj, getAggregationType());

        double[] w = null;
        if (lambda_ == 1) {
            w = s.internalStorage();
        } else {
            w = new double[s.getLength()];
            TsDataBlock.all(s).data.copyTo(w, 0);
            for (int i = 0; i < w.length; ++i) {
                w[i] = Math.pow(Math.abs(w[i]), lambda_);
            }
        }

        if (getAggregationType() == TsAggregationType.Average
                || getAggregationType() == TsAggregationType.Sum) {
            SsfDenton denton = new SsfDenton(c, w);
            DisturbanceSmoother dsmoother = new DisturbanceSmoother();
            dsmoother.setSsf(denton);
            dsmoother.process(new SsfData(y, null));
            SmoothingResults drslts = dsmoother.calcSmoothedStates();

            double[] b;
            if (w != null) {
                b = new double[s.getLength()];
                for (int i = 0; i < b.length; ++i) {
                    b[i] = w[i] * (drslts.A(i).get(1));
                }
            } else {
                b = drslts.component(1);
            }
            return s.minus(new TsData(s.getStart(), b, false));
        } else {
            WeightedSsf<SsfRw> denton = new WeightedSsf<>(w, new SsfRw());
            DisturbanceSmoother dsmoother = new DisturbanceSmoother();
            dsmoother.setSsf(denton);
            dsmoother.process(new SsfData(y, null));
            SmoothingResults drslts = dsmoother.calcSmoothedStates();

            double[] b = new double[s.getLength()];
            for (int i = 0; i < b.length; ++i) {
                b[i] = denton.ZX(i, drslts.A(i));
            }

            return s.minus(new TsData(s.getStart(), b, false));

        }
    }

    /**
     *
     * @param value
     */
    public void setRho(double value) {
        rho_ = value;
    }

    public void setLambda(double lambda) {
        lambda_ = lambda;
    }

    @Override
    protected TsData benchmark(TsData s, TsData constraints) {
        s = correctBias(s, constraints);
        if (rho_ == 1) {
            return rwcholette(s, constraints);
        } else {
            return archolette(s, constraints);
        }
    }
}
