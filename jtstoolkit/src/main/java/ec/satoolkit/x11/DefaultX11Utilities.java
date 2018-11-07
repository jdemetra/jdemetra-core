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
import ec.tstoolkit.maths.linearfilters.SymmetricFilter;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class DefaultX11Utilities extends DefaultX11Algorithm implements IX11Utilities {

    private double eps = 1e-9;

    /**
     * Replace negative values with either the mean of the two nearest positive
     * replacements before and after the value, or the nearest value if it is on
     * the ends of the series.
     *
     * @param s
     * @return
     */
    @Override
    public boolean checkPositivity(TsData s) {
        double[] stc = s.internalStorage();
        int n = s.getLength();
        boolean changed = false;
        for (int i = 0; i < n; ++i) {
            if (stc[i] <= 0) {
                changed = true;
                int before = i - 1;
                while (before >= 0 && stc[before] <= 0) {
                    --before;
                }
                int after = i + 1;
                while (after < n && stc[after] <= 0) {
                    ++after;
                }
                double m;
                if (before < 0 && after >= n) {
                    throw new X11Exception("Negative series");
                }
                if (before >= 0 && after < n) {
                    m = (stc[before] + stc[after]) / 2;
                } else if (after >= n) {
                    m = stc[before];
                } else {
                    m = stc[after];
                }
                stc[i] = m;
            }
        }
        return changed;
    }

    /**
     * Corrects a series, using weights attached to the observations.
     *
     * @param sorig The series being corrected
     * @param sweights The weights of the different data of the series
     * @param dalternative The value that will be used to replace values with a
     * weight equal to 0
     * @return
     */
    @Override
    public TsData correctSeries(TsData sorig, TsData sweights,
            double dalternative) {
        TsData ns = sorig.clone();
        for (int i = 0; i < ns.getLength(); ++i) {
            double x = sweights.get(i);
            if (x == 0) {
                ns.set(i, dalternative);
            }
        }
        return ns;
    }

    /**
     * Corrects a series, using weights attached to the observations and an
     * alternative series
     *
     * @param sorig The series being corrected
     * @param sweights The weights of the different data of the series
     * @param salternative The series containing the alternative values
     * @return The corrected series
     */
    @Override
    public TsData correctSeries(TsData sorig, TsData sweights,
            TsData salternative) {
        TsData ns = sorig.clone();
        for (int i = 0; i < ns.getLength(); ++i) {
            double x = sweights.get(i);
            if (x == 0) {
                ns.set(i, salternative.get(i));
            }
        }
        return ns;
    }

    /**
     *
     * @param t
     * @param s
     * @param i
     * @return
     */
    @Override
    public TsData correctTrendBias(TsData t, TsData s, TsData i, BiasCorrection bias) {
        switch (bias){
            case Legacy:
                return legacyBiasCorrection(t, s, i);
            case Ratio:
                return ratioBiasCorrection(t, s, i);
            case Smooth:
                return smoothBiasCorrection(t, s, i);
            default:
                return t;
        }
    }
    
    private TsData legacyBiasCorrection(TsData t, TsData s, TsData i) {
        double issq = i.log().ssq();
        double sig = Math.exp(issq / (2 * i.getLength()));
        int ifreq = t.getFrequency().intValue();
        int length = 2 * ifreq - 1;
        SymmetricFilter smoother = TrendCycleFilterFactory
                .makeHendersonFilter(length);
        DefaultTrendFilteringStrategy filter = new DefaultTrendFilteringStrategy(
                smoother, new AsymmetricEndPoints(MusgraveFilterFactory
                .makeFilters(smoother, 4.5)));

        TsData hs = filter.process(s, null);
        hs.applyOnFinite(x -> x * sig);
        return t.times(hs);
    }

    private TsData smoothBiasCorrection(TsData t, TsData s, TsData i) {
        double issq = i.log().ssq();
        double sig = Math.exp(issq / (2 * i.getLength()));
        int ifreq = t.getFrequency().intValue();
        TsData hs=new DefaultNormalizingStrategie().process(s, null, ifreq);
        hs.applyOnFinite(x -> x * sig);
        return t.times(hs);
    }

    private TsData ratioBiasCorrection(TsData t, TsData s, TsData i) {
        // average of s, i on complete years
        double sbias=s.fullYears().average(), ibias=i.average();
        s.apply(x->x/sbias);
        return t.times(sbias*ibias);
    }
    /**
     *
     * @param l
     * @param r
     * @return
     */
    @Override
    public TsData differences(final TsData l, final TsData r) {
        TsData o = new TsData(l.getDomain());
        int n = o.getLength();
        for (int i = 0; i < n; ++i) {
            double xl = l.get(i), xr = r.get(i);
            if (Math.abs(xl - xr) > eps) {
                o.set(i, xl);
            }
        }
        return o;
    }

    /**
     * @return the eps
     */
    public double getEpsilon() {
        return eps;
    }

    /**
     * @param eps the eps to set
     */
    public void setEpsilon(double eps) {
        this.eps = eps;
    }
}
