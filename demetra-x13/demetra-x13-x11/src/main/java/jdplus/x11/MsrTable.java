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
package jdplus.x11;

import demetra.data.DoubleSeq;
import demetra.data.DoublesMath;
import demetra.sa.DecompositionMode;
import jdplus.data.DataBlock;
import jdplus.math.linearfilters.SymmetricFilter;
import static jdplus.x11.X11Kernel.table;
import jdplus.x11.filter.X11FilterFactory;
import jdplus.x11.filter.endpoints.FilteredMeanEndPoints;

/**
 * Moving seasonality table.
 * This class generates information that corresponds to the table D9a
 *
 * @author Frank Osaer, Jean Palate
 */
public final class MsrTable {

    private static double[] C = {1.00000e0, 1.02584e0, 1.01779e0, 1.01383e0,
        1.00000e0, 3.00000e0, 1.55291e0, 1.30095e0};

    /**
     *
     * @param ss Seasonal component
     * @param is Irregular component
     * @param firstPeriod Position of the first observation in a cycle
     * @param period Length of a cycle
     * @param mul Multiplicative decomposition
     * @return
     */
    public static MsrTable of(DoubleSeq ss, DoubleSeq is, int firstPeriod, int period, boolean mul) {

        MsrTable rms = new MsrTable();
        // should be improved by means of partial iterators
        rms.rs = new double[period];
        rms.ri = new double[period];
        rms.n = new int[period];
        int n = ss.length();
        for (int i = 0; i < period; ++i) {
            int start = i >= firstPeriod ? i - firstPeriod : (period + i - firstPeriod);
            double ci = 0, cs = 0;
            int nc = 0;
            for (int jprev = start, j = start + period; j < n; jprev = j, j += period) {
                double x0 = is.get(jprev);
                double x1 = is.get(j);
                double d = x1 - x0;
                if (mul) {
                    d /= x0;
                }
                ci += Math.abs(d);
                x0 = ss.get(jprev);
                x1 = ss.get(j);
                d = x1 - x0;
                if (mul) {
                    d /= x0;
                }
                cs += Math.abs(d);
                ++nc;
            }
            rms.ri[i] = ci / nc * fis(nc);
            rms.rs[i] = cs / nc * cs(nc);
            rms.n[i] = nc;
        }
        return rms;
    }
    
    /**
     * Generates default MsrTable. The series is split in seas and irr. component
     * using the default seasonal filter on 7 periods (S3x5) and stable handling of end-points
     * @param s
     * @param firstPeriod
     * @param period
     * @param mode Decomposition mode
     * @return 
     */
    public static MsrTable defaultMsrTable(DoubleSeq s, int firstPeriod, int period, DecompositionMode mode){
        SymmetricFilter filter = X11FilterFactory.makeSymmetricFilter(7);
        FilteredMeanEndPoints f = new FilteredMeanEndPoints(filter);

        int cut = (s.length() + firstPeriod) % period;
        s=s.drop(0, cut);
        int n=s.length();
        double[] x = new double[n];
        DataBlock ss = DataBlock.of(x);

        // 1. Seasonal
        for (int j = 0; j < period; j++) {
            int nj=1+(n-1-j)/period;
            DoubleSeq bin = s.extract(j, nj, period);
            DataBlock bout = ss.extract(j, nj, period);
            f.process(bin, bout);
        }
        
        DoubleSeq is;
        boolean mul=false;
        switch (mode){
            case PseudoAdditive:
                is=s.fn(ss, (a,b)->a-b+1);
                break;
            case Multiplicative:
                is=DoublesMath.divide(s, ss);
                mul=true;
                break;
            default:  
                is=DoublesMath.subtract(s, ss);
        }

         // 3. MSR table
        return of (ss, is, firstPeriod, period, mul);
    }

    private static double cs(int n) {
        if (n < 2) {
            return 1;
        } else if (n < 6) {
            return C[n + 2];
        } else {
            return n * 1.732051e0 / (8.485281e0 + (n - 6) * 1.732051e0);
        }
    }

    private static double fis(int n) {
        if (n < 2) {
            return 1;
        } else if (n < 6) {
            return C[n - 2];
        } else {
            return n * 12.247449e0 / (73.239334e0 + (n - 6) * 12.247449e0);
        }
    }

    private double[] rs;
    private double[] ri;
    private int[] n;

    private MsrTable() {
    }

    private MsrTable(double[] rs, double[] ri, int[] n) {
        this.rs = rs;
        this.ri = ri;
        if (n != null) {
            this.n = n;
        }
    }

    /**
     * Gets the number of elements per array. Also the periodicity
     *
     * @return
     */
    public int getCount() {
        return ri.length;
    }

    /**
     * The property returns the global Moving Seasonality Ratio. This is a
     * weighted sum
     * of the per-period ratios. Weighting is done using the number of years per
     * period.
     *
     * @return
     */
    public double getGlobalMsr() {

        double gri = 0.0, grs = 0.0;
        for (int i = 0; i < ri.length; i++) {
            gri += ri[i] * n[i];
            grs += rs[i] * n[i];
        }
        return gri / grs;
    }

    /**
     * The property returns the mean evolutions of the irregular component
     *
     * @return
     */
    public double[] getMeanIrregularEvolutions() {
        return ri;
    }

    /**
     * The property returns the mean evolutions of the seasonal component
     *
     * @return
     */
    public double[] getMeanSeasonalEvolutions() {
        return rs;
    }

    /**
     * The method returns the Moving Seasonality Ratio for the given period.
     *
     * @param idx >The position of the period (between 0 and period)
     * @return
     */
    public double getRMS(int idx) {
        return ri[idx] / rs[idx];
    }
}
