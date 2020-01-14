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

/**
 * Java implementation of the original FORTRAN routine
 *
 * Source; R.B. Cleveland, W.S.Cleveland, J.E. McRae, and I. Terpenning, STL: A
 * Seasonal-Trend Decomposition Procedure Based on Loess, Statistics Research
 * Report, AT&T Bell Laboratories.
 *
 * @author Jean Palate
 */
public class StlPlus {
    
    protected static final DoubleUnaryOperator W = x -> {
        double t = 1 - x * x * x;
        return t * t * t;
    };
    
    private final LoessFilter tfilter;
    private final SeasonalFilter[] sfilter;
    protected DoubleUnaryOperator wfn = x -> {
        double t = 1 - x * x;
        return t * t;
    };
    
    protected DoubleUnaryOperator loessfn = x -> {
        double t = 1 - x * x * x;
        return t * t * t;
    };
    
    protected double[] y;
    protected double[][] season;
    protected double[] trend;
    protected double[] irr;
    protected double[] weights;
    protected double[] fit;
    private boolean mul;
    
    private int ni = 2, no = 0;
    private double wthreshold = .001;
    
    private int n() {
        return y.length;
    }
    
    public StlPlus(final LoessFilter tfilter, final SeasonalFilter sfilter) {
        this.tfilter = tfilter;
        this.sfilter = new SeasonalFilter[]{sfilter};
    }
    
    public StlPlus(final LoessFilter tfilter, final SeasonalFilter[] sfilter) {
        this.tfilter = tfilter;
        this.sfilter = sfilter;
    }
    
    public StlPlus(final int period, final int swindow) {
        LoessSpecification sspec = LoessSpecification.of(swindow, 0);
        int twindow = (int) Math.ceil((1.5 * period) / (1 - 1.5 / swindow));
        if (twindow % 2 == 0) {
            ++twindow;
        }
        LoessSpecification tspec = LoessSpecification.of(twindow);
        tfilter = new LoessFilter(tspec);
        sfilter = new SeasonalFilter[]{new SeasonalFilter(sspec, LoessSpecification.of(period + 1), period)};
    }
    
    public boolean process(DoubleSeq data) {
        
        if (!initializeProcessing(data)) {
            return false;
        }
        int istep = 0;
        do {
            innerLoop();
            if (++istep > no) {
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
    
    private boolean finishProcessing() {
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
        return true;
    }
    
    private boolean initializeProcessing(DoubleSeq data) {
        int n = data.length();
        y = new double[n];
        data.copyTo(y, 0);
        fit = new double[n];
        season = new double[sfilter.length][];
        for (int i = 0; i < sfilter.length; ++i) {
            season[i] = new double[n];
        }
        trend = new double[n];
        if (mul) {
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

        for (int j = 0; j < ni; ++j) {
            
            for (int i = 0; i < n; ++i) {
                si[i] = invop(y[i], trend[i]);
            }
            // compute S
            for (int s = 0; s < sfilter.length; ++s) {
                sfilter[s].filter(IDataGetter.of(si), weights == null ? null : k -> weights[k], mul, IDataSelector.of(season[s]));
                if (s != sfilter.length - 1) {
                    for (int i = 0; i < n; ++i) {
                        si[i] = invop(si[i], season[s][i]);
                    }
                }
            }
            // seasonal adjustment
            for (int i = 0; i < n; ++i) {
                w[i] = y[i];
                for (int s = 0; s < sfilter.length; ++s) {
                    w[i] = invop(w[i], season[s][i]);
                }
            }
            // Step 6: T=smooth(sa)
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

    /**
     * @return the tfilter
     */
    public LoessFilter getTfilter() {
        return tfilter;
    }

    /**
     * @return sfilter1lter
     */
    public SeasonalFilter[] getSfilter() {
        return sfilter;
    }

    /**
     * @return the ni
     */
    public int getNi() {
        return ni;
    }

    /**
     * @param ni the ni to set
     */
    public void setNi(int ni) {
        this.ni = ni;
    }

    /**
     * @return the no
     */
    public int getNo() {
        return no;
    }

    /**
     * @param no the no to set
     */
    public void setNo(int no) {
        this.no = no;
    }

    /**
     * @return the wthreshold
     */
    public double getWthreshold() {
        return wthreshold;
    }

    /**
     * @param wthreshold the wthreshold to set
     */
    public void setWthreshold(double wthreshold) {
        this.wthreshold = wthreshold;
    }
    
    public void setMultiplicative(boolean multiplicative) {
        mul = multiplicative;
    }

    /**
     * @return the mul
     */
    public boolean isMultiplicative() {
        return mul;
    }
    
    private double op(double l, double r) {
        return mul ? l * r : l + r;
    }
    
    private double invop(double l, double r) {
        return mul ? l / r : l - r;
    }
    
    private double mean() {
        return mul ? 1 : 0;
    }
}
