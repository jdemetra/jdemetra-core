/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x11;

import demetra.data.DataBlock;
import demetra.maths.linearfilters.IFiniteFilter;
import demetra.maths.linearfilters.SymmetricFilter;
import static demetra.x11.X11Kernel.table;
import demetra.x11.extremevaluecorrector.IExtremeValuesCorrector;
import demetra.x11.filter.AutomaticHenderson;
import demetra.x11.filter.DefaultSeasonalNormalizer;
import demetra.x11.filter.IFiltering;
import demetra.x11.filter.MusgraveFilterFactory;
import demetra.x11.filter.X11FilterFactory;
import demetra.x11.filter.X11SeasonalFiltersFactory;
import demetra.x11.filter.endpoints.AsymmetricEndPoints;
import lombok.AccessLevel;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Getter
public class X11CStep {

    private DoubleSeq c1, c2, c4, c5a, c5, c6, c7, c9, c10a, c10, c11, c13, c17, c20;

    @lombok.Getter(AccessLevel.NONE)
    private DoubleSeq refSeries;
    private int c2drop;

    public void process(DoubleSeq refSeries, DoubleSeq input, X11Context context) {
        this.refSeries = refSeries;
        c1 = input;
        c2(context);
        c4(context);
        c5(context);
        c6(context);
        c7(context);
        c9(context);
        cfinal(context);
    }

    private void c2(X11Context context) {
        SymmetricFilter filter = X11FilterFactory.makeSymmetricFilter(context.getPeriod());
        c2drop = filter.length() / 2;

        double[] x = table(c1.length() - 2 * c2drop, Double.NaN);
        DataBlock out = DataBlock.of(x, 0, x.length);
        filter.apply(c1, out);
        c2 = DoubleSeq.of(x);
    }

    private void c4(X11Context context) {
        c4 = context.remove(c1.drop(c2drop, c2drop), c2);
    }

    private void c5(X11Context context) {
        IFiltering filter = X11SeasonalFiltersFactory.filter(context.getPeriod(), context.getInitialSeasonalFilter());
        c5a = filter.process(c4);
        c5 = DefaultSeasonalNormalizer.normalize(c5a, c2drop, context);
    }

    private void c6(X11Context context) {
        c6 = context.remove(c1, c5);
    }

    private void c7(X11Context context) {
        SymmetricFilter filter;
        if (context.isAutomaticHenderson()) {
            double icr = AutomaticHenderson.calcICR(context, c6);
            int filterLength = AutomaticHenderson.selectFilter(icr, context.getPeriod());
            filter = context.trendFilter(filterLength);
        } else {
            filter = context.trendFilter();
        }
        int ndrop = filter.length() / 2;

        double[] x = table(c6.length(), Double.NaN);
        DataBlock out = DataBlock.of(x, ndrop, x.length - ndrop);
        filter.apply(c6, out);

        // apply asymmetric filters
        double r = MusgraveFilterFactory.findR(filter.length(), context.getPeriod());
        IFiniteFilter[] asymmetricFilter = context.asymmetricTrendFilters(filter, r);
        AsymmetricEndPoints aep = new AsymmetricEndPoints(asymmetricFilter, 0);
        aep.process(c6, DataBlock.of(x));
        c7 = DoubleSeq.of(x);
        if (context.isMultiplicative()) {
            c7 = context.makePositivity(c7);
        }
    }

    private void c9(X11Context context) {
        c9 = context.remove(c1, c7);
    }

    private void cfinal(X11Context context) {
        IFiltering filter = X11SeasonalFiltersFactory.filter(context.getPeriod(), context.getFinalSeasonalFilter());
        c10a = filter.process(c9);
        c10 = DefaultSeasonalNormalizer.normalize(c10a, 0, context);
        c11 = context.remove(refSeries, c10);
        c13 = context.remove(c11, c7);

        IExtremeValuesCorrector ecorr = context.getExtremeValuesCorrector();
        ecorr.setStart(context.getFirstPeriod());
        ecorr.analyse(c13, context);
        c17 = ecorr.getObservationWeights();
        c20 = ecorr.getCorrectionFactors();
    }
}
