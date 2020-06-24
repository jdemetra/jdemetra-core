/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x11.filter;

import demetra.data.DoubleSeq;
import demetra.sa.DecompositionMode;
import demetra.x11.SeasonalFilterOption;
import jdplus.x11.X11Context;
import static jdplus.x11.X11Kernel.table;
import jdplus.x11.filter.endpoints.FilteredMeanEndPoints;
import jdplus.data.DataBlock;
import jdplus.math.linearfilters.SymmetricFilter;
import demetra.x11.MsrTable;

/**
 *
 * @author Nina Gonschorreck
 */
public class MsrFilterSelection {

    private static final double[] C = {1.00000, 1.02584, 1.01779, 1.01383,
        1.00000, 3.00000, 1.55291, 1.30095};

    private DoubleSeq seas;
    private DoubleSeq irr;

    private MsrTable msrTable;
    private double msr;
    private int iter;
    private boolean useDefault;

    public MsrTable getMsrTable() {
        return msrTable;
    }

    public double getGlobalMsr() {
        return msr;
    }

    public int getIterCount() {
        return iter;
    }

    public boolean isUsingDefault() {
        return useDefault;
    }

    public SeasonalFilterOption doMSR(DoubleSeq data, X11Context context) {
        SeasonalFilterOption seasFilter = null;
        //0. Remove fore- and backcast
        int nf = context.getForecastHorizon();
        int nb = context.getBackcastHorizon();
        DoubleSeq series = data.drop(nb, nf);

        // 0. complete year
        series = completeYear(series, context);
        useDefault = false;
        iter = 0;
        msr = 0;
        do {
            ++iter;
            // 1. calc Components
            calcComponents(series, context);
            // 2. calc periodic variations
            msrTable = MsrTable.of(seas, irr, context.getPeriod(), context.getFirstPeriod(), context.getMode() == DecompositionMode.Multiplicative);
            // 3. calc gmsr
            msr = msrTable.getGlobalMsr();
            // 4. decision
            seasFilter = decideFilter(msr);
            if (seasFilter != null) {
                break;
            }
            // 5. cut year
            series = series.drop(0, context.getPeriod());
//          As we have shortend the series, we must adapt the test on the length (5 instead of 6)
        } while (series.length() / context.getPeriod() >= 5);
        if (seasFilter == null) {
            useDefault = true;
            seasFilter = SeasonalFilterOption.S3X5;
        }
        return seasFilter;
    }

    private DoubleSeq completeYear(DoubleSeq series, X11Context context) {
        //check incomplete year
        int cut = (series.length() + context.getFirstPeriod()) % context.getPeriod();
        return series.drop(context.getFirstPeriod(), cut);
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
