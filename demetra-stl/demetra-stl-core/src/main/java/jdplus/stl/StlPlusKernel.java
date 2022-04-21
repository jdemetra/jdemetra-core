/*
 * Copyright 2016 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this fit except in compliance with the Licence.
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
package jdplus.stl;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;
import demetra.data.DoubleSeq;
import demetra.stl.SeasonalSpecification;
import demetra.stl.StlPlusSpecification;

/**
 * Java implementation of the original FORTRAN routine
 *
 * Source; R.B. Cleveland, W.S.Cleveland, J.E. McRae, and I. Terpenning, STL: A
 * Seasonal-Trend Decomposition Procedure Based on Loess, Statistics Research
 * Report, AT&T Bell Laboratories.
 *
 * @author Jean Palate
 */
public class StlPlusKernel {

    public StlPlusKernel(StlPlusSpecification spec) {
        this.spec = spec;
    }

    private final StlPlusSpecification spec;

    protected double[] y;
    protected double[][] season;
    protected double[] trend;
    protected double[] irr;
    protected double[] weights;
    protected double[] fit;

    private int n() {
        return y.length;
    }

    public StlPlusResults process(DoubleSeq data) {

        if (!initializeProcessing(data)) {
            return null;
        }
        int istep = 0;
        do {
            innerLoop();
            if (++istep > spec.getOuterLoopsCount()) {
                return finishProcessing();
            }
            if (weights == null) {
                weights = new double[n()];
                fit = new double[n()];
            }
            for (int i = 0; i < n(); ++i) {
                fit[i] = trend[i];
                for (int j = 0; j < season.length; ++j) {
                    fit[i] = op(fit[i], season[j][i]);
                }
            }
            computeRobustWeights(fit, weights);
        } while (true);
    }

    private StlPlusResults finishProcessing() {
        for (int i = 0; i < n(); ++i) {
            fit[i] = trend[i];
            for (int j = 0; j < season.length; ++j) {
                fit[i] = op(fit[i], season[j][i]);
            }
            if (Double.isFinite(y[i])) {
                irr[i] = invop(y[i], fit[i]);
            } else {
                irr[i] = mean();
            }
        }
        StlPlusResults.Builder builder = StlPlusResults.builder()
                .series(DoubleSeq.of(y))
                .trend(DoubleSeq.of(trend))
                .irregular(DoubleSeq.of(irr))
                .fit(DoubleSeq.of(fit));
        for (int i = 0; i < season.length; ++i) {
            builder.season(DoubleSeq.of(season[i]));
        }
        if (weights != null)
            builder.weights(DoubleSeq.of(weights));
        return builder.build();
    }

    private boolean initializeProcessing(DoubleSeq data) {
        int nseas = spec.getSeasonalSpecs().size();
        int n = data.length();
        y = new double[n];
        data.copyTo(y, 0);
        fit = new double[n];
        season = new double[nseas][];
        for (int i = 0; i < nseas; ++i) {
            season[i] = new double[n];
        }
        trend = new double[n];
        if (spec.isMultiplicative()) {
            Arrays.setAll(trend, i -> 1);
        }
        irr = new double[n];
        return true;
    }

    private static double mad(double[] r) {
        double[] sr = r.clone();
        Arrays.sort(sr);
        int n = r.length;
        int n2 = n >> 1;
        if (n % 2 != 0) {
            return 6 * sr[n2];
        } else {
            return 3 * (sr[n2 - 1] + sr[n2]);
        }
    }

    private void computeRobustWeights(double[] fit, double[] w) {

        int n = n();
        for (int i = 0; i < n; ++i) {
            if (Double.isFinite(y[i])) {
                w[i] = Math.abs(invop(y[i], fit[i]) - mean());
            }
        }

        double mad = mad(w);
        double wthreshold = spec.getRobustWeightThreshold();
        DoubleUnaryOperator wfn = spec.getRobustWeightFunction().asFunction();
        double c1 = wthreshold * mad;
        double c9 = (1 - wthreshold) * mad;

        for (int i = 0; i < n; ++i) {
            double r = w[i];
            if (r <= c1) {
                w[i] = 1;
            } else if (r <= c9) {
                w[i] = wfn.applyAsDouble(r / mad);
            } else {
                w[i] = 0;
            }
        }

    }

    /**
     *
     */
    protected void innerLoop() {
        int n = n();
        double[] si = new double[n];
        double[] w = new double[n];
        // Step 1: SI=Y-T

        for (int j = 0; j < spec.getInnerLoopsCount(); ++j) {

            for (int i = 0; i < n; ++i) {
                si[i] = invop(y[i], trend[i]);
            }
            // compute S
            int s = 0;
            for (SeasonalSpecification sspec : spec.getSeasonalSpecs()) {
                SeasonalFilter sfilter = SeasonalFilter.of(sspec);
                sfilter.filter(IDataGetter.of(si), weights == null ? null : k -> weights[k], spec.isMultiplicative(), IDataSelector.of(season[s]));
                if (s != season.length - 1) {
                    for (int i = 0; i < n; ++i) {
                        si[i] = invop(si[i], season[s][i]);
                    }
                }
                ++s;
            }
            // seasonal adjustment
            for (int i = 0; i < n; ++i) {
                w[i] = y[i];
                for (s = 0; s < season.length; ++s) {
                    w[i] = invop(w[i], season[s][i]);
                }
            }
            // Step 6: T=smooth(sa)
            LoessFilter tfilter = new LoessFilter(spec.getTrendSpec());
            tfilter.filter(IDataSelector.of(w), weights == null ? null : k -> weights[k], IDataSelector.of(trend));
        }
    }

    /**
     * @return the y
     */
    public double[] getY() {
        return y;
    }

    /**
     * @param i
     * @return the season
     */
    public double[] getSeason(int i) {
        return season[i];
    }

    /**
     * @return the trend
     */
    public double[] getTrend() {
        return trend;
    }

    /**
     * @return the irr
     */
    public double[] getIrr() {
        return irr;
    }

    /**
     * @return the weights
     */
    public double[] getWeights() {
        return weights;
    }

    /**
     * @return the fit
     */
    public double[] getFit() {
        return fit;
    }

    private double op(double l, double r) {
        return spec.isMultiplicative() ? l * r : l + r;
    }

    private double invop(double l, double r) {
        return spec.isMultiplicative() ? l / r : l - r;
    }

    private double mean() {
        return spec.isMultiplicative() ? 1 : 0;
    }
}
