/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x11;

import demetra.data.DataBlock;
import demetra.data.DoubleSequence;
import demetra.maths.linearfilters.IFilterOutput;
import demetra.maths.linearfilters.IFiniteFilter;
import demetra.maths.linearfilters.SymmetricFilter;
import static demetra.x11.X11Kernel.table;
import demetra.x11.extremevaluecorrector.IExtremeValuesCorrector;
import demetra.x11.filter.AutomaticHenderson;
import demetra.x11.filter.DefaultSeasonalNormalizer;
import demetra.x11.filter.MusgraveFilterFactory;
import demetra.x11.filter.X11FilterFactory;
import demetra.x11.filter.X11SeasonalFilterProcessor;
import demetra.x11.filter.X11SeasonalFiltersFactory;
import demetra.x11.filter.endpoints.AsymmetricEndPoints;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Getter
public class X11BStep {

    private DoubleSequence b1, b2, b3, b4, b4a, b4anorm, b4d, b4g, b5, b6,
            b7, b8, b9, b9g, b10, b11, b13, b17, b20;
    private int b2drop;

    public X11BStep() {
    }

    public void process(DoubleSequence input, X11Context context) {

        b1 = input;
        b2(context);
        b3(context);
        b4(context);
        b5(context);
        b6(context);
        b7(context);
        b8(context);
        b9(context);
        bfinal(context);
    }

    private void b2(X11Context context) {
        SymmetricFilter filter = X11FilterFactory.makeSymmetricFilter(context.getPeriod());
        b2drop = filter.length() / 2;

        double[] x = table(b1.length() - 2 * b2drop, Double.NaN);
        DataBlock out = DataBlock.ofInternal(x, 0, x.length);
        filter.apply(i -> b1.get(i), IFilterOutput.of(out, b2drop));
        b2 = DoubleSequence.ofInternal(x);
    }

    private void b3(X11Context context) {
        b3 = context.remove(b1.drop(b2drop, b2drop), b2);
    }

    private void b4(X11Context context) {
        X11SeasonalFilterProcessor processor = X11SeasonalFiltersFactory.filter(context.getPeriod(), context.getInitialSeasonalFilter());
        b4a = processor.process(b3, (context.getFirstPeriod() + b2drop) % context.getPeriod());

        b4anorm = DefaultSeasonalNormalizer.normalize(b4a, 0, context, b2drop);
        b4d = context.remove(b3, b4anorm);
        IExtremeValuesCorrector ecorr = context.selectExtremeValuesCorrector(b4d);
        int j = (b2drop + context.getFirstPeriod()) % context.getPeriod();
        ecorr.setStart(j);
        ecorr.analyse(b4d, context);

        b4 = ecorr.computeCorrections(b3);
        b4g = ecorr.applyCorrections(b3, b4);
    }

    private void b5(X11Context context) {
        X11SeasonalFilterProcessor processor = X11SeasonalFiltersFactory.filter(context.getPeriod(), context.getInitialSeasonalFilter());
        DoubleSequence b5a = processor.process(b4g, (context.getFirstPeriod() + b2drop) % context.getPeriod());
        b5 = DefaultSeasonalNormalizer.normalize(b5a, b2drop, context);
    }

    private void b6(X11Context context) {
        b6 = context.remove(b1, b5);
    }

    private void b7(X11Context context) {
        SymmetricFilter filter;
        if (context.isAutomaticHenderson()) {
            double icr = AutomaticHenderson.calcICR(context, b6);
            int filterLength;
            if (icr >= 1.0) {
                filterLength = context.getPeriod() + 1;
            } else {
                filterLength = AutomaticHenderson.selectFilter(icr, context.getPeriod());
            }
            filter = context.trendFilter(filterLength);
        } else {
            filter = context.trendFilter();
        }
        int ndrop = filter.length() / 2;

        double[] x = table(b6.length(), Double.NaN);
        DataBlock out = DataBlock.ofInternal(x, ndrop, x.length - ndrop);
        filter.apply(i -> b6.get(i), IFilterOutput.of(out, ndrop));

        // apply asymmetric filters
        double r = MusgraveFilterFactory.findR(filter.length(), context.getPeriod());
        IFiniteFilter[] asymmetricFilter = context.asymmetricTrendFilters(filter, r);
        AsymmetricEndPoints aep = new AsymmetricEndPoints(asymmetricFilter, 0);
        aep.process(b6, DataBlock.ofInternal(x));
        b7 = DoubleSequence.ofInternal(x);
        if (context.isMultiplicative()) {
            b7 = X11Context.makePositivity(b7);
        }
    }

    private void b8(X11Context context) {
        b8 = context.remove(b1, b7);
    }

    private void b9(X11Context context) {
        X11SeasonalFilterProcessor processor = X11SeasonalFiltersFactory.filter(context.getPeriod(), context.getFinalSeasonalFilter());
        DoubleSequence b9a = processor.process(b8, context.getFirstPeriod());
        DoubleSequence b9c = DefaultSeasonalNormalizer.normalize(b9a, 0, context);
        DoubleSequence b9d = context.remove(b8, b9c);
        IExtremeValuesCorrector ecorr = context.getExtremeValuesCorrector();
        ecorr.setStart(context.getFirstPeriod());
        ecorr.analyse(b9d, context);

        b9 = ecorr.computeCorrections(b8);
        b9g = ecorr.applyCorrections(b8, b9);
    }

    private void bfinal(X11Context context) {
        X11SeasonalFilterProcessor processor = X11SeasonalFiltersFactory.filter(context.getPeriod(), context.getFinalSeasonalFilter());
        DoubleSequence b10a = processor.process(b9g, context.getFirstPeriod());
        b10 = DefaultSeasonalNormalizer.normalize(b10a, 0, context);
        b11 = context.remove(b1, b10);
        b13 = context.remove(b11, b7);

        IExtremeValuesCorrector ecorr = context.selectExtremeValuesCorrector(b13);
        ecorr.setStart(context.getFirstPeriod());
        ecorr.analyse(b13, context);
        b17 = ecorr.getObservationWeights();
        b20 = ecorr.getCorrectionFactors();
    }
}
