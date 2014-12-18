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
import ec.tstoolkit.timeseries.simplets.PeriodIterator;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;

/**
 *
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SeriesEvolution {

    public static double[] calcAbsMeanVariations(TsData s, TsDomain domain,
            boolean mul) {
        int freq = s.getFrequency().intValue();
        double[] mean = new double[freq];

        double[] x = s.getValues().internalStorage();
        int istart, iend;
        if (domain != null) {
            istart = s.getDomain().search(domain.getStart());
            iend = s.getDomain().search(domain.getLast()) + 1;
        }
        else {
            istart = 0;
            iend = s.getLength();
        }
        for (int l = 1; l <= freq; ++l) {
            double sum = 0;
            for (int i = istart + l; i < iend; ++i) {
                double x1 = x[i], x0 = x[i - l];
                double d = x1 - x0;
                if (mul) {
                    d *= 100/x0;
                }
                sum += Math.abs(d);
            }
            mean[l - 1] = sum / (iend - istart - l);
        }
        return mean;
    }

    public static double calcAbsMeanVariations(TsData s, TsDomain domain,
            int lag, boolean mul) {
        double[] x = s.getValues().internalStorage();
        int istart, iend;
        if (domain != null) {
            istart = s.getDomain().search(domain.getStart());
            iend = s.getDomain().search(domain.getLast()) + 1;
        }
        else {
            istart = 0;
            iend = s.getLength();
        }
        double sum = 0;
        for (int i = istart + lag; i < iend; ++i) {
            double x1 = x[i], x0 = x[i - lag];
            double d = Math.abs(x1 - x0);
            if (mul) {
                d *= 100/x0;
            }
            sum += d;
        }
        return sum / (iend - istart - lag);
    }

    public static double[] calcAbsMeanVariationsByPeriod(TsData s,
            TsDomain domain, boolean mul) {
        int freq = s.getFrequency().intValue();
        // should be improved by means of partial iterators
        double[] pmean = new double[freq];
        PeriodIterator bi = domain == null ? new PeriodIterator(s) : new PeriodIterator(s, domain);
        int tell = 0;
        while (bi.hasMoreElements()) {
            double cc = 0.0;
            DataBlock bd = bi.nextElement().data;
            int nc = bd.getLength();
            for (int i = 1; i < nc; i++) {
                double x0 = bd.get(i - 1);
                double x1 = bd.get(i);
                double d = x1 - x0;
                if (mul) {
                    d *= 100/x0;
                }

                cc += Math.abs(d);
            }
            pmean[tell++] = cc / (nc - 1);
        }
        return pmean;
    }

    public static double[] calcMeanVariations(TsData s, TsDomain domain,
            boolean mul) {
        int freq = s.getFrequency().intValue();
        double[] mean = new double[freq];

        double[] x = s.getValues().internalStorage();
        int istart, iend;
        if (domain != null) {
            istart = s.getDomain().search(domain.getStart());
            iend = s.getDomain().search(domain.getLast()) + 1;
        }
        else {
            istart = 0;
            iend = s.getLength();
        }
        for (int l = 1; l <= freq; ++l) {
            double sum = 0;
            for (int i = istart + l; i < iend; ++i) {
                double x1 = x[i], x0 = x[i - l];
                double d = x1 - x0;
                if (mul) {
                    d *= 100/x0;
                }
                sum += d;
            }
            mean[l - 1] = sum / (iend - istart - l);
        }
        return mean;
    }

    public static double[][] calcVariations(TsData s, TsDomain domain,
            boolean mul) {
        int freq = s.getFrequency().intValue();
        double[] mean = new double[freq];
        double[] std = new double[freq];

        double[] x = s.getValues().internalStorage();
        int istart, iend;
        if (domain != null) {
            istart = s.getDomain().search(domain.getStart());
            iend = s.getDomain().search(domain.getLast()) + 1;
        }
        else {
            istart = 0;
            iend = s.getLength();
        }
        for (int l = 1; l <= freq; ++l) {
            double sum = 0, sum2 = 0;
            for (int i = istart + l; i < iend; ++i) {
                double x1 = x[i], x0 = x[i - l];
                double d = x1 - x0;
                if (mul) {
                    d *= 100/x0;
                }
                sum += d;
                sum2 += d * d;
            }
            int n = (iend - istart - l);
                mean[l - 1] = sum / n;
                std[l - 1] = Math.sqrt((sum2 - sum * sum / n) / n);
         }
        return new double[][]{mean, std};
    }

    public static double Adr(TsData ts, boolean mul) {
        if (ts == null) {
            return 0;
        }
        TsData del = mul ? ts.pctVariation(1) : ts.delta(1);
        int n = del.getLength();
        double[] x = del.getValues().internalStorage();
        int c = 0;
        int s = 0;
        for (int i = 0; i < n; ++i) {
            int cur = sign(x[i]);
            if (s != cur && cur != 0) {
                ++c;
                s = cur;
            }
        }
        double N = n;
        return N / c;
    }

    private static int sign(double val) {
        if (val < 0) {
            return -1;
        }
        else if (val > 0) {
            return 1;
        }
        else {
            return 0;
        }
    }
}
