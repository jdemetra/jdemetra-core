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
package ec.satoolkit.x11;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.design.NewObject;
import ec.tstoolkit.timeseries.simplets.PeriodIterator;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import ec.tstoolkit.timeseries.simplets.YearIterator;

/**
 * Default implementation for the correction of extreme values
 *
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Beta)
class DefaultExtremeValuesCorrector extends DefaultX11Algorithm
        implements IExtremeValuesCorrector {

    /**
     * Returns the averages by period
     *
     * @param s The analyzed time series
     * @return An array of doubles (length = annual frequency), with the
     * averages for each period (months/quarters) of the year.
     */
    public static double[] periodAverages(TsData s) {
        int freq = s.getFrequency().intValue();
        double[] outs = new double[freq];
        PeriodIterator bi = new PeriodIterator(s);
        for (int i = 0; i < freq; ++i) {
            DataBlock bd = bi.nextElement().data;
            outs[i] = bd.sum() / bd.getLength();
        }
        return outs;
    }
    protected double lsigma = 1.5, usigma = 2.5;
    protected double[] stdev;
    protected TsData scur, scorr, sweights;
    protected boolean isexcludefcast;
    //protected int forcasthorizont;

    /**
     * Searches the extreme values in a given series
     *
     * @param s The analysed series
     * @return The number of extreme values that have been detected (>= 0)
     */
    @Override
    public int analyse(final TsData s) {
        TsData scurwithfcast;
        scur = s;
        sweights = null;
        scorr = null;
        // compute standard deviations
        scurwithfcast = scur;
        scur = excludeforecast(scur);
        calcStdev();
        scur = scurwithfcast;

        int noutliers = outliersDetection();
        if (noutliers > 0) {
            removeExtremes();
            scur = scorr;
            scurwithfcast = scur;
            scur = excludeforecast(scur);
            calcStdev();
            scur = scurwithfcast;
            scur = s;
            noutliers = outliersDetection();
        }
        return noutliers;
    }

    /**
     * Applies the detected corrections to the original series
     *
     * @param sorig The original series
     * @param corrections The corrections
     * @return The corrected series. A new time series is always returned.
     */
    @Override
    @NewObject
    public TsData applyCorrections(TsData sorig, TsData corrections) {
        TsData ns = sorig.clone();
        for (int i = 0; i < corrections.getLength(); ++i) {
            double x = corrections.get(i);
            if (!Double.isNaN(x)) {
                ns.set(i, x);
            }
        }
        return ns;
    }

    protected void calcStdev() {
        // one value for each year
        TsPeriod start = scur.getStart(), end = scur.getLastPeriod();
        int y0 = start.getYear(), y1 = end.getYear();
        int ny = y1 - y0 + 1;
        int freq = scur.getFrequency().intValue();
        // number of obs in first year. O if year is complete
        int nbeg = freq - start.getPosition();
        int nfy = ny;
        if (nbeg == freq) {
            nbeg = 0;
        } else {
            --nfy;
        }
        // number of obs in last year. O if year is complete
        int nend = end.getPosition() + 1;
        if (nend == freq) {
            nend = 0;
        } else {
            --nfy;
        }
        DataBlock all = new DataBlock(scur.internalStorage());
        //stdev = new double[ny];
        if (isexcludefcast) {
            stdev = new double[ny + 1];
        } else {
            stdev = new double[ny];
        }

        double e;
        if (nfy < 5) {
            e = calcStdev(all);
            for (int i = 0; i < stdev.length; ++i) {
                stdev[i] = e;
            }
            return;
        }

        int ibeg = 2;
        if (nbeg > 0) {
            ++ibeg;
        }
        int iend = ibeg + nfy - 5;
        // first data block
        DataBlock cur = all.range(0, nbeg + 5 * freq);
        e = calcStdev(cur);
        for (int i = 0; i < ibeg; ++i) {
            stdev[i] = e;
        }
        int pos = nbeg;
        while (ibeg <= iend) {
            cur = all.range(pos, pos + 5 * freq);
            stdev[ibeg++] = calcStdev(cur);
            pos += freq;
        }
        // the last block is too short...
        if (nend > 0) {
            pos -= freq;
            cur = all.range(pos, scur.getLength());
            e = calcStdev(cur);
        } else {
            e = stdev[ibeg - 1];
        }

        for (int i = ibeg; i < stdev.length; ++i) {
            stdev[i] = e;

            //die nächste Zeile muss wieder gelöscht werden
            // if(isexcludefcast){
            //  stdev[ny]=stdev[ny-1];}
        }
    }

    protected double calcStdev(DataBlock data) {
        int n = data.getLength();
        int nm = 0;
        double e = 0;
        for (int i = 0; i < n; ++i) {
            double x = data.get(i);
            if (Double.isNaN(x)) {
                ++nm;
            } else {
                if (isMultiplicative()) {
                    x -= 1;
                }
                e += x * x;
            }
        }
        return Math.sqrt(e / (n - nm));
    }

    /**
     * Computes the corrections for a given series
     *
     * @param s The series being corrected
     * @return A new time series is always returned. It will contain missing
     * values for the periods that should not be corrected and the actual
     * corrections for the other periods
     */
    @Override
    @NewObject
    public TsData computeCorrections(TsData s) {
        TsData ns = new TsData(s.getDomain());
        int n = ns.getLength();
        int beg = ns.getStart().getPosition();
        int freq = ns.getFrequency().intValue();
        double[] avgs = null;
        for (int i = 0; i < n; i++) {
            double e = sweights.get(i);
            if (e == 1.0) {
                ns.set(i, Double.NaN); // 01/02/2006
            } else {
                // correct value
                double x = e * s.get(i);
                //   int[] pos = searchPositionsForOutlierCorrection(i, freq);
                int[] pos;
                if (s.getLength() < sweights.getLength()) {
                    TsData tempsweights = sweights.clone();
                    sweights = sweights.drop(0, sweights.getLength() - s.getLength());
                    pos = searchPositionsForOutlierCorrection(i, freq);
                    sweights = tempsweights;
                } else {
                    pos = searchPositionsForOutlierCorrection(i, freq);
                }

                if (pos != null) {
                    for (int k = 0; k < 4; k++) {
                        x += s.get(pos[k]);
                    }
                    x *= 1.0 / (4.0 + e);
                    ns.set(i, x);
                } else {
                    if (avgs == null) {
                        avgs = periodAverages(s);
                    }
                    ns.set(i, avgs[(beg + i) % freq]);
                }
            }
        }
        return ns;
    }

    /**
     * Gets the correction factors. The correction factors are computed on the
     * original series, using the weights of each observation. The corrections
     * will depend on the type of the decomposition (multiplicative or not).
     *
     * @return A new series is always returned
     */
    @Override
    @NewObject
    public TsData getCorrectionFactors() {
        TsData ns = new TsData(scur.getDomain());
        ns.set(()->context.getMean());
        for (int i = 0; i < sweights.getLength(); ++i) {
            double x = sweights.get(i);
            if (x < 1) {
                double s = scur.get(i);
                if (context.isMultiplicative()) {
                    ns.set(i, s / (1 + x * (s - 1)));
                } else {
                    ns.set(i, s * (1 - x));
                }
            }

        }
        return ns;
    }

    @Override
    @NewObject
    public TsData getObservationWeights() {
        return this.sweights;
    }

    /**
     * Gets the standard deviations for each year
     *
     * @return
     */
    public double[] getStandardDeviations() {
        return stdev;
    }

    protected int outliersDetection() {
        int nval = 0;
        double lv, uv;
        sweights = new TsData(scur.getDomain());
        YearIterator iteri = new YearIterator(scur);
        YearIterator itero = new YearIterator(sweights);
        sweights.set(()->1);

        double xbar = getMean();
        int y = 0;
        while (iteri.hasMoreElements()) {
            if (y > stdev.length - 1) {
                lv = stdev[stdev.length - 1] * lsigma;
                uv = stdev[stdev.length - 1] * usigma;
            } else {
                lv = stdev[y] * lsigma;
                uv = stdev[y] * usigma;
            }
            DataBlock dbi = iteri.nextElement().data;
            DataBlock dbo = itero.nextElement().data;

            for (int i = 0; i < dbi.getLength(); i++) {
                double tt = Math.abs(dbi.get(i) - xbar);
                if (tt > uv) {
                    dbo.set(i, 0.0);
                    ++nval;
                } else if (tt > lv) {
                    dbo.set(i, (uv - tt) / (uv - lv));
                }
            }
            y++;
        }
        return nval;
    }

    private void removeExtremes() {
        scorr = scur.clone();
        for (int i = 0; i < sweights.getLength(); ++i) {
            if (sweights.get(i) == 0) {
                scorr.set(i, Double.NaN);
            }
        }
    }

    private int[] searchPositionsForOutlierCorrection(int p, final int frequency) {
        int lp = 0, up = 0, lb = p, ub = p, k = 0;

        int[] outs = new int[4];
        // look for two positions above value
        while (lb >= frequency && lp != 2) {
            lb -= frequency;
            if (sweights.get(lb) == 1.0) {
                lp++;
                outs[k++] = lb;
            }
        }
        // look for two positions below value
        int len = sweights.getLength();
        while (ub < (len - frequency) && up != 2) {
            ub += frequency;
            if (sweights.get(ub) == 1.0) {
                up++;
                outs[k++] = ub;
            }
        }

        if (lp < 2) {
            while (ub < (len - frequency) && k < 4) {
                ub += frequency;
                if (sweights.get(ub) == 1.0) {
                    lp++;
                    outs[k++] = ub;
                }
            }
        } else if (up < 2) {
            while (lb >= frequency && k < 4) {
                lb -= frequency;
                if (sweights.get(lb) == 1.0) {
                    up++;
                    outs[k++] = lb;
                }
            }
        }

        if (lp + up < 4) {
            return null;
        }
        return outs;
    }

    /**
     * Gets the lower sigma value
     *
     * @return
     */
    public double getLowerSigma() {
        return lsigma;
    }

    /**
     * Gets the upper sigma value
     *
     * @return
     */
    public double getUpperSigma() {
        return usigma;
    }

    /**
     * Sets the limits for the detection of extreme values.
     *
     * @param lsig The low sigma value
     * @param usig The high sigma value
     * @throws An exception is thrown when the limits are invalid (usig <= lsig
     * or lsig <= 0.5).
     */
    public void setSigma(double lsig, double usig) {
        if (usig <= lsig || lsig <= 0.5) {
            throw new X11Exception("Invalid sigma options");
        }
        lsigma = lsig;
        usigma = usig;
    }

    private TsData excludeforecast(TsData tsWithForcast) {
        TsData tsWithoutforCast;
        if (isexcludefcast) {
            tsWithoutforCast = tsWithForcast.drop(context.getBackcastHorizon(),context.getForecastHorizon());
            return tsWithoutforCast;
        } else {
            return tsWithForcast;
        }
    }

    @Override
    public void setExcludefcast(boolean isExcludefcast) {
        isexcludefcast = isExcludefcast;
    }

    @Override
    public boolean getExcludefcast() {
        return isexcludefcast;
    }

//    @Override
//    public void setForecasthorizont(int forcasthorizont) {
//        this.forcasthorizont = forcasthorizont;
//
//    }
//
//    @Override
//    public int getForecasthorizont() {
//        return this.forcasthorizont;
//
//    }
}
