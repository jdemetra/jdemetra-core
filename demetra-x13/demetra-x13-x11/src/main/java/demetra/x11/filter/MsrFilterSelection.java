/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x11.filter;

import jdplus.data.DataBlock;
import jdplus.maths.linearfilters.SymmetricFilter;
import demetra.x11.SeasonalFilterOption;
import demetra.x11.X11Context;
import static demetra.x11.X11Kernel.table;
import demetra.x11.filter.endpoints.FilteredMeanEndPoints;
import demetra.data.DoubleSeq;

/**
 *
 * @author Nina Gonschorreck
 */
public class MsrFilterSelection {

    private static final double[] C = {1.00000e0, 1.02584e0, 1.01779e0, 1.01383e0,
        1.00000e0, 3.00000e0, 1.55291e0, 1.30095e0};

    private DoubleSeq seas;
    private DoubleSeq irr;

    private double[] s;
    private double[] i;
    private double[] n;

    public SeasonalFilterOption doMSR(DoubleSeq data, X11Context context) {
        SeasonalFilterOption seasFilter = null;
        // 0. complete year
        DoubleSeq series = completeYear(data, context);
        double msr;
        do {
            // 1. calc Components
            calcComponents(series, context);
            // 2. calc periodic variations
            calcPeriodicVariation(context);
            // 3. calc gmsr
            msr = getGlobalMsr();
            // 4. decision
            seasFilter = decideFilter(msr);
            series = series.drop(0, context.getPeriod());
//          As we have shortend the series, we must adapt the test on the length (5 instead of 6)
        } while (seasFilter == null && series.length() / context.getPeriod() >= 5);
        if (seasFilter == null) {
            seasFilter = SeasonalFilterOption.S3X5;
        }
        return seasFilter;
    }

    private DoubleSeq completeYear(DoubleSeq series, X11Context context) {
        DoubleSeq seriesCopy;
        //check incomplete year
        int cut = (series.length() + context.getFirstPeriod()) % context.getPeriod();
        if (cut != 0) {
            seriesCopy = series.drop(0, cut);
        } else {
            seriesCopy = series.drop(0, 0);
        }
        return seriesCopy;
    }

    private void calcComponents(DoubleSeq series, X11Context context) {
        // 0. Remove fore- and backcast

        //TODO
        // 1. estimate series component
        SymmetricFilter filter = X11FilterFactory.makeSymmetricFilter(7);
        FilteredMeanEndPoints f = new FilteredMeanEndPoints(filter);

        double[] x = table(series.length(), Double.NaN);
        DataBlock out = DataBlock.of(x);

        for (int j = 0; j < context.getPeriod(); j++) {
            DataBlock bin = DataBlock.of(series).extract(j, -1, context.getPeriod());
            DataBlock bout = out.extract(j, -1, context.getPeriod());
            f.process(bin, bout);
        }

        seas = out;

        // 2. estimate irregular component
        irr = context.remove(series, seas);
    }

    private void calcPeriodicVariation(X11Context context) {

        int start = context.getFirstPeriod();
        int period = context.getPeriod();
        boolean multi = context.isMultiplicative();

        s = new double[period];
        i = new double[period];
        n = new double[period];

        DataBlock iter_seas = DataBlock.of(seas);
        DataBlock iter_irr = DataBlock.of(irr);

        for (int p = 0; p < period; p++) {
            int curPeriod = (start + p) % period;
            double[] seasPeriodspecific = iter_seas.extract(curPeriod, -1, period).toArray();
            double[] irrPeriodspecific = iter_irr.extract(curPeriod, -1, period).toArray();

            double ci = 0.0, cs = 0.0;
            for (int j = 1; j < seasPeriodspecific.length; j++) {
                double ds = seasPeriodspecific[j] - seasPeriodspecific[j - 1];
                double di = irrPeriodspecific[j] - irrPeriodspecific[j - 1];
                if (multi) {
                    ds /= seasPeriodspecific[j - 1];
                    di /= irrPeriodspecific[j - 1];
                }
                cs += Math.abs(ds);
                ci += Math.abs(di);
            }
            int nc = seasPeriodspecific.length - 1;
            s[p] = cs / nc * cs(nc);
            i[p] = ci / nc * fis(nc);
            n[p] = nc;
        }
    }

    private double fis(int n) {
        if (n < 2) {
            return 1;
        } else if (n < 6) {
            return C[n - 2];
        } else {
            return n * 12.247449e0 / (73.239334e0 + (n - 6) * 12.247449e0);
        }
    }

    private double cs(int n) {
        if (n < 2) {
            return 1;
        } else if (n < 6) {
            return C[n + 2];
        } else {
            return n * 1.732051e0 / (8.485281e0 + (n - 6) * 1.732051e0);
        }
    }

    public double getGlobalMsr() {

        double ri = 0.0, rs = 0.0;
        for (int j = 0; j < i.length; j++) {
            ri += i[j] * n[j];
            rs += s[j] * n[j];
        }
        return ri / rs;
    }

    private SeasonalFilterOption decideFilter(double msr) {
        // table of msr
        if (msr < 2.5) {
            return SeasonalFilterOption.S3X3;
        } else if (msr >= 2.5 && msr < 3.5) {
            return null;
        } else if (msr >= 3.5 && msr < 5.5) {
            return SeasonalFilterOption.S3X5;
        } else if (msr >= 5.5 && msr < 6.5) {
            return null;
        } else {
            return SeasonalFilterOption.S3X9;
        }
    }
}
