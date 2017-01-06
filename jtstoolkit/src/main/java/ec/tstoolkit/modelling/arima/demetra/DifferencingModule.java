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
package ec.tstoolkit.modelling.arima.demetra;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.maths.polynomials.UnitRoots;
import ec.tstoolkit.modelling.arima.IDifferencingModule;
import ec.tstoolkit.modelling.arima.IPreprocessingModule;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.modelling.arima.ProcessingResult;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.timeseries.regression.AbstractOutlierVariable;

/**
 *
 * @author Jean Palate
 */
public class DifferencingModule extends DemetraModule implements IDifferencingModule, IPreprocessingModule {

    public static final int MAXD = 2, MAXBD = 1;
    public static final double EPS = 1e-5;

    private int d, bd, freq;
    private double tmean;
    private double k = 1.2;
    private double tstat = 1.96;
    private int maxd = MAXD, maxbd = MAXBD;
    private boolean regularFirst = false;
    private boolean mad = false;

    /**
     *
     */
    public DifferencingModule() {
    }

    /**
     *
     */
    private void clear() {
        freq = 0;
        d = 0;
        bd = 0;
    }

    /**
     *
     * @return
     */
    @Override
    public int getBD() {
        return bd;
    }

    @Override
    public boolean isMeanCorrection() {
        return Math.abs(tmean) > tstat;
    }

    /**
     *
     * @return
     */
    @Override
    public int getD() {
        return d;
    }

    /**
     *
     * @return
     */
    public BackFilter getDifferencingFilter() {
        Polynomial D = UnitRoots.D(1, d);
        Polynomial BD = UnitRoots.D(freq, bd);
        return BackFilter.of(D.times(BD).getCoefficients());
    }

    private double std(IReadDataBlock z) {
        if (!mad) {
            return Math.sqrt(z.ssqc(z.average()) / z.getLength());
        } else {
            return AbstractOutlierVariable.mad(z, true);
        }
    }

    /**
     *
     * @param context
     * @return
     */
    @Override
    public ProcessingResult process(ModellingContext context) {
        try {
            // correct data for estimated outliers...
            DataBlock res;
            if (context.estimation != null) {
                int xcount = context.estimation.getRegArima().getXCount();
                int xout = context.description.getOutliers().size();

                res = context.estimation.getCorrectedData(xcount - xout, xcount);
            } else {
                res = new DataBlock(context.description.transformedOriginal());
            }
            SarimaSpecification nspec = context.description.getSpecification();
            // get residuals
            freq = context.description.getFrequency();
            process(res, freq, nspec.getD(), nspec.getBD());
            boolean changed = false;
            if (nspec.getD() != d || nspec.getBD() != bd) {
                changed = true;
                SarimaSpecification cspec = new SarimaSpecification(freq);
                cspec.setD(d);
                cspec.setBD(bd);
                context.description.setSpecification(cspec);
                context.estimation = null;
            }
            if (isMeanCorrection() != context.description.isEstimatedMean()) {
                changed = true;
                context.description.setMean(isMeanCorrection());
                context.estimation = null;
            }
//            addDifferencingInfo(context, d, bd, mean);
            return changed ? ProcessingResult.Changed : ProcessingResult.Unchanged;

        } catch (RuntimeException err) {
            context.description.setAirline(context.hasseas);
            context.estimation = null;
            return ProcessingResult.Failed;
        }
    }

    @Override
    public void process(final IReadDataBlock res, int freq, int curd, int curbd) {
        clear();
        // try alternatively to make regular/seasonal differencing
        // stop when differencing increases the variance of the series
        d = curd;
        bd = curbd;
        DataBlock z = new DataBlock(res);
        for (int i = 0; i < curd; ++i) {
            z.difference();
            z.shrink(1, 0);
        }
        for (int i = 0; i < curbd; ++i) {
            z.difference(1.0, freq);
            z.shrink(freq, 0);
        }
        double refe = std(z);
        boolean ok = d < maxd, bok = bd < maxbd;
        do {
            DataBlock tmp;
            double e;
            if (regularFirst && d < maxd && ok) {
                tmp = z.deepClone();
                tmp.difference();
                tmp.shrink(1, 0);
                e = std(tmp);
                if (e < refe * k) {
                    z = tmp;
                    refe = e;
                    ++d;
                    ok = d < maxd;
                    bok = bd < maxbd;
                } else {
                    ok = false;
                }
            }
            if (bd < maxbd && bok) {
                tmp = z.deepClone();
                tmp.difference(1.0, freq);
                tmp.shrink(freq, 0);
                e = std(tmp);
                if (e < refe * k) {
                    refe = e;
                    z = tmp;
                    ++bd;
                    bok = bd < maxbd;
                    ok = d < maxd;
                } else {
                    bok = false;
                }
            }
            if (!regularFirst && d < maxd && ok) {
                tmp = z.deepClone();
                tmp.difference();
                tmp.shrink(1, 0);
                e = std(tmp);
                if (e < refe * k) {
                    z = tmp;
                    refe = e;
                    ++d;
                    ok = d < maxd;
                    bok = bd < maxbd;
                } else {
                    ok = false;
                }
            }
        } while (ok || bok);

        testMean(z);
    }

    private void testMean(DataBlock z) {
        double s = z.sum(), s2 = z.ssq();
        int n = z.getLength();
        tmean = s / Math.sqrt((s2 * n - s * s) / n);
    }

    /**
     * @return the tmean
     */
    public double getTmean() {
        return tmean;
    }

    /**
     * @return the k
     */
    public double getK() {
        return k;
    }

    /**
     * @param k the k to set
     */
    public void setK(double k) {
        this.k = k;
    }

    /**
     * @return the tstat
     */
    public double getTstat() {
        return tstat;
    }

    /**
     * @param tstat the tstat to set
     */
    public void setTstat(double tstat) {
        this.tstat = tstat;
    }

    @Override
    public void setLimits(int maxd, int maxbd) {
        this.maxd = maxd;
        this.maxbd = maxbd;
    }

    /**
     * @return the regularFirst
     */
    public boolean isRegularFirst() {
        return regularFirst;
    }

    /**
     * @param regularFirst the regularFirst to set
     */
    public void setRegularFirst(boolean regularFirst) {
        this.regularFirst = regularFirst;
    }

    /**
     * @return the mad
     */
    public boolean isMad() {
        return mad;
    }

    /**
     * @param mad the mad to set
     */
    public void setMad(boolean mad) {
        this.mad = mad;
    }

}
