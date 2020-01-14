/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x11.filter;

import demetra.data.DoubleSeq;
import demetra.sa.DecompositionMode;
import demetra.x11.SeasonalFilterOption;
import demetra.x11.X11Context;
import static demetra.x11.X11Kernel.table;
import demetra.x11.filter.endpoints.FilteredMeanEndPoints;
import jdplus.data.DataBlock;
import jdplus.math.linearfilters.SymmetricFilter;

/**
 *
 * @author Nina Gonschorreck
 */
public class MsrFilterSelection {

    private static final double[] C = {1.00000, 1.02584, 1.01779, 1.01383,
        1.00000, 3.00000, 1.55291, 1.30095};

    private DoubleSeq seas;
    private DoubleSeq irr;

    private double[] s;
    private double[] i;
    private int[] n;

    public SeasonalFilterOption doMSR(DoubleSeq data, X11Context context) {
        SeasonalFilterOption seasFilter = null;
        //0. Remove fore- and backcast
        int nf = context.getForecastHorizon();
        int nb = context.getBackcastHorizon();
        DoubleSeq series = data.drop(nb, nf);

        // 0. complete year
        series = completeYear(series, context);
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
            // 5. cut year
            series = series.drop(0, context.getPeriod());
//          As we have shortend the series, we must adapt the test on the length (5 instead of 6)
        } while (seasFilter == null && series.length() / context.getPeriod() >= 5);
        if (seasFilter == null) {
            seasFilter = SeasonalFilterOption.S3X5;
        }
        return seasFilter;
    }

    private DoubleSeq completeYear(DoubleSeq series, X11Context context) {
        //check incomplete year
        int cut = (series.length() + context.getFirstPeriod()) % context.getPeriod();
        return series.drop(0, cut);
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
        irr = calcIrregular(context, series, seas);
    }

    protected DoubleSeq calcIrregular(X11Context context, DoubleSeq series, DoubleSeq seas) {
        return context.remove(series, seas);
    }

    private void calcPeriodicVariation(X11Context context) {

        int start = context.getFirstPeriod();
        int period = context.getPeriod();
        boolean multi = DecompositionMode.Multiplicative.equals(context.getMode());

        s = new double[period];
        i = new double[period];
        n = new int[period];

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
            return n * 12.247449 / (73.239334 + (n - 6) * 12.247449);
        }
    }

    private double cs(int n) {
        if (n < 2) {
            return 1;
        } else if (n < 6) {
            return C[n + 2];
        } else {
            return n * 1.732051 / (8.485281 + (n - 6) * 1.732051);
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
